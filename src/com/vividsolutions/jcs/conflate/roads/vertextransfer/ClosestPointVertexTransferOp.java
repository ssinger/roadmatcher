package com.vividsolutions.jcs.conflate.roads.vertextransfer;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;

public class ClosestPointVertexTransferOp extends AbstractVertexTransferOp
		implements Serializable {

	protected Coordinate transferredCoordinate(Coordinate sourceCoordinate,
			int sourceCoordinateIndex) {
		return lineSegment(
				(int) indexOfAndLengthToClosestSegment(sourceCoordinate,
						getDestinationCoordinates())[0],
				getDestinationCoordinates()).closestPoint(sourceCoordinate);
	}
}