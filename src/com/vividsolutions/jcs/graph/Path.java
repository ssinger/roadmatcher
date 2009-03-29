package com.vividsolutions.jcs.graph;

import java.util.*;

/**
 * Models a path through the graph.
 * A path is a sequence of {@link DirectedEdge}s.
 * Currently does not enforce that the path is connected
 * (although this would be easy to do).
 */
public class Path {

  protected List dirEdges = new ArrayList();

  /**
   * Creates an empty path.
   */
  public Path()
  {
  }

  /**
   * Creates a path containing a single edge.
   * @param de the DirectedEdge to include in the path
   */
  public Path(DirectedEdge de)
  {
    extend(de);
  }

  public void extend(DirectedEdge de)
  {
    dirEdges.add(de);
  }

  public void extend(Collection deColl)
  {
    for (Iterator i = deColl.iterator(); i.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) i.next();
      extend(de);
    }
  }

  public int size() { return dirEdges.size(); }

  public DirectedEdge getDirectedEdge(int index) { return (DirectedEdge) dirEdges.get(index); }

  public DirectedEdge getEndDirectedEdge() { return (DirectedEdge) dirEdges.get(dirEdges.size() - 1); }

  public Node getEndNode() { return getEndDirectedEdge().getToNode(); }

  public Node getStartNode() { return ((DirectedEdge) dirEdges.get(0)).getFromNode(); }
}