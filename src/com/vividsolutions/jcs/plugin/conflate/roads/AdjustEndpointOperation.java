package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.UndoableCommand;

public class AdjustEndpointOperation {
	public static UndoableCommand createUndoableCommand(String name,
			final SourceRoadSegment roadSegment, final LineString oldLine,
			final LineString newLine, final LayerManager layerManager,
			final WorkbenchContext context) {
		return new UndoableCommand(name) {
			private void changeLine(LineString line) {
				roadSegment.setApparentLine(line);
				new Transaction(ToolboxModel.instance(layerManager, context),
						context.getErrorHandler(), new DummyTaskMonitor())
						.markAsModified(roadSegment).execute();
			}

			public void execute() {
				changeLine(newLine);
			}

			public void unexecute() {
				changeLine(oldLine);
			}
		};
	}

	public static boolean checkLineSegmentLength(LineString newLine,
			String doNotShowAgainID, WorkbenchContext context) {
		return checkLineSegmentLength("Adjust anyway", newLine,
				doNotShowAgainID, context);
	}

	public static boolean checkLineSegmentLength(String proceedButtonText,
			LineString newLine, String doNotShowAgainID,
			WorkbenchContext context) {
		if (newLine.getLength() == 0) {
			context.getWorkbench().getFrame().warnUser(
					ErrorMessages.adjustEndpointOperation_zeroLength);
			return false;
		}
		if (shortestLineSegmentLength(newLine, context) < minLineSegmentLength(ToolboxModel
				.instance(context).getSession())) {
			return FUTURE_PreventableConfirmationDialog
					.show(
							context.getWorkbench().getFrame(),
							"JUMP",
							FUTURE_StringUtil
									.substitute(
											ErrorMessages.adjustEndpointOperation_shortLineSegment_statusLineWarning,
											new Object[] {
													FUTURE_StringUtil
															.format(shortestLineSegmentLength(
																	newLine,
																	context)),
													minLineSegmentLength(ToolboxModel
															.instance(context)
															.getSession())
															+ "" }),
							FUTURE_StringUtil
									.substitute(
											ErrorMessages.adjustEndpointOperation_shortLineSegment_dialogText,
											new Object[] {
													FUTURE_StringUtil
															.format(shortestLineSegmentLength(
																	newLine,
																	context)),
													minLineSegmentLength(ToolboxModel
															.instance(context)
															.getSession())
															+ "" }),
							proceedButtonText, "Cancel", doNotShowAgainID);
		}
		return true;
	}

	private static double shortestLineSegmentLength(LineString newLine,
			WorkbenchContext context) {
		double shortestLineSegmentLength = Integer.MAX_VALUE;
		for (int i = 1; i < newLine.getNumPoints(); i++) {
			if (newLine.getCoordinateN(i).distance(
					newLine.getCoordinateN(i - 1)) < shortestLineSegmentLength) {
				shortestLineSegmentLength = newLine.getCoordinateN(i).distance(
						newLine.getCoordinateN(i - 1));
			}
		}
		return shortestLineSegmentLength;
	}

	public static double minLineSegmentLength(ConflationSession session) {
		return session.getMatchOptions().getEdgeMatchOptions()
				.getLineSegmentLengthTolerance();
	}

	public static Coordinate closest(Coordinate a1, Coordinate a2, Coordinate b) {
		return a1 == null ? a2 : a2 == null ? a1 : a1.distance(b) < a2
				.distance(b) ? a1 : a2;
	}

	public static Coordinate closestEndpointWithinTolerance(
			SourceRoadSegment roadSegment, Coordinate c, double tolerance) {
		return nullIfOutsideTolerance(AdjustEndpointOperation.closest(
				roadSegment.getApparentStartCoordinate(), roadSegment
						.getApparentEndCoordinate(), c), c, tolerance);
	}

	private static Coordinate nullIfOutsideTolerance(Coordinate c,
			Coordinate target, double tolerance) {
		return c.distance(target) < tolerance ? c : null;
	}
}