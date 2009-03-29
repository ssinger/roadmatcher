package com.vividsolutions.jcs.conflate.roads.vertextransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;

/**
 * Not thread-safe.
 */
public abstract class AbstractVertexTransferOp {
	private List sourceCoordinates;

	private VertexTransferStatistics statistics;

	private static class InsertionDirection {
		public static InsertionDirection BACKWARDS = new InsertionDirection();

		public static InsertionDirection FORWARDS = new InsertionDirection();
	}

	protected Coordinate coordinate(int i, List coordinates) {
		return (Coordinate) coordinates.get(i);
	}

	protected List getDestinationCoordinates() {
		return destinationCoordinates;
	}

	protected double[] indexOfAndLengthToClosestSegment(Coordinate c,
			List coordinates) {
		Assert.isTrue(!coordinates.isEmpty());
		int indexOfClosestSegment = -1;
		double distanceToClosestSegment = -1;
		double lengthToClosestSegment = -1;
		double lengthToCurrentSegment = 0;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			double distanceToCurrentSegment = lineSegment(i, coordinates)
					.distance(c);
			if (indexOfClosestSegment == -1
					|| distanceToCurrentSegment < distanceToClosestSegment) {
				indexOfClosestSegment = i;
				distanceToClosestSegment = distanceToCurrentSegment;
				lengthToClosestSegment = lengthToCurrentSegment;
			}
			lengthToCurrentSegment += lineSegment(i, coordinates).getLength();
		}
		return new double[] { indexOfClosestSegment, lengthToClosestSegment };
	}

	private void insert(Coordinate transferredVertex, Coordinate sourceVertex) {
		double[] indexOfAndLengthToClosestSegment = indexOfAndLengthToClosestSegment(
				transferredVertex, destinationCoordinates);
		insert(transferredVertex, (int) indexOfAndLengthToClosestSegment[0],
				indexOfAndLengthToClosestSegment[1], destinationCoordinates,
				sourceVertex);
	}

	private void insert(Coordinate originalTransferredVertex,
			int indexOfClosestSegment, double lengthToClosestSegment,
			List coordinates, Coordinate sourceVertex) {
		Coordinate transferredVertex = originalTransferredVertex;
		LineSegment lineSegment = lineSegment(indexOfClosestSegment,
				coordinates);
		double insertionDistance = lengthToClosestSegment
				+ lineSegment.project(transferredVertex).distance(
						lineSegment.p0);
		if (!satisfiesInsertionDirection(insertionDistance)) {
			statistics.recordDiscardDueToVertexOutOfOrder();
			return;
		}
		Coordinate endpointTooClose = endpointTooClose(transferredVertex,
				lineSegment);
		if (endpointTooClose != null && lastTransferredVertex != null
				&& endpointTooClose.equals2D(lastTransferredVertex)) {
			statistics.recordDiscardDueToShortSegment();
			return;
		}
		if (endpointTooClose != null) {
			transferredVertex = setZ(new Coordinate(endpointTooClose),
					transferredVertex.z);
			Coordinate existingVertex = match((Coordinate) coordinates
					.get(indexOfClosestSegment), (Coordinate) coordinates
					.get(indexOfClosestSegment + 1), transferredVertex.x,
					transferredVertex.y);
			// If source and destination vertices are equal in all three
			// dimensions, don't treat it as a transfer, for compatibility
			// with existing JUnit tests. [Jon Aquino 2004-10-05]
			if (existingVertex.equals3D(transferredVertex)
					|| Double.isNaN(transferredVertex.z)) {
				statistics.recordDiscardDueToShortSegment();
				return;
			}
			existingVertex.setCoordinate(transferredVertex);
		} else {
			coordinates.add(indexOfClosestSegment + 1, transferredVertex);
		}
		insertionDistances.add(new Double(insertionDistance));
		statistics.recordTransfer(sourceVertex, transferredVertex);
		lastTransferredVertex = transferredVertex;
	}

	private Coordinate match(Coordinate a, Coordinate b, double x, double y) {
		return a.x == x && a.y == y ? a : b.x == x && b.y == y ? b : null;
	}

	private InsertionDirection insertionDirection(
			double previousInsertionDistance, double nextInsertionDistance) {
		return nextInsertionDistance > previousInsertionDistance ? InsertionDirection.FORWARDS
				: InsertionDirection.BACKWARDS;
	}

	private double lastInsertionDistance() {
		return ((Double) insertionDistances.get(insertionDistances.size() - 1))
				.doubleValue();
	}

	protected LineSegment lineSegment(int i, List coordinates) {
		lineSegment.setCoordinates(coordinate(i, coordinates), coordinate(
				i + 1, coordinates));
		return lineSegment;
	}

	private boolean satisfiesInsertionDirection(double insertionDistance) {
		return insertionDistances.size() < 2
				|| insertionDirection(secondToLastInsertionDistance(),
						lastInsertionDistance()) == insertionDirection(
						lastInsertionDistance(), insertionDistance);
	}

	private boolean satisfiesMinLineSegmentLength(Coordinate newVertex,
			LineSegment lineSegment) {
		return newVertex.distance(lineSegment.p0) >= minLineSegmentLength
				&& newVertex.distance(lineSegment.p1) >= minLineSegmentLength;
	}

	private Coordinate endpointTooClose(Coordinate newVertex,
			LineSegment lineSegment) {
		return newVertex.distance(lineSegment.p0) < minLineSegmentLength ? lineSegment.p0
				: newVertex.distance(lineSegment.p1) < minLineSegmentLength ? lineSegment.p1
						: null;
	}

	private double secondToLastInsertionDistance() {
		return ((Double) insertionDistances.get(insertionDistances.size() - 2))
				.doubleValue();
	}

	private void transfer(Coordinate sourceCoordinate, int sourceCoordinateIndex) {
		// Create new coordinate, as #transferredCoordinate may return
		// Coordinate instance from LineSegment or sourceCoordinates.
		// [Jon Aquino 2004-09-30]
		insert(setZ(new Coordinate(transferredCoordinate(sourceCoordinate,
				sourceCoordinateIndex)), sourceCoordinate.z), sourceCoordinate);
	}

	private Coordinate setZ(Coordinate coordinate, double z) {
		coordinate.z = z;
		return coordinate;
	}

	public LineString transfer(LineString source, LineString destination,
			double minLineSegmentLength, VertexTransferStatistics statistics) {
		Assert.isTrue(minLineSegmentLength > 0,
				"Otherwise get duplicate vertices [Jon Aquino 2004-09-30]]");
		this.statistics = statistics;
		insertionDistances = new ArrayList();
		lastTransferredVertex = null;
		this.minLineSegmentLength = minLineSegmentLength;
		// Clone the destination, as we will be modifying its coordinates'
		// z-values [Jon Aquino 2004-10-01]
		destinationCoordinates = new ArrayList(Arrays
				.asList(((LineString) destination.clone()).getCoordinates()));
		sourceCoordinates = new ArrayList(Arrays
				.asList(source.getCoordinates()));
		for (int i = 0; i < sourceCoordinates.size(); i++) {
			transfer(coordinate(i, sourceCoordinates), i);
		}
		return destination.getFactory()
				.createLineString(
						(Coordinate[]) destinationCoordinates
								.toArray(new Coordinate[destinationCoordinates
										.size()]));
	}

	private Coordinate lastTransferredVertex;

	protected abstract Coordinate transferredCoordinate(
			Coordinate sourceCoordinate, int sourceCoordinateIndex);

	private List destinationCoordinates;

	private List insertionDistances;

	private LineSegment lineSegment = new LineSegment();

	private double minLineSegmentLength;

	protected List getSourceCoordinates() {
		return sourceCoordinates;
	}
}