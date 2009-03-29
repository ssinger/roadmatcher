package com.vividsolutions.jcs.edgemerge;

import java.util.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jcs.graph.*;
import com.vividsolutions.jts.geom.*;
/**
 * Merges edges of a linear network together based on the
 * differences between edge attributes
 *
 * @version 1.0
 */
public class NetworkEdgeMerger {

  private MergeGraph graph = new MergeGraph();
  private List resultGeom;

  public NetworkEdgeMerger(FeatureCollection fc) {
    build(fc);
  }

  private void build(FeatureCollection fc)
  {
    for (Iterator i = fc.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      graph.add(f);
    }
  }

  public FeatureCollection merge()
  {
    resultGeom = new ArrayList();
    for (Iterator i = graph.nodeIterator(); i.hasNext(); ) {
      Node node = (Node) i.next();
      if (node.getDegree() > 2)
        mergeEdges(node);
    }
    FeatureCollection fc = FeatureDatasetFactory.createFromGeometry(resultGeom);
    return fc;
  }

  private void mergeEdges(Node node)
  {
    DirectedEdgeStar deStar = node.getOutEdges();
    for (Iterator i = deStar.iterator(); i.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) i.next();
      if (! ((MergeEdge) de.getEdge()).isVisited())
        merge(de);
    }
  }

  private static void addCoordinates(Coordinate[] coords, boolean isForward, CoordinateList coordList)
  {
    if (isForward) {
      for (int i = 0; i < coords.length; i++) {
        coordList.add(coords[i], false);
      }
    }
    else {
      for (int i = coords.length - 1; i >= 0; i--) {
        coordList.add(coords[i], false);
      }
    }
  }

  private void merge(DirectedEdge startDE)
  {
    List deList = traceSequentialEdges(startDE);
    CoordinateList pts = new CoordinateList();
    GeometryFactory geomFact = null;
    for (Iterator i = deList.iterator(); i.hasNext();) {
      DirectedEdge de = (DirectedEdge) i.next();
      MergeEdge e = (MergeEdge) de.getEdge();
      //assert: edge is not visited
      e.setVisited(true);
      Geometry g = e.getGeometry();
      // save the factory to use later
      geomFact = g.getFactory();
      addCoordinates(e.getGeometry().getCoordinates(), de.getEdgeDirection(), pts);
    }
    resultGeom.add(geomFact.createLineString(pts.toCoordinateArray()));
  }

  private List traceSequentialEdges(DirectedEdge de)
  {
    List deList = new ArrayList();
    int degree = 2;
    do {
      deList.add(de);
      Node nextNode = de.getToNode();
      degree = nextNode.getDegree();
      if (degree == 2)
        de = nextNode.getOutEdges().getNextEdge(de.getSym());
    }
    while (degree == 2);
    return deList;
  }
}