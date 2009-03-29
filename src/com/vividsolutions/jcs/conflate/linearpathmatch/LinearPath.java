package com.vividsolutions.jcs.conflate.linearpathmatch;

import java.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * A contiguous set of {@link LinearEdges}.
 * Although not strictly enforced, most uses of this class
 * are expected to represent paths
 * that self-intersect at most at their endpoints.
 *
 * @version 1.0
 */
public class LinearPath
{
  private List edges = new ArrayList();
  private Coordinate[] nodes = null;
  private double length = 0.0;
  private int segmentCount = 0;

  public LinearPath() {
  }

  public LinearPath(Collection edges)
  {
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      add((LinearEdge) i.next());
    }
  }

  public LinearPath(LinearPath path, int nEdgesToUse)
  {
    if (nEdgesToUse > path.size())
      throw new IllegalArgumentException("edge count exceeds size of path");

    for (int i = 0; i < nEdgesToUse; i++) {
      add(path.getEdge(i));
    }
  }


  public void add(LinearEdge edge)
  {
    clearCache();
    edges.add(edge);
    segmentCount += edge.getGeometry().getNumPoints() - 1;
  }

  private void clearCache()
  {
    nodes = null;
  }

  public int size() { return edges.size(); }

  public int getNumSegments() { return segmentCount; }

  public LinearEdge getEdge(int i) { return (LinearEdge) edges.get(i); }

  public Coordinate[] getNodes()
  {
    if (nodes == null)
      nodes = computeNodes();
    return nodes;
  }

  private Coordinate[] computeNodes()
  {
    //int nNodes = edges.size() + 1;
    Coordinate[] pathNodes = new Coordinate[edges.size() + 1];
    for (int i = 0; i < edges.size(); i++) {
      LinearEdge edge = getEdge(i);
      LineString line = edge.getGeometry();
      Coordinate[] pts = line.getCoordinates();
      pathNodes[i] = pts[0];

      if (i == edges.size() - 1) {
        Coordinate finalNode = pts[pts.length - 1];
        pathNodes[i + 1] = finalNode;
      }
    }
    return pathNodes;
  }

  /**
   * Gets the GeometryFactory used for the geometries in the argument
   * path (if any).
   *
   * @return the {@link GeometryFactory}, or <code>null</code> if none
   */
  public GeometryFactory getGeometryFactory()
  {
    int numDE = edges.size();
    if (numDE == 0) return null;
    LinearEdge de = getEdge(0);
    return de.getGeometry().getFactory();
  }

  /**
   * Computes the {@link MultiLineString} representing the Geometry of the path.
   *
   * @return the MultiLineString for the entire path, or <code>null</code> if path is empty
   */
  public MultiLineString getGeometry()
  {
    if (edges.size() == 0) return null;

    // MD - could cache the result of this method
    LineString[] lines = new LineString[edges.size()];
    int linei = 0;
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      LinearEdge edge = (LinearEdge) i.next();
      LineString line = edge.getGeometry();
      lines[linei++] = line;
    }
    GeometryFactory factory = getGeometryFactory();
    if (factory != null) {
      return factory.createMultiLineString(lines);
    }
    return null;
  }

  /**
   * Computes the {@link LineString} representing the Geometry of the entire path.
   *
   * @return the LineString for the entire path, or <code>null</code> if path is empty
   */
  public LineString getLinearGeometry()
  {
    // MD - could cache the result of this method

    CoordinateList coordList = new CoordinateList();
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      LinearEdge edge = (LinearEdge) i.next();
      LineString line = edge.getGeometry();
      coordList.add(line.getCoordinates(), false);
    }
    Coordinate[] pathCoords = coordList.toCoordinateArray();
    GeometryFactory factory = getGeometryFactory();
    if (factory != null) {
      return factory.createLineString(pathCoords);
    }
    return null;
  }

}