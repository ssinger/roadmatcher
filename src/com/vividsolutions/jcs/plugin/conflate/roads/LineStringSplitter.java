package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.vividsolutions.jcs.algorithm.linearreference.LengthSubstring;
import com.vividsolutions.jcs.algorithm.linearreference.LengthToPoint;
import com.vividsolutions.jcs.jump.FUTURE_CollectionUtil;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.CoordUtil;
public class LineStringSplitter {
	/**
	 * Splits the lineString at the point closest to c.
	 * 
	 * @param lineString
	 *                   the closest point to the target must not be an endpoint
	 * @param moveSplitToTarget
	 *                   true to move the split point to the target; false to leave
	 *                   the split point at the closest point on the line to the
	 *                   target
	 * @param minLineSegmentLength
	 *                   we will attempt to move the split to obey this constraint
	 */
	public LineString[] split(LineString lineString, Coordinate target,
			boolean moveSplitToTarget, double minLineSegmentLength) {
		Assert.isTrue(!(moveSplitToTarget && minLineSegmentLength > 0));
		Assert.isTrue(!closestPointIsTerminal(lineString, target));
		LineString[] newLineStrings = split(lineString, target,
				minLineSegmentLength);
		if (moveSplitToTarget) {
			newLineStrings[0].getEndPoint().getCoordinate().setCoordinate(
					(Coordinate) target.clone());
			newLineStrings[1].getStartPoint().getCoordinate().setCoordinate(
					(Coordinate) target.clone());
		}
		return newLineStrings;
	}
	public static boolean closestPointIsTerminal(LineString lineString, Coordinate target) {
		return closestPointIsTerminal(lineString, target, lineString
				.getStartPoint()) || closestPointIsTerminal(lineString, target,
				lineString.getEndPoint());
	}
	private static boolean closestPointIsTerminal(LineString lineString,
			Coordinate target, Point terminal) {
		return new DistanceOp(lineString, lineString.getFactory().createPoint(
				target)).closestLocations()[0].getCoordinate().equals(
				terminal.getCoordinate());
	}
	private LineString[] split(LineString lineString, Coordinate target,
			double minLineSegmentLength) {
		LineSegment[] lineSegments = lineSegments(lineString);
		int i = indexOfClosestLineSegment(lineSegments, target);
		LineSegment[] splitLineSegments = split(lineSegments[i], target,
				minLineSegmentLength, i == 0, i == lineSegments.length - 1);
		return new LineString[]{
				lineString((List) FUTURE_CollectionUtil.concatenate(Arrays
						.asList(lineSegments).subList(0, i), Collections
						.singleton(splitLineSegments[0])), lineString
						.getFactory()),
				lineString((List) FUTURE_CollectionUtil.concatenate(Collections
						.singleton(splitLineSegments[1]), Arrays.asList(
						lineSegments).subList(i + 1, lineSegments.length)),
						lineString.getFactory())};
	}
	private LineSegment[] split(LineSegment lineSegment, Coordinate target,
			double minLineSegmentLength, boolean firstLineSegment,
			boolean lastLineSegment) {
		Coordinate closestTerminal = lineSegment.p0.distance(target) < lineSegment.p1
				.distance(target) ? lineSegment.p0 : lineSegment.p1;
		Coordinate furthestTerminal = closestTerminal != lineSegment.p0 ? lineSegment.p0
				: lineSegment.p1;
		Coordinate split = lineSegment.closestPoint(target);
		//closestTerminal will equal split if
		//CreateSplitNodeTool#snapToExistingVertex is used
		//[Jon Aquino 2004-03-17]
		if (!closestTerminal.equals(split)
				&& closestTerminal.distance(split) < minLineSegmentLength) {
			//Fudge to guarantee length > min [Jon Aquino 2004-03-17]
			double fudge = 1.001;
			if (lineSegment.getLength() > 2 * fudge * minLineSegmentLength) {
				split = CoordUtil.add(closestTerminal, CoordUtil.multiply(
						minLineSegmentLength * fudge, CoordUtil.divide(
								CoordUtil.subtract(furthestTerminal,
										closestTerminal), lineSegment
										.getLength())));
			} else if ((closestTerminal == lineSegment.p0 && !firstLineSegment)
					|| (closestTerminal == lineSegment.p1 && !lastLineSegment)) {
				split = closestTerminal;
			} else if ((furthestTerminal == lineSegment.p0 && !firstLineSegment)
					|| (furthestTerminal == lineSegment.p1 && !lastLineSegment)) {
				split = furthestTerminal;
			}
		}
		//Order of coordinates is important [Jon Aquino 2004-03-17]
		return new LineSegment[]{
				new LineSegment(lineSegment.p0, (Coordinate) split.clone()),
				new LineSegment((Coordinate) split.clone(), lineSegment.p1)};
	}
	private int indexOfClosestLineSegment(LineSegment[] lineSegments,
			Coordinate target) {
		int indexOfClosestLineSegment = 0;
		for (int i = 1; i < lineSegments.length; i++) {
			if (lineSegments[i].distance(target) < lineSegments[indexOfClosestLineSegment]
					.distance(target)) {
				indexOfClosestLineSegment = i;
			}
		}
		return indexOfClosestLineSegment;
	}
	private LineString lineString(List lineSegments, GeometryFactory factory) {
		return factory.createLineString(new CoordinateList(
				coordinates(array(lineSegments)), false).toCoordinateArray());
	}
	private Coordinate[] coordinates(LineSegment[] lineSegments) {
		Coordinate[] coordinates = new Coordinate[lineSegments.length + 1];
		for (int i = 0; i < lineSegments.length; i++) {
			coordinates[i] = lineSegments[i].p0;
			if (i > 0) {
				Assert
						.isTrue(lineSegments[i].p0
								.equals(lineSegments[i - 1].p1));
			}
		}
		coordinates[lineSegments.length] = lineSegments[lineSegments.length - 1].p1;
		return coordinates;
	}
	public static LineSegment[] lineSegments(LineString lineString) {
		List lineSegments = new ArrayList();
		for (int i = 1; i < lineString.getNumPoints(); i++) {
			lineSegments.add(new LineSegment(lineString.getCoordinateN(i - 1),
					lineString.getCoordinateN(i)));
		}
		return array(lineSegments);
	}
	private static LineSegment[] array(List lineSegments) {
		return (LineSegment[]) lineSegments
				.toArray(new LineSegment[lineSegments.size()]);
	}
}
