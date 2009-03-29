package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_CompositeUndoableCommand;
import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;

public class RevertAllOp {

	public boolean execute(Geometry fence, WorkbenchContext context)
			throws Exception {
		// Keep this operation undoable, but truncate the undo history
		// to work around this problem:
		//		 
		// * RevertAll assumes that segment references at a particular point in
		// the undo history are invariant.
		// * MoveSplitNode does not - it always creates new segments on the
		// redo.
		//		 
		// Example:
		//		 
		// MoveSplitNode removes 100,101 adds 200,201
		// RevertAll removes 200,201 adds 300
		// Undo RevertAll removes 300 adds 200,201
		// Undo MoveSplitNode removes 200,201 adds 100,101
		// Redo MoveSplitNode removes 100,101 adds *** 400,401 ***
		// Redo RevertAll removes 200,201...FAILS
		//		 
		// => assertion failure => duplicate segments.
		//		 
		// So I'm going to disable Undo on Revert All.
		// [Jon Aquino 2004-12-07]
		context.getLayerManager().getUndoableEditReceiver().getUndoManager()
				.discardAllEdits();
		Collection roadFeaturesInFence = roadFeaturesInFence(fence, context
				.getLayerViewPanel(), ToolboxModel.instance(context));
		if (roadFeaturesInFence.isEmpty()) {
			context.getWorkbench().getFrame().warnUser(
					ErrorMessages.revertAllOp_noKnownSegmentsOrSplitNodes);
			return false;
		}
		UndoableCommand command = createUndoableCommand(roadFeaturesInFence,
				context.getLayerViewPanel(), fence, ToolboxModel
						.instance(context));
		AbstractPlugIn.execute(command, context.getLayerViewPanel());
		warnUserIfSplitSegmentsDiscontiguous(
				parentSegments(roadFeaturesInFence), ToolboxModel.instance(
						context).getSession(), context.getWorkbench()
						.getFrame());
		return true;
	}

	private void warnUserIfSplitSegmentsDiscontiguous(Set parentSegments,
			ConflationSession session, WorkbenchFrame workbenchFrame) {
		CollectionMap parentSegmentToDiscontiguousSplitSegmentsMap = parentSegmentToDiscontiguousSplitSegmentsMap(
				parentSegments, session.getSourceNetwork(0));
		parentSegmentToDiscontiguousSplitSegmentsMap
				.putAll(parentSegmentToDiscontiguousSplitSegmentsMap(
						parentSegments, session.getSourceNetwork(1)));
		if (parentSegmentToDiscontiguousSplitSegmentsMap.isEmpty()) {
			return;
		}
		logDiscontiguousSplitSegments(
				parentSegmentToDiscontiguousSplitSegmentsMap, workbenchFrame);
		FUTURE_PreventableConfirmationDialog
				.show(
						workbenchFrame,
						"JUMP",
						ErrorMessages.revertAllOp_segmentsDiscontiguous_statusLineWarning,
						ErrorMessages.revertAllOp_segmentsDiscontiguous_dialogText,
						"OK", null, getClass().getName()
								+ " - DO NOT SHOW AGAIN");
	}

	private void logDiscontiguousSplitSegments(
			CollectionMap parentSegmentToDiscontiguousSplitSegmentsMap,
			WorkbenchFrame workbenchFrame) {
		StringBuffer buffer = new StringBuffer(
				"Discontiguous split segments:\n");
		for (Iterator i = parentSegmentToDiscontiguousSplitSegmentsMap.keySet()
				.iterator(); i.hasNext();) {
			SourceRoadSegment parentSegment = (SourceRoadSegment) i.next();
			buffer.append("    Parent: " + string(parentSegment) + "\n");
			for (Iterator j = parentSegmentToDiscontiguousSplitSegmentsMap
					.getItems(parentSegment).iterator(); j.hasNext();) {
				SplitRoadSegment childSegment = (SplitRoadSegment) j.next();
				buffer.append("        Child: " + string(childSegment)
						+ " Start: " + string(childSegment.getSiblingAtStart())
						+ " End: " + string(childSegment.getSiblingAtEnd())
						+ "\n");
			}
		}
		workbenchFrame.log(buffer.toString());
	}

	private String string(SourceRoadSegment segment) {
		return segment == null ? "null" : Integer.toHexString(segment
				.hashCode());
	}

	private CollectionMap parentSegmentToDiscontiguousSplitSegmentsMap(
			Set parentSegments, RoadNetwork network) {
		CollectionMap parentSegmentToSplitSegmentsMap = parentSegmentToSplitSegmentsMap(
				parentSegments, network);
		for (Iterator i = parentSegmentToSplitSegmentsMap.keySet().iterator(); i
				.hasNext();) {
			SourceRoadSegment parentSegment = (SourceRoadSegment) i.next();
			if (contiguous(parentSegmentToSplitSegmentsMap
					.getItems(parentSegment))) {
				i.remove();
			}
		}
		return parentSegmentToSplitSegmentsMap;
	}

	private boolean contiguous(Collection splitSegments) {
		Collection leftovers = new HashSet(splitSegments);
		for (Iterator i = splitSegments.iterator(); i.hasNext();) {
			SplitRoadSegment segment = (SplitRoadSegment) i.next();
			if (segment.getSiblingAtStart() == null) {
				leftovers.remove(segment);
			} else {
				if (!splitSegments.contains(segment.getSiblingAtStart())) {
					return false;
				}
				leftovers.remove(segment.getSiblingAtStart());
			}
			if (segment.getSiblingAtEnd() == null) {
				leftovers.remove(segment);
			} else {
				if (!splitSegments.contains(segment.getSiblingAtEnd())) {
					return false;
				}
				leftovers.remove(segment.getSiblingAtEnd());
			}
		}
		return leftovers.isEmpty();
	}

	public static CollectionMap parentSegmentToSplitSegmentsMap(Set parentSegments,
			RoadNetwork network) {
		CollectionMap parentSegmentToSplitSegmentsMap = new CollectionMap();
		for (Iterator i = network.getGraph().getEdges().iterator(); i.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (!(segment instanceof SplitRoadSegment)) {
				continue;
			}
			if (!parentSegments.contains(((SplitRoadSegment) segment)
					.getParent())) {
				continue;
			}
			parentSegmentToSplitSegmentsMap.addItem(
					((SplitRoadSegment) segment).getParent(), segment);
		}
		return parentSegmentToSplitSegmentsMap;
	}

	private Set parentSegments(Collection features) {
		Set parentSegments = new HashSet();
		for (Iterator i = features.iterator(); i.hasNext();) {
			SourceFeature feature = (SourceFeature) i.next();
			if (!(feature.getRoadSegment() instanceof SplitRoadSegment)) {
				continue;
			}
			parentSegments.add(((SplitRoadSegment) feature.getRoadSegment())
					.getParent());
		}
		return parentSegments;
	}

	private Collection roadFeaturesInFence(Geometry fence,
			LayerViewPanel panel, ToolboxModel toolboxModel) {
		Map layerToFeaturesInFenceMap = panel
				.visibleLayerToFeaturesInFenceMap(fence);
		Collection featuresInFence = FUTURE_CollectionUtil.concatenate(
				(Collection) LangUtil.ifNull(layerToFeaturesInFenceMap
						.get(toolboxModel.getSourceLayer(0)), new ArrayList()),
				(Collection) LangUtil.ifNull(layerToFeaturesInFenceMap
						.get(toolboxModel.getSourceLayer(1)), new ArrayList()));
		return featuresInFence;
	}

	private static final String NAME = "Revert All";

	private UndoableCommand createUndoableCommand(
			Collection roadFeaturesInFence, LayerViewPanel panel,
			Geometry fence, final ToolboxModel toolboxModel) throws Exception {
		final List commands = new ArrayList();
		for (Iterator i = roadFeaturesInFence.iterator(); i.hasNext();) {
			SourceRoadSegment segment = ((SourceFeature) i.next())
					.getRoadSegment();
			if (RevertOp.revertable(segment)) {
				commands.add(RevertOp.createUndoableCommand(segment, NAME,
						panel, toolboxModel, true));
			}
		}
		// Revert all features before deleting split nodes, which
		// will delete features. Otherwise we'll get exceptions. [Jon Aquino
		// 2004-08-06]
		Set segmentsSplitInFence = new HashSet();
		for (Iterator i = splitSegments(roadFeaturesInFence).iterator(); i
				.hasNext();) {
			SplitRoadSegment segment = ((SplitRoadSegment) i.next());
			// Include the split segment even if its split nodes are not in the
			// fence. Otherwise we cannot remove duplicate split segments
			// without split nodes i.e. identical to the parent. [Jon Aquino
			// 2004-12-07]
			segmentsSplitInFence.add(segment);
			// Explicitly add split segment at the start and end. Normally this
			// would not be necessary, but the "discontiguous split segments"
			// bug involves split segments pointing to siblings no longer in
			// the network. These "stale" siblings would otherwise not be
			// included in roadFeaturesInFence. [Jon Aquino 2004-12-03]
			// But only add them if they are "stale" i.e. not in network.
			// Otherwise we might add a sibling that is not in the fence
			// and hence possibly not Unknown, leading to an assertion failure
			// because DeleteSplitNodesFromUnknownSegmentsOp requires Unknown
			// segments. [Jon Aquino 2004-12-07]
			if (segment.isSplitAtStart()
					&& fence
							.contains(segment.getApparentLine().getStartPoint())
					&& !segment.getSiblingAtStart().isInNetwork()) {
				segmentsSplitInFence.add(segment.getSiblingAtStart());
			}
			if (segment.isSplitAtEnd()
					&& fence.contains(segment.getApparentLine().getEndPoint())
					&& !segment.getSiblingAtEnd().isInNetwork()) {
				segmentsSplitInFence.add(segment.getSiblingAtEnd());
			}
		}
		commands.add(new DeleteSplitNodesFromUnknownSegmentsOp()
				.createUndoableCommand(segmentsSplitInFence, toolboxModel,
						panel.getContext(), NAME));
		final List commandsExecuted = new ArrayList();
		return new UndoableCommand(NAME) {
			public void execute() {
				commandsExecuted.clear();
				commandsExecuted.addAll(process("Reverting . . .", commands,
						new Block() {
							public Object yield(Object command) {
								((UndoableCommand) command).execute();
								return null;
							}
						}, true, toolboxModel.getContext()));
			}

			public void unexecute() {
				process("Unreverting . . .", CollectionUtil
						.reverse(new ArrayList(commandsExecuted)), new Block() {
					public Object yield(Object command) {
						((UndoableCommand) command).unexecute();
						return null;
					}
				}, false, toolboxModel.getContext());
			}
		};
	}

	private Collection splitSegments(Collection features) {
		return CollectionUtil.select(CollectionUtil.collect(features,
				new Block() {
					public Object yield(Object arg) {
						return ((SourceFeature) arg).getRoadSegment();
					}
				}), new Block() {
			public Object yield(Object arg) {
				return Boolean.valueOf(arg instanceof SplitRoadSegment);
			}
		});
	}

	private List process(final String description, final List commands,
			final Block commandProcessor, final boolean cancelVisible,
			WorkbenchContext context) {
		final List commandsProcessed = new ArrayList();
		new TaskMonitorManager().execute(new ThreadedBasePlugIn() {
			public String getName() {
				return NAME;
			}

			public void run(TaskMonitor monitor, PlugInContext context)
					throws Exception {
				if (cancelVisible) {
					monitor.allowCancellationRequests();
				}
				monitor.report(description);
				for (int i = 0; i < commands.size(); i++) {
					UndoableCommand command = (UndoableCommand) commands.get(i);
					if (monitor.isCancelRequested()) {
						break;
					}
					commandProcessor.yield(command);
					commandsProcessed.add(command);
					//commands.size() is no longer accurate. It is number of
					// segments + 1. [Jon Aquino 2004-12-06]
					monitor.report(i + 1, commands.size(),
							"segments and split nodes");
				}
			}
		}, context.createPlugInContext());
		return commandsProcessed;
	}
}