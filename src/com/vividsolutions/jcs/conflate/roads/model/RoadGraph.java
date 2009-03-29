package com.vividsolutions.jcs.conflate.roads.model;

import java.util.*;
import com.vividsolutions.jcs.graph.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jcs.conflate.roads.model.RoadSegment;

public class RoadGraph extends PlanarGraph {

    public RoadGraph(RoadNetwork network) {
        this.network = network;
    }

    public void add(Edge edge) {
        RoadSegment roadSegment = (RoadSegment) edge;
        RoadNode startNode = getNode(roadSegment.getLine().getCoordinateN(0));
        RoadNode endNode = getNode(roadSegment.getLine()
                .getCoordinateN(roadSegment.getLine().getNumPoints() - 1));
        roadSegment.setStartNode(startNode);
        roadSegment.setEndNode(endNode);
        DirectedEdge directedEdge0 = new DirectedEdge(startNode, endNode, roadSegment.getLine()
                .getCoordinateN(1),
                true);
        DirectedEdge directedEdge1 = new DirectedEdge(endNode, startNode, roadSegment.getLine()
                .getCoordinateN(roadSegment.getLine().getNumPoints() - 2),
                false);
        roadSegment.setDirectedEdges(directedEdge0, directedEdge1);
        super.add(roadSegment);
    }

    public RoadNetwork getNetwork() {
        return network;
    }

    public RoadNode getNode(Coordinate point) {
        RoadNode node = (RoadNode) findNode(point);
        if (node == null) {
            node = new RoadNode(point, this);
            add(node);
        }
        return node;
    }

    /**
     * Checks for any coincident ends of edges.
     *
     * @return a List of the coincident edges, if any
     */
    public Set checkCoincidentEdges()
    {
      Set coincidentEdges = new HashSet();
      for (Iterator i = nodeIterator(); i.hasNext(); ) {
        Node node = (Node) i.next();
        DirectedEdgeStar deStar = node.getOutEdges();
        DirectedEdge firstDE = null;
        DirectedEdge prevDE = null;
        for (Iterator j = deStar.iterator(); j.hasNext(); ) {
          DirectedEdge de = (DirectedEdge) j.next();
          if (firstDE == null) firstDE = de;
          if (prevDE != null) {
            if (isCoincidentEdge(de, prevDE)) {
              coincidentEdges.add(de.getEdge());
              coincidentEdges.add(prevDE.getEdge());
            }
          }
          prevDE = de;
        }
        if (firstDE != prevDE)
          if (isCoincidentEdge(firstDE, prevDE)) {
            coincidentEdges.add(firstDE.getEdge());
            coincidentEdges.add(prevDE.getEdge());
          }
      }
      return coincidentEdges;
    }

    private boolean isCoincidentEdge(DirectedEdge de0, DirectedEdge de1)
    {
      if (de0.getAngle() == de1.getAngle()) {
//        System.out.println("found coincident edge");
//        RoadSegment rs = (RoadSegment) de0.getEdge();
//        System.out.println(rs.getLine());
        return true;
      }
      return false;
    }

    private RoadNetwork network;
}