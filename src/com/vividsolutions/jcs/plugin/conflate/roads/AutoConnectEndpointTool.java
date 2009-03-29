package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;

public class AutoConnectEndpointTool extends SpecifyClosestRoadFeatureTool {

	public AutoConnectEndpointTool(WorkbenchContext context) {
		super(true, true, null, "auto-adjust-endpoint-tool-button.png",
				Color.black, context, GestureMode.POINT);
	}

	protected boolean includeInProximitySearch(SourceFeature feature,
			Point clickPoint) {
		try {
			if (!getBoxInModelCoordinates().contains(
					feature.getRoadSegment().apparentEndpointClosestTo(
							getModelDestination()))) {
				return false;
			}
		} catch (NoninvertibleTransformException e) {
			return false;
		}
		Object result = autoAdjuster().autoAdjustable(
				feature.getRoadSegment(),
				feature.getRoadSegment().apparentEndpointClosestTo(
						getModelDestination()), toolboxModel().getSession());
		if (result instanceof String) {
			setNoRoadSegmentsWarning((String) result);
			return false;
		}
		return ((Boolean) result).booleanValue();
	}

	private AutoAdjuster autoAdjuster() {
		return toolboxModel().getConsistencyConfiguration().getAutoAdjuster();
	}

	protected void gestureFinished(SourceFeature feature, Layer layer) {
		//Always use WarpLocallyAdjustmentMethod rather than the current
		//adjustment method, because the current adjustment method may
		//be Shift Entire Segment, which we obviously do not want
		//[Jon Aquino 2004-03-22]
		final Adjustment adjustment;
		try {
			adjustment = autoAdjuster().adjustment(
					feature.getRoadSegment(),
					feature.getRoadSegment().apparentEndpointClosestTo(
							getModelDestination()),
					toolboxModel().getSession(),
					AutoAdjustOptions.get(toolboxModel().getSession()).getRefinedMethod(),
					getPanel());
		} catch (ZeroLengthException e) {
			getContext().getWorkbench().getFrame().warnUser(
					WorkbenchFrame.toMessage(e));
			getContext().getWorkbench().getFrame()
					.log(StringUtil.stackTrace(e));
			return;
		}
		if (!new ConstraintChecker(getContext().getWorkbench().getFrame())
				.proceedWith(adjustment, toolboxModel().getSession())) {
			return;
		}
		if (!checkLineSegmentLength(adjustment.getNewApparentLines(),
				getClass().getName() + " - DO NOT SHOW AGAIN", getWorkbench()
						.getContext())) {
			return;
		}
		ToolboxModel toolboxModel = ToolboxModel.instance(getWorkbench()
				.getContext().getLayerManager(), getWorkbench().getContext());
		AbstractPlugIn.execute(createUndoableCommand(adjustment, false,
				getName(), toolboxModel, new DummyTaskMonitor()), toolboxModel
				.getContext());
	}

	public static UndoableCommand createUndoableCommand(
			final Adjustment adjustment, final boolean automated, String name,
			final ToolboxModel toolboxModel, final TaskMonitor monitor) {
		return new UndoableCommand(name) {
			public void execute() {
				setLines(adjustment.getNewApparentLines());
			}

			public void unexecute() {
				setLines(adjustment.getOldApparentLines());
			}

			private void setLines(final List lines) {
				if (automated) {
					toolboxModel.getSession().doAutomatedProcess(new Block() {
						public Object yield() {
							setLinesProper(lines);
							return null;
						}
					});
				} else {
					setLinesProper(lines);
				}
			}

			private void setLinesProper(List lines) {
				Transaction transaction = new Transaction(toolboxModel,
						toolboxModel.getContext().getErrorHandler(), monitor);
				for (int i = 0; i < lines.size(); i++) {
					((SourceRoadSegment) adjustment.getRoadSegments().get(i))
							.setApparentLine(((LineString) lines.get(i)));
					transaction.markAsModified(((SourceRoadSegment) adjustment
							.getRoadSegments().get(i)));
				}
				transaction.execute();
			}
		};
	}

	private boolean checkLineSegmentLength(List newLines,
			String doNotShowAgainID, WorkbenchContext context) {
		for (Iterator i = newLines.iterator(); i.hasNext();) {
			LineString newLine = (LineString) i.next();
			if (!AdjustEndpointOperation.checkLineSegmentLength(newLine,
					doNotShowAgainID, context)) {
				return false;
			}
		}
		return true;
	}

	protected String getDefaultNoRoadSegmentsWarning() {
		return ErrorMessages.autoConnectEndpointTool_noRoadSegments;
	}
}