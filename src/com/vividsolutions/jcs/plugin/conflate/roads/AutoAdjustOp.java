package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jcs.jump.FUTURE_DummyLayerViewPanelContext;
import com.vividsolutions.jcs.jump.FUTURE_LineString;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.DebugTimer;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class AutoAdjustOp {
	private static final int MAX_SEGMENT_LENGTH_RATIO = 2;
	private static final double MIN_SEGMENT_LENGTH_RATIO = 0.5;
	private IncidenceAngleChecker incidenceAngleChecker = new IncidenceAngleChecker();

	public int autoAdjust(final String name, Collection roadSegments,
			final TaskMonitor monitor, final WorkbenchContext context,
			AutoAdjuster autoAdjuster) {
		return autoAdjust(roadSegments, monitor, autoAdjuster,
				session(context), new Block() {
					public Object yield(Object adjustment) {
						AbstractPlugIn.execute(AutoConnectEndpointTool
								.createUndoableCommand((Adjustment) adjustment,
										true, name, ToolboxModel
												.instance(context), monitor),
								ToolboxModel.instance(context).getContext());
						return null;
					}
				});
	}

	public int autoAdjust(Collection roadSegments, TaskMonitor monitor,
			AutoAdjuster autoAdjuster, ConflationSession session,
			Block lineSetter) {
		Set adjustedRoadSegments = new HashSet();
		int iterations = 0;
		do {
			iterations++;
			monitor.report("Computing adjustments (iteration " + iterations
					+ ")");
			Adjustment combinedAdjustment = combine(adjustments(roadSegments,
					autoAdjuster, session, monitor));
			//Don't adjust if empty -- adds an unnecessary undo step.
			//[Jon Aquino 2004-05-11]
			if (combinedAdjustment.getRoadSegments().isEmpty()) {
				break;
			}
			adjustedRoadSegments.addAll(combinedAdjustment
					.getAdjustedRoadSegments());
			monitor.report("Performing adjustments (iteration " + iterations
					+ ")");
			lineSetter.yield(combinedAdjustment);
		} while (iterations < 5 && !monitor.isCancelRequested());
		return adjustedRoadSegments.size();
	}

	public boolean checkAutoAdjustNotConstrained(WorkbenchContext context) {
		return checkAutoAdjustNotConstrained(session(context).getSourceNetwork(
				AutoAdjustOptions.get(session(context)).getDatasetName()),
				context);
	}

	private boolean checkAutoAdjustNotConstrained(RoadNetwork network,
			WorkbenchContext context) {
		if (adjustmentsConstrainedFor(network)) {
			JOptionPane
					.showMessageDialog(
							context.getWorkbench().getFrame(),
							FUTURE_StringUtil
									.substitute(
											ErrorMessages.autoAdjustOp_adjustmentConstraintsViolation,
											new Object[] { network.getName() }),
							"Adjustment Constraints Violation",
							JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	private boolean adjustmentsConstrainedFor(RoadNetwork network) {
		return !network.isEditable()
				|| network.getSession().isWarningAboutAdjustments(
						network.getID());
	}

	private ConflationSession session(WorkbenchContext context) {
		return ToolboxModel.instance(context).getSession();
	}

	private Adjustment combine(Collection adjustments) {
		int inconsistentNodesBeingAdjusted = 0;
		List roadSegments = new ArrayList();
		List newApparentLines = new ArrayList();
		for (Iterator i = adjustments.iterator(); i.hasNext();) {
			Adjustment adjustment = (Adjustment) i.next();
			inconsistentNodesBeingAdjusted += adjustment
					.getInconsistentNodesBeingAutoAdjusted();
			roadSegments.addAll(adjustment.getRoadSegments());
			newApparentLines.addAll(adjustment.getNewApparentLines());
		}
		return new Adjustment(null, roadSegments, newApparentLines,
				inconsistentNodesBeingAdjusted);
	}

	private Collection adjustments(Collection roadSegments,
			AutoAdjuster autoAdjuster, ConflationSession session,
			TaskMonitor monitor) {
		AutoAdjustOptions options = AutoAdjustOptions.get(session);
		Collection adjustments = new ArrayList();
		int j = 0;
		for (Iterator i = roadSegments.iterator(); i.hasNext()
				&& !monitor.isCancelRequested();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			monitor.report(++j, roadSegments.size(), "road segments");
			Adjustment adjustment;
			try {
				adjustment = adjustment(roadSegment, autoAdjuster, session);
			} catch (Exception e) {
				//e.g. ZeroLengthException, "Matrix is singular"
				//RuntimeException thrown by LUDecomposition#solve, ...
				//[Jon Aquino 2004-05-07]
				continue;
			}
			if (adjustment == null) {
				continue;
			}
			if (anyRoadSegmentsAlreadyMarkedForAdjusting(adjustment,
					adjustments)) {
				continue;
			}
			if (adjustment.getInconsistentNodesBeingAutoAdjusted() != 1) {
				continue;
			}
			if (containsRoadSegmentsWithNetworkID(adjustment
					.getAdjustedRoadSegments(), 1 - session.getSourceNetwork(
					options.getDatasetName()).getID())) {
				continue;
			}
			if (containsSegmentAngleDeltaGreaterThan(options
					.getMaximumSegmentAngleDelta(), adjustment)) {
				continue;
			}
			// #containsAdjustmentSizeGreaterThan and
			// #containsExtremeSegmentLengthRatio
			// are two different ways to prevent very large AutoAdjustments.
			// [Jon Aquino 2004-08-10]
			if (containsAdjustmentSizeGreaterThan(options
					.getMaximumAdjustmentSize(), adjustment)) {
				continue;
			}
			if (containsExtremeSegmentLengthRatio(adjustment)) {
				continue;
			}
			if (incidenceAngleChecker.createsIncidenceAngleLessThan(options
					.getMinimumIncidenceAngle(), adjustment, session)) {
				continue;
			}
			if (!constraintChecker.proceedWith(adjustment, session)) {
				continue;
			}
			adjustments.add(adjustment);
		}
		return adjustments;
	}

	private ConstraintChecker constraintChecker = new ConstraintChecker(
			new FUTURE_DummyLayerViewPanelContext());

	private boolean containsSegmentAngleDeltaGreaterThan(
			double maximumSegmentAngleDelta, Adjustment adjustment) {
		for (int i = 0; i < adjustment.getRoadSegments().size(); i++) {
			// Check the start and end angles, rather than the overall angle,
			// as the warp may be partial [Jon Aquino 2004-08-10]
			if (segmentAngleDeltaGreaterThan(maximumSegmentAngleDelta,
					(LineString) adjustment.getOldApparentLines().get(i),
					(LineString) adjustment.getNewApparentLines().get(i))) {
				return true;
			}
		}
		return false;
	}

	protected boolean segmentAngleDeltaGreaterThan(
			double maximumSegmentAngleDelta, LineString oldLine,
			LineString newLine) {
		return Angle.toDegrees(Angle.diff(angle(oldLine, true), angle(newLine,
				true))) > maximumSegmentAngleDelta
				|| Angle.toDegrees(Angle.diff(angle(oldLine, false), angle(
						newLine, false))) > maximumSegmentAngleDelta;
	}

	private boolean containsExtremeSegmentLengthRatio(Adjustment adjustment) {
		for (int i = 0; i < adjustment.getRoadSegments().size(); i++) {
			if (segmentLengthRatioExtreme((LineString) adjustment
					.getOldApparentLines().get(i), (LineString) adjustment
					.getNewApparentLines().get(i))) {
				return true;
			}
		}
		return false;
	}

	protected boolean segmentLengthRatioExtreme(LineString oldLine,
			LineString newLine) {
		return lengthRatio(newLine, oldLine) < MIN_SEGMENT_LENGTH_RATIO
				|| MAX_SEGMENT_LENGTH_RATIO < lengthRatio(newLine, oldLine);
	}

	private double lengthRatio(LineString newLine, LineString oldLine) {
		return newLine.getLength() / oldLine.getLength();
	}

	protected double angle(LineString line, boolean start) {
		return start ? startAngle(line) : endAngle(line);
	}

	protected double startAngle(LineString line) {
		return Angle.angle(line.getCoordinateN(0), line.getCoordinateN(1));
	}

	protected double endAngle(LineString line) {
		return Angle.angle(line.getCoordinateN(line.getNumPoints() - 1), line
				.getCoordinateN(line.getNumPoints() - 2));
	}

	private boolean containsRoadSegmentsWithNetworkID(Collection roadSegments,
			int networkID) {
		for (Iterator i = roadSegments.iterator(); i.hasNext();) {
			SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
			if (roadSegment.getNetworkID() == networkID) {
				return true;
			}
		}
		return false;
	}

	private Adjustment adjustment(SourceRoadSegment roadSegment,
			AutoAdjuster autoAdjuster, ConflationSession session)
			throws ZeroLengthException {
		Adjustment adjustment = adjustment(roadSegment, roadSegment
				.getApparentStartCoordinate(), autoAdjuster, session);
		return adjustment != null ? adjustment : adjustment(roadSegment,
				roadSegment.getApparentEndCoordinate(), autoAdjuster, session);
	}

	private Adjustment adjustment(SourceRoadSegment roadSegment,
			Coordinate apparentEndpoint, AutoAdjuster autoAdjuster,
			ConflationSession session) throws ZeroLengthException {
		if (autoAdjuster.autoAdjustable(roadSegment, apparentEndpoint, session) != Boolean.TRUE) {
			return null;
		}
		return autoAdjuster.adjustment(roadSegment, apparentEndpoint, session,
				AutoAdjustOptions.get(session).getRefinedMethod(), null);
	}

	private boolean anyRoadSegmentsAlreadyMarkedForAdjusting(
			Adjustment newAdjustment, Collection oldAdjustments) {
		for (Iterator i = oldAdjustments.iterator(); i.hasNext();) {
			Adjustment oldAdjustment = (Adjustment) i.next();
			if (FUTURE_CollectionUtil.containsAny(oldAdjustment
					.getRoadSegments(), newAdjustment.getRoadSegments())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsAdjustmentSizeGreaterThan(
			double maximumAdjustmentSize, Adjustment adjustment) {
		for (int i = 0; i < adjustment.getRoadSegments().size(); i++) {
			if (SourceRoadSegment.adjustmentSize((LineString) adjustment
					.getOldApparentLines().get(i), (LineString) adjustment
					.getNewApparentLines().get(i)) > maximumAdjustmentSize) {
				return true;
			}
		}
		return false;
	}
}