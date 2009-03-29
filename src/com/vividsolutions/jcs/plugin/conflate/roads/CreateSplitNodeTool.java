package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateSplitNodeOp.ShortSegmentException;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
public class CreateSplitNodeTool extends SpecifyClosestRoadFeatureTool {
	public CreateSplitNodeTool(boolean layer0, boolean layer1,
			String cursorImage, String buttonImage, Color color,
			WorkbenchContext context) {
		super(layer0, layer1, cursorImage, buttonImage, color, context,
				GestureMode.POINT);
	}
	protected void gestureFinished(SourceFeature feature, Layer layer)
			throws Exception {
		try {
			execute(createUndoableCommand(feature));
		} catch (CreateSplitNodeOp.ShortSegmentException e) {
			//Eat it. The thrower has already notified the user. [Jon Aquino]
		}
	}
	protected static boolean closestPointIsNode(Point p,
			SourceRoadSegment roadSegment) {
		Assert
				.isTrue(
						!roadSegment.isAdjusted(),
						"Not-adjusted assertion allows us to use #getLine rather than #getApparentLine [Jon Aquino 12/23/2003]");
		Coordinate closestPoint = new DistanceOp(p, roadSegment.getLine())
				.closestPoints()[1];
		if (closestPoint.equals(roadSegment.getStartNode().getCoordinate())) {
			return true;
		}
		if (closestPoint.equals(roadSegment.getEndNode().getCoordinate())) {
			return true;
		}
		return false;
	}
	protected UndoableCommand createUndoableCommand(SourceFeature feature) throws NoninvertibleTransformException,
			ShortSegmentException {
		return CreateSplitNodeOp.createUndoableCommand(feature,
				snapToExistingVertex(getModelDestination(),
						(LineString) feature.getGeometry(),
						getBoxInModelCoordinates()), false, true, 
				true, toolboxModel(), getPanel().getContext());
	}
	protected boolean includeInProximitySearch(SourceFeature feature,
			Point clickPoint) {
		if (feature.getRoadSegment().isAdjusted()) {
			setNoRoadSegmentsWarning(ErrorMessages.createSplitNodeTool_adjusted);
			return false;
		}
		return !closestPointIsNode(clickPoint, feature.getRoadSegment());
	}
	public static Coordinate snapToExistingVertex(Coordinate target,
			LineString lineString, Envelope vertexSearchEnvelope)
			throws NoninvertibleTransformException {
        //Even though we do a kind of snapping in LineStringSplitter, we still
        //need this method because LineStringSplitter's does only small snaps
        //(less than the minimum line segment length). This method can do
        //large snaps -- the kind that happen when you're zoomed way out.
        //[Jon Aquino 2004-03-17]
		Coordinate closestVertex = null;
		for (int i = 0; i < lineString.getNumPoints(); i++) {
			//Skip endpoints, but can't just check index because there may be
			//non-endpoints that are equal to the endpoints (rare case) [Jon
			// Aquino 11/25/2003]
			if (lineString.getCoordinateN(i) == lineString.getCoordinateN(0)) {
				continue;
			}
			if (lineString.getCoordinateN(i) == lineString
					.getCoordinateN(lineString.getNumPoints() - 1)) {
				continue;
			}
			if (!vertexSearchEnvelope.contains(lineString.getCoordinateN(i))) {
				continue;
			}
			if (closestVertex == null
					|| lineString.getCoordinateN(i).distance(target) < closestVertex
							.distance(target)) {
				closestVertex = lineString.getCoordinateN(i);
			}
		}
		return (Coordinate) LangUtil.ifNull(closestVertex, target);
	}
	private GeometryFactory factory = new GeometryFactory();
}