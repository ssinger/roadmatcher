package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.Serializable;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.conflate.roads.SimpleAdjustmentMethod.Terminal;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class AutoAdjustOptions implements Serializable {
	public static final int DEFAULT_SEGMENT_ADJUSTMENT_LENGTH = 20;

	private double minimumIncidenceAngle = 20;

	private double maximumSegmentAngleDelta = 20;

	private double maximumAdjustmentSize = 1000;

	private String datasetName;

	private static final String INSTANCE_KEY = AutoAdjustOptions.class
			.getName()
			+ " - INSTANCE";

	private transient SimpleAdjustmentMethod refinedMethod;

	private Class methodClass = WarpAdjustmentMethod.class;

	private double segmentAdjustmentLength = DEFAULT_SEGMENT_ADJUSTMENT_LENGTH;

	private boolean shiftingSegmentsWithOneConnectedEnd = false;

	public double getMaximumSegmentAngleDelta() {
		return maximumSegmentAngleDelta;
	}

	public void setMaximumSegmentAngleDelta(double maximumSegmentAngleDelta) {
		this.maximumSegmentAngleDelta = maximumSegmentAngleDelta;
	}

	public double getMinimumIncidenceAngle() {
		return minimumIncidenceAngle;
	}

	public void setMinimumIncidenceAngle(double minimumIncidenceAngle) {
		this.minimumIncidenceAngle = minimumIncidenceAngle;
	}

	public static AutoAdjustOptions get(ConflationSession session) {
		if (session.getBlackboard().get(INSTANCE_KEY) == null) {
			set(new AutoAdjustOptions(), session);
		}
		return (AutoAdjustOptions) session.getBlackboard().get(INSTANCE_KEY);
	}

	public static void set(AutoAdjustOptions autoAdjustOptions,
			ConflationSession session) {
		session.getBlackboard().put(INSTANCE_KEY, autoAdjustOptions);
	}

	public double getMaximumAdjustmentSize() {
		return maximumAdjustmentSize;
	}

	public void setMaximumAdjustmentSize(double maximumAdjustmentSize) {
		this.maximumAdjustmentSize = maximumAdjustmentSize;
	}

	public String getDatasetName() {
		Assert.isTrue(datasetName != null);
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public Class getMethodClass() {
		return methodClass;
	}

	public void setMethodClass(Class methodClass) {
		refinedMethod = null;
		this.methodClass = methodClass;
	}

	public boolean isShiftingSegmentsWithOneConnectedEnd() {
		return shiftingSegmentsWithOneConnectedEnd;
	}

	public void setShiftingSegmentsWithOneConnectedEnd(
			boolean shiftingSegmentsWithUnconnectedEnd) {
		refinedMethod = null;
		this.shiftingSegmentsWithOneConnectedEnd = shiftingSegmentsWithUnconnectedEnd;
	}

	public SimpleAdjustmentMethod getRefinedMethod() {
		if (refinedMethod == null) {
			refinedMethod = new SimpleAdjustmentMethod() {
				private ShiftAdjustmentMethod shiftAdjustmentMethod = new ShiftAdjustmentMethod();

				public LineString adjust(LineString line,
						SourceRoadSegment segment, Terminal terminal,
						Coordinate newTerminalLocation, LayerViewPanel panel) {
					try {
						// Only do the shift for unadjusted segments:
						// 1. It's then easy to check the degree of the other
						// end
						// 2. I sense that it's the right thing to do
						// [Jon Aquino 2004-08-10]
						return (isShiftingSegmentsWithOneConnectedEnd()
								&& segment.getApparentLine().getLength() < 2 * AutoAdjustOptions
										.get(segment.getNetwork().getSession())
										.getSegmentAdjustmentLength()
								&& !segment.isAdjusted()
								&& !segment.isStartNodeConstrained()
								&& !segment.isEndNodeConstrained()
								&& unconnected(segment, terminal.other()) ? shiftAdjustmentMethod
								: (SimpleAdjustmentMethod) methodClass
										.newInstance()).adjust(line, segment,
								terminal, newTerminalLocation, panel);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				/**
				 * Works regardless of consistency rule.
				 */
				private boolean unconnected(SourceRoadSegment segment,
						Terminal terminal) {
					Assert.isTrue(node(segment, terminal).getDegree() > 0);
					return node(segment, terminal).getDegree() == 1;
				}

				private RoadNode node(SourceRoadSegment segment,
						Terminal terminal) {
					return terminal == Terminal.START ? segment.getStartNode()
							: segment.getEndNode();
				}
			};
		}
		return refinedMethod;
	}

	public double getSegmentAdjustmentLength() {
		return segmentAdjustmentLength;
	}

	public void setSegmentAdjustmentLength(double segmentAdjustmentLength) {
		this.segmentAdjustmentLength = segmentAdjustmentLength;
	}
}