package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;

/**
 * Records the line segments making up a {@link LinearPath}
 * as a list of vertices,
 * along with index information about their position in the original path.
 *
 * @version 1.0
 */
public class FlatPath
{

  private double minSegLen;
  private LinearPath path;
  private Coordinate[] pts;
  private boolean[] isNode;
  private int[] nodeVertexIndex;

  public FlatPath(LinearPath path)
  {
    this.path = path;
    build();
  }

  public LinearPath getPath() { return path; }
  public int getNumPoints() { return pts.length; }
  public Coordinate getCoordinate(int i)  {    return pts[i];  }
  public boolean isNode(int i)  { return isNode[i];  }

  public int getNumNodes() { return nodeVertexIndex.length; }
  public int getNodeVertexIndex(int i) { return nodeVertexIndex[i]; }

  private void build()
  {
    int iPathPt = 0;

    int numEdges = path.size();
    nodeVertexIndex = new int[numEdges + 1];

    int numVertices = path.getNumSegments() + 1;
    pts = new Coordinate[numVertices];
    isNode = new boolean[numVertices];

    for (int iEdge = 0; iEdge < numEdges; iEdge++) {
      LinearEdge edge = path.getEdge(iEdge);
      LineString line = edge.getGeometry();
      Coordinate[] edgePts = line.getCoordinates();
      boolean isForward = true;
      int numEdgePts = edgePts.length;
      int numEdgeSegs = numEdgePts - 1;
      isNode[iPathPt] = true;
      nodeVertexIndex[iEdge] = iPathPt;
      for (int iPt = 0; iPt < numEdgeSegs; iPt++) {
        pts[iPathPt] = edgePts[iPt];
        iPathPt++;
      }
      // add closing point for this edge if it's the last one
      if (iEdge == numEdges - 1) {
        pts[pts.length - 1] = edgePts[edgePts.length - 1];
      }
    }
    // last point in path is always a node!
    isNode[isNode.length - 1] = true;
    nodeVertexIndex[nodeVertexIndex.length - 1] = pts.length - 1;
  }

  public Coordinate findClosestPoint(Coordinate pt, int[] segIndex)
  {
    return findClosestPoint(pt, 0, segIndex);
  }

  public Coordinate findClosestPoint(Coordinate pt, int minSegmentIndex, int[] segIndex)
  {
    LineSegment seg = new LineSegment();
    Coordinate closestPt = null;
    double distance = 0.0;
    int closestSegmentIndex = minSegmentIndex;

    for (int i = minSegmentIndex; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];
      Coordinate candidateClosestPt = seg.closestPoint(pt);
      double candidateDistance = pt.distance(candidateClosestPt);
      if (closestPt == null || candidateDistance < distance) {
        closestPt = candidateClosestPt;
        distance = candidateDistance;
        /**
         * use the convention that segments are closed at their start end
         * and open at the final end.
         * Thus if the closest point is the last point of a segment,
         * the index used is the index of the next segment
         * (which may be the index of the final vertex in the path)
         */
        if (closestPt.equals(seg.p1))
            closestSegmentIndex = i + 1;
        else
          closestSegmentIndex = i;
      }
    }
    // ensure a valid point is always returned
    if (closestPt == null)
      closestPt = pts[pts.length - 1];
    segIndex[0] = closestSegmentIndex;
    return closestPt;
  }

}