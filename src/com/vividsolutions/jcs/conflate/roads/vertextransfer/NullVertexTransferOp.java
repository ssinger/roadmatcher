package com.vividsolutions.jcs.conflate.roads.vertextransfer;
import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;
public class NullVertexTransferOp extends AbstractVertexTransferOp implements Serializable {

	protected Coordinate transferredCoordinate(Coordinate sourceCoordinate,
			int sourceCoordinateIndex) {
		return sourceCoordinate;
	}
}
