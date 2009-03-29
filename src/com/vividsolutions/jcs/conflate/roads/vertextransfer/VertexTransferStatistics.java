package com.vividsolutions.jcs.conflate.roads.vertextransfer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
public class VertexTransferStatistics {
	private double maxDistance = 0;
	private double totalDistance = 0;
	private int transfers = 0;
	private int discardsDueToShortSegments = 0;
	private int discardsDueToVerticesOutOfOrder = 0;
	public void recordTransfer(Coordinate sourceVertex,
			Coordinate transferredVertex) {
		maxDistance = Math.max(maxDistance, sourceVertex
				.distance(transferredVertex));
		totalDistance += sourceVertex.distance(transferredVertex);
		transfers++;
		vectors.add(factory.createLineString(new Coordinate[]{
				sourceVertex, transferredVertex}));
	}
	private List vectors = new ArrayList();
	private GeometryFactory factory = new GeometryFactory();
	public void recordDiscardDueToShortSegment() {
		discardsDueToShortSegments++;
	}
	public void recordDiscardDueToVertexOutOfOrder() {
		discardsDueToVerticesOutOfOrder++;
	}
	public double getAverageDistance() {
		return transfers == 0 ? 0 : totalDistance / transfers;
	}
	public int getDiscardsDueToShortSegments() {
		return discardsDueToShortSegments;
	}
	public int getDiscardsDueToVerticesOutOfOrder() {
		return discardsDueToVerticesOutOfOrder;
	}
	public double getMaxDistance() {
		return maxDistance;
	}
	public int getTransfers() {
		return transfers;
	}
	public List getVectors() {
		return Collections.unmodifiableList(vectors);
	}
}
