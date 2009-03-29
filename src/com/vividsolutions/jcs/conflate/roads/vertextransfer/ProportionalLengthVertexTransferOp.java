package com.vividsolutions.jcs.conflate.roads.vertextransfer;

import java.io.Serializable;
import java.util.List;

import com.vividsolutions.jcs.algorithm.linearreference.LocatePoint;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;

public class ProportionalLengthVertexTransferOp extends
		AbstractVertexTransferOp implements Serializable {

	private boolean sameOrientation;

	private double sourceLength;

	private double destinationLength;

	protected Coordinate transferredCoordinate(Coordinate sourceCoordinate,
			int sourceCoordinateIndex) {
		// Avoid roundoff errors by returning explicit values for the endpoints
		// [Jon Aquino 2004-03-23]
		return sourceCoordinateIndex == 0 ? destinationCoordinateForFirstSourceCoordinate()
				: sourceCoordinateIndex == getSourceCoordinates().size() - 1 ? destinationCoordinateForLastSourceCoordinate()
						: convertLengthToCoordinate(
								proportionalityFactor(sourceCoordinate)
										* destinationLength,
								getDestinationCoordinates());
	}

	private Coordinate destinationCoordinateForFirstSourceCoordinate() {
		return sameOrientation ? firstDestinationCoordinate()
				: lastDestinationCoordinate();
	}

	private Coordinate destinationCoordinateForLastSourceCoordinate() {
		return sameOrientation ? lastDestinationCoordinate()
				: firstDestinationCoordinate();
	}

	private Coordinate lastDestinationCoordinate() {
		return coordinate(getDestinationCoordinates().size() - 1,
				getDestinationCoordinates());
	}

	private Coordinate firstDestinationCoordinate() {
		return coordinate(0, getDestinationCoordinates());
	}

	private Coordinate convertLengthToCoordinate(final double targetLength,
			List coordinates) {
		double currentLength = 0;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			LineSegment lineSegment = lineSegment(i, coordinates);
			if (currentLength + lineSegment.getLength() >= targetLength) {
				return LocatePoint.pointAlongSegment(lineSegment, targetLength
						- currentLength);
			}
			currentLength += lineSegment.getLength();
		}
		Assert.shouldNeverReachHere("targetLength=" + targetLength
				+ "; currentLength=" + currentLength);
		return null;
	}

	private double proportionalityFactor(Coordinate sourceCoordinate) {
		return proportionalityFactor(getSourceCoordinates().indexOf(
				sourceCoordinate));
	}

	private double proportionalityFactor(int i) {
		return length(sameOrientation ? getSourceCoordinates()
				.subList(0, i + 1) : getSourceCoordinates().subList(i,
				getSourceCoordinates().size()))
				/ sourceLength;
	}

	private double length(List coordinates) {
		double length = 0;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			length += coordinate(i, coordinates).distance(
					coordinate(i + 1, coordinates));
		}
		return length;
	}

	public LineString transfer(LineString source, LineString destination,
			double minimumLineSegmentLength, VertexTransferStatistics statistics) {
		sameOrientation = sameOrientation(source, destination);
		sourceLength = source.getLength();
		destinationLength = destination.getLength();
		return super.transfer(source, destination, minimumLineSegmentLength,
				statistics);
	}

	private boolean sameOrientation(LineString a, LineString b) {
		return LineStringUtil.first(a).distance(LineStringUtil.first(b))
				+ LineStringUtil.last(a).distance(LineStringUtil.last(b)) < LineStringUtil
				.first(a).distance(LineStringUtil.last(b))
				+ LineStringUtil.last(a).distance(LineStringUtil.first(b));
	}
}