package com.vividsolutions.jcs.edgemerge;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jcs.graph.*;

public class MergeGraph
    extends PlanarGraph
{

  public MergeGraph() {
  }

  public void add(Feature f)
  {
    LineString line = (LineString) f.getGeometry();
    Coordinate[] linePts = CoordinateArrays.removeRepeatedPoints(line.getCoordinates());
    Coordinate startPt = linePts[0];
    Coordinate endPt = linePts[linePts.length - 1];

    Node nStart = getNode(startPt);
    Node nEnd = getNode(endPt);

    DirectedEdge de0 = new DirectedEdge(nStart, nEnd, linePts[1], true);
    DirectedEdge de1 = new DirectedEdge(nEnd, nStart, linePts[linePts.length - 2], false);
    Edge edge = new MergeEdge(f);
    edge.setDirectedEdges(de0, de1);
    add(edge);

  }
  private Node getNode(Coordinate pt)
  {
    Node node = findNode(pt);
    if (node == null) {
      node = new Node(pt);
      // ensure node is only added once to graph
      add(node);
    }
    return node;
  }


}