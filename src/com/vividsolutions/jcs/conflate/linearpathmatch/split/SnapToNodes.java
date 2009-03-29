package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Optimizes the splitting of a path by
 * snapping split nodes to nodes on the path if
 * they are within tolerance.
 * Uses the rule that a given node will have the
 * closest split node snapped to it.
 * This class is an algorithm class.
 *
 * @version 1.0
 */
public class SnapToNodes
{
  private SplitNode[] splitNodes;
  private QuantizedPath qPath;
  private FlatPath flatPath;
  private double nodeDistanceTolerance;

  /**
   * The input must be a valid matched split path.
   *
   * @param splitPath a splitPath which has been matched
   * @param nodeDistanceTolerance
   */
  public SnapToNodes(SplitPath splitPath, double nodeDistanceTolerance)
  {
    splitNodes = splitPath.getSplitNodes();
    qPath = splitPath.getQuantizedPath();
    flatPath = qPath.getFlatPath();
    this.nodeDistanceTolerance = nodeDistanceTolerance;
    snapNodes();
  }

  private void snapNodes()
  {
    // nothing to do!
    if (splitNodes.length <= 0)
      return;
    for (int i = 0; i < flatPath.getNumNodes(); i++) {
      snapNode(i);
    }
  }

  private void snapNode(int nodeIndex)
  {
    int nodeVertexi = flatPath.getNodeVertexIndex(nodeIndex);
    Coordinate nodeCoord = flatPath.getCoordinate(nodeVertexi);
    int closestSplitNodeIndex = findClosestNonNodeMatchedSplitNodeIndex(nodeCoord);
    // couldn't find one to match to
    if (closestSplitNodeIndex < 0)
      return;
    // don't snap if node is too far away
    double distance = nodeCoord.distance(splitNodes[closestSplitNodeIndex].getSplittingPt());
    if (distance > nodeDistanceTolerance)
      return;
    // don't snap end split nodes
    if (closestSplitNodeIndex == 0 || closestSplitNodeIndex == splitNodes.length - 1)
      return;

    QuantumIndex nodeQuantum = qPath.getVertexIndex(nodeVertexi);
    SplitNode candidateSplitNode = splitNodes[closestSplitNodeIndex];

    // check that splitNode location can be changed to the node location
    if (! isValidLocation(closestSplitNodeIndex, nodeVertexi))
      return;

    splitNodes[closestSplitNodeIndex].setPlace(nodeQuantum);
  }

  /**
   * Tests if a vertex is a valid location for a split node.
   * The location is valid if the splitnode ordering invariant
   * is maintained.  This is the case iff
   * the vertex location is strictly between
   * the locations of the adjacent splitNodes (if any).
   *
   * @param splitNodeIndex the index of the split node to test
   * @param candidateVertexIndex the proposed new location
   * @return <code>true</code> if the location is valid
   */
  private boolean isValidLocation(int splitNodeIndex, int candidateVertexIndex)
  {
    QuantumIndex lowerBound = null;
    if (splitNodeIndex > 0)
      lowerBound = splitNodes[splitNodeIndex - 1].getPlace();
    QuantumIndex upperBound = null;
    if (splitNodeIndex < splitNodes.length - 1)
      upperBound = splitNodes[splitNodeIndex + 1].getPlace();
    QuantumInterval qInt = new QuantumInterval(lowerBound, upperBound);
    return qInt.isProperlyContained(candidateVertexIndex);
  }

  private boolean isPlacedAtNode(SplitNode splitNode)
  {
    if (! splitNode.isPlaced())
      return false;
    if (! splitNode.getPlace().isVertex())
      return false;
    int vertexi = splitNode.getPlace().getSegmentIndex();
    if (vertexi > flatPath.getNumPoints())
      System.out.println("vertex out of range: " + vertexi);
    return flatPath.isNode(vertexi);
  }

  private int findClosestNonNodeMatchedSplitNodeIndex(Coordinate coord)
  {
    int closestIndex = -1;
    double minDistance = Double.MAX_VALUE;
    for (int i = 0; i < splitNodes.length; i++)
    {
      if (isPlacedAtNode(splitNodes[i]))
        continue;
      double dist = coord.distance(splitNodes[i].getSplittingPt());
      if (dist < minDistance) {
        closestIndex = i;
        minDistance = dist;
      }
    }
    return closestIndex;
  }
}