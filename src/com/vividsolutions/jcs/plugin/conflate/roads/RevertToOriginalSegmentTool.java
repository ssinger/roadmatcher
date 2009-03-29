package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SplitRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public class RevertToOriginalSegmentTool extends SpecifyRoadFeaturesTool {

	public RevertToOriginalSegmentTool(WorkbenchContext context) {
		super(true, true, null, "Hammer.gif", Color.magenta, context,
				GestureMode.LINE);
	}

	protected void gestureFinished() throws Exception {
		getPanel().getLayerManager().getUndoableEditReceiver()
				.reportIrreversibleChange();
		if (!checkConflationSessionStarted(getContext())) {
			return;
		}
		if (unknown(segments()).isEmpty()) {
			warnUser(
					ErrorMessages.revertToOriginalSegmentsTool_noUnknownSegments,
					getContext());
			return;
		}
		if (splitSegments().isEmpty()) {
			return;
		}
		Collection nonUnknownSiblings = nonUnknown(children(parents(splitSegments())));
		if (!nonUnknownSiblings.isEmpty()) {
			warnUser(
					ErrorMessages.revertToOriginalSegmentsTool_splitSegmentsNotUnknown,
					getContext());
			getContext().getWorkbench().getFrame().log(
					"FIDS = " + fids(nonUnknownSiblings));
			return;
		}
		unsplit(parents(splitSegments()), ToolboxModel.instance(getContext()));
	}

	private void unsplit(Set parents, ToolboxModel toolboxModel) {
		for (Iterator i = parents.iterator(); i.hasNext();) {
			SourceRoadSegment parent = (SourceRoadSegment) i.next();
			new RevertToOriginalSegmentOp().unsplit(parent, toolboxModel);
		}
	}

	private String fids(Collection segments) {
		return StringUtil.toCommaDelimitedString(CollectionUtil.collect(
				segments, new Block() {
					public Object yield(Object segment) {
						return ((SourceRoadSegment) segment).getFeature()
								.getID()
								+ "";
					}
				}));
	}

	private Collection nonUnknown(Collection segments) {
		return CollectionUtil.select(segments, new Block() {
			public Object yield(Object segment) {
				return Boolean
						.valueOf(((SourceRoadSegment) segment).getState() != SourceState.UNKNOWN);
			}
		});
	}

	private Collection unknown(Collection segments) {
		return CollectionUtil.select(segments, new Block() {
			public Object yield(Object segment) {
				return Boolean
						.valueOf(((SourceRoadSegment) segment).getState() == SourceState.UNKNOWN);
			}
		});
	}

	private Collection children(Set parents) {
		ArrayList children = new ArrayList();
		for (Iterator i = parents.iterator(); i.hasNext();) {
			SourceRoadSegment parent = (SourceRoadSegment) i.next();
			children.addAll(RevertToOriginalSegmentOp.children(parent));
		}
		return children;
	}

	private Set parents(Collection splitSegments) {
		HashSet parents = new HashSet();
		for (Iterator i = splitSegments.iterator(); i.hasNext();) {
			SplitRoadSegment segment = (SplitRoadSegment) i.next();
			parents.add(segment.getParent());
		}
		return parents;
	}

	private Collection splitSegments() throws NoninvertibleTransformException {
		return CollectionUtil.select(segments(), new Block() {
			public Object yield(Object segment) {
				return Boolean.valueOf(segment instanceof SplitRoadSegment);
			}
		});
	}

	private Collection segments() throws NoninvertibleTransformException {
		return CollectionUtil.collect(CollectionUtil
				.concatenate(layerToSpecifiedFeaturesMap().values()),
				new Block() {
					public Object yield(Object feature) {
						return ((SourceFeature) feature).getRoadSegment();
					}
				});
	}
}