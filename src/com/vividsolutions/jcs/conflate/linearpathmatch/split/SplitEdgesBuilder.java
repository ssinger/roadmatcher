package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jts.util.Assert;

/**
 * A factory to create the {@link SplitEdge}s for a {@link SplitPath}
 * based on the final locations of the {@link SplitNode}s.
 *
 * @version 1.0
 */
public class SplitEdgesBuilder
{
  private GeometryFactory geomFact;
  private List splitEdges = new ArrayList();

  public SplitEdgesBuilder(GeometryFactory geomFact) {
    this.geomFact = geomFact;
  }

  public List buildEdges(SplitPath splitPath)
  {
    List allPathNodes = NodeMerger.mergeNodes(splitPath);
    return buildEdges(splitPath, allPathNodes);
  }

  private List buildEdges(SplitPath splitPath, List allPathNodes)
  {
    List splitEdges = new ArrayList();
    for (int i = 0; i < allPathNodes.size() - 1; i++) {
      SplitEdgeNode node0 = (SplitEdgeNode) allPathNodes.get(i);
      SplitEdgeNode node1 = (SplitEdgeNode) allPathNodes.get(i + 1);
      splitEdges.add(buildSplitEdge(i, splitPath, node0, node1, node0.splittingEdgeIndex));
    }
    return splitEdges;
  }

  private SplitEdge buildSplitEdge(
      int splitEdgeIndex,
      SplitPath splitPath,
      SplitEdgeNode node0,
      SplitEdgeNode node1,
      int splittingEdgeIndex)
  {
    FlatPath flatPath = splitPath.getQuantizedPath().getFlatPath();
    CoordinateList coordList = new CoordinateList();

    if (! node0.isPathVertex())
      coordList.add(node0.getCoordinate(), false);
    else
      coordList.add(flatPath.getCoordinate(node0.vertexIndex), false);

    // start loop from the vertex after node0.
    // This handles both situations (of node0 being a vertex or not)
    for (int i = node0.vertexIndex + 1; i <= node1.vertexIndex; i++) {
      coordList.add(flatPath.getCoordinate(i), false);
    }

    if (! node1.isPathVertex())
      coordList.add(node1.getCoordinate(), false);
    // if node1 is a path vertex, its coordinate has already been added in the loop above

    LinearEdge parent = flatPath.getPath().getEdge(node0.sourceEdgeIndex);
    return new SplitEdge(splitEdgeIndex, parent, getLine(coordList), node0.sourceEdgeIndex, splittingEdgeIndex);
  }

  /**
   * Creates a LineString from the current coordList (if it contains a valid line)
   *
   * @return the LineString created from the points in the coordList,
   * or <code>null</code> if the list is empty or the line is invalid
   */
  private LineString getLine(CoordinateList coordList)
  {
    if (coordList == null
        || coordList.size() < 2) {
      return null;
    }
    Coordinate[] pts = coordList.toCoordinateArray();
    LineString line = geomFact.createLineString(pts);
    return line;
  }
}

class NodeMerger
{
  public static List mergeNodes(SplitPath splitPath)
  {
    NodeMerger merger = new NodeMerger();
    return merger.computeMergedNodes(splitPath);
  }

  private FlatPath flatPath;
  private SplitNode[] splitNodesOnPath;

  private int numPathNodes;
  private int pathNodei = 0;

  private int numSplitNodes;
  private int splitNodei = 0;
  private int splittingEdgeIndex = SplitEdge.UNKNOWN;

  private static final int PATH_NODE_FIRST = 1;
  private static final int NODES_EQUAL = 0;
  private static final int SPLIT_NODE_FIRST = -1;

  /**
   * Compares the next path node and split node (if any)
   * and determines which comes first in the merge order, or if they are both the same.
   *
   * @return 1, 0, or -1 if the path node is first, both are equal, or if the split node is first
   */
  private int compareNodeOrder()
  {
    if (! hasNextPathNode()) return -1;
    if (! hasNextSplitNode()) return 1;

    int pathNodeVertexi = getPathNodeVertexIndex();
    int splitNodeVertexi = splitNodesOnPath[splitNodei].getPlace().getSegmentIndex();
    int splitNodeQuantumi = splitNodesOnPath[splitNodei].getPlace().getQuantumIndex();

    if (splitNodeVertexi < pathNodeVertexi)
      return -1;
    if (pathNodeVertexi < splitNodeVertexi
             || (pathNodeVertexi == splitNodeVertexi && splitNodeQuantumi > 0))
      return 1;
    // assert: splitNodeVertexi == pathNodeVertexi
    return 0;
  }

  private int getPathNodeVertexIndex() { return flatPath.getNodeVertexIndex(pathNodei); }

  private boolean hasNextPathNode() { return pathNodei < numPathNodes; }

  private void nextPathNode() {    pathNodei++;  }

  private boolean hasNextSplitNode() { return splitNodei < numSplitNodes; }

  private void nextSplitNode()  {    splitNodei++;  }

  private void setCurrentSplittingEdge()
  {
    if (splitNodei < numSplitNodes)
      splittingEdgeIndex = splitNodesOnPath[splitNodei].getSourceEdgeIndex();
    else
      splittingEdgeIndex = SplitEdge.UNKNOWN;
  }

  private void init(SplitPath splitPath)
  {
    flatPath = splitPath.getQuantizedPath().getFlatPath();
    splitNodesOnPath = splitPath.getSplitNodes();
    numPathNodes = flatPath.getNumNodes();
    numSplitNodes = splitNodesOnPath.length;;
  }

  public List computeMergedNodes(SplitPath splitPath)
  {
    init(splitPath);
    List allPathNodes = new ArrayList();

    while (hasNextPathNode()) {
      switch (compareNodeOrder()) {
        case SPLIT_NODE_FIRST:
          setCurrentSplittingEdge();
          allPathNodes.add(new SplitEdgeNode(
              pathNodei - 1,
              splitNodesOnPath[splitNodei].getPlace().getSegmentIndex(),
              splitNodesOnPath[splitNodei].getSplitCoordinate(),
              splitNodesOnPath[splitNodei].getSourceEdgeIndex()));
          nextSplitNode();
          break;
        case NODES_EQUAL:
          setCurrentSplittingEdge();
          allPathNodes.add(new SplitEdgeNode(
              pathNodei,
              getPathNodeVertexIndex(),
              splittingEdgeIndex));
          nextSplitNode();
          nextPathNode();
          break;
        case PATH_NODE_FIRST:
          allPathNodes.add(new SplitEdgeNode(
              pathNodei,
              getPathNodeVertexIndex(),
              splittingEdgeIndex));
          nextPathNode();
          break;
      }
    }
    return allPathNodes;
  }
}

class SplitEdgeNode
{
  int sourceEdgeIndex = -1;
  int vertexIndex = -1;
  Coordinate splitPt = null;
  int splittingEdgeIndex = -1;

  public SplitEdgeNode(int sourceEdgeIndex, int vertexIndex, int splittingEdgeIndex)
  {
    this.sourceEdgeIndex = sourceEdgeIndex;
    this.vertexIndex = vertexIndex;
    this.splittingEdgeIndex = splittingEdgeIndex;
  }

  public SplitEdgeNode(int sourceEdgeIndex, int containingVertexIndex, Coordinate splitPt, int splittingEdgeIndex)
  {
    this.sourceEdgeIndex = sourceEdgeIndex;
    this.vertexIndex = containingVertexIndex;
    this.splitPt = splitPt;
    this.splittingEdgeIndex = splittingEdgeIndex;
  }

  public Coordinate getCoordinate()
  {
    return splitPt;
  }

  public boolean  isPathVertex()
  {
    return splitPt == null;
  }
}


