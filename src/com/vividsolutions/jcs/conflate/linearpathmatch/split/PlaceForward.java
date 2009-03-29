package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A strategy class to place splitting nodes along the path
 * in a forward direction only
 * (i.e. using a greedy approach).
 * This approach may result in unmatched nodes being left at the
 * end of the path - these will have to be resolved by further placement.
 * <p>
 * Maintains the invariant:
 * <ul>
 * <li>Split nodes occur in the same order along the split path
 * as the splitting points occur along the splitting path
 * </ul>
 *
 * @version 1.0
 */
public class PlaceForward {

  private SplitPath splitPath;
  private SplitNode[] splitNodes;
  private QuantizedPath qPath;
  private double nodeDistanceTolerance;

  public PlaceForward(SplitPath splitPath, double nodeDistanceTolerance)
  {
    this.splitPath = splitPath;
    splitNodes = splitPath.getSplitNodes();
    qPath = splitPath.getQuantizedPath();
    this.nodeDistanceTolerance = nodeDistanceTolerance;
    place();
  }

  private void place()
  {
    // check if there's anything to do!
    if (splitNodes.length == 0)
      return;
    /**
     * Match the nodes on the end of the splitting path first, to ensure
     * reasonable behaviour at the ends of the path.
     * End splitting nodes are matched to the end nodes of the target path,
     * if possible (i.e. within tolerance).
     * Otherwise, they are matched to a node if there is one close enough.
     */
    placeFirstNode();
    placeLastNode();

    // now match all nodes in a forward direction
    placeNodesForward();
  }

  /**
   * Match all unmatched split nodes in a forward direction,
   * ensuring that each node match is strictly further along
   * the path.
   * Some nodes may be left unmatched at the end of this process
   * (because they have "piled up" at the end of the target path).
   */
  private void placeNodesForward()
  {
    QuantumInterval validInt = new QuantumInterval(
        splitNodes[0].getPlace(),
        splitNodes[splitNodes.length - 1].getPlace());

    for (int i = 0; i < splitNodes.length; i++) {
      SplitNode splitNode = splitNodes[i];
      if (! splitNode.isPlaced()) {
        QuantumIndex closestQI = splitNode.getClosestLocation();
        /**
         * Heuristic to prevent split nodes that should match at the end
         * of the current interval from being matched to the beginning
         */
        if (isAtOrBeyondInterval(closestQI, validInt))
          break;
        QuantumIndex matchQI = findLocationInInterval(closestQI, validInt);
        /**
         * if no match made, the interval must be too small to allow any matches
         * In this case, we won't be able to make any more matches at all, so exit.
         */
        if (matchQI == null)
          break;
        splitNode.setPlace(matchQI);
        // update the interval to set the new lower bound for match locations
        validInt.setBound(0, matchQI);
      }
    }
  }

  private boolean isAtOrBeyondInterval(QuantumIndex qi, QuantumInterval validInt)
  {
    QuantumIndex upperBound = validInt.getBound(1);
    if (upperBound == null) return false;
    boolean isAtOrBeyond = qi.compareTo(upperBound) >= 0;
    return qi.compareTo(upperBound) >= 0;
  }

  private QuantumIndex findLocationInInterval(QuantumIndex qi, QuantumInterval validInt)
  {
    /**
     * If closest point is not in valid interval,
     * replace it with the next allowable value;
     * e.g. the lowest quantum inside the interval (if any)
     */
    if (! validInt.isProperlyContained(qi)) {
      // check if the interval contains any quantums
      if (validInt.getBound(0).isLast())
        return null;
      qi = validInt.getBound(0).next();
      /**
       * if the new value is not contained
       * (this can happen if the interval was of zero width)
       * don't assign a match
       */
      if (! validInt.isProperlyContained(qi)) {
        return null;
      }
    }
    return qi;
  }

  /**
   * Attempt to match the first splitting Node to an endnode or to an internal node.
   * This provides a "clean" beginning for the matched path.
   */
  private void placeFirstNode()
  {
    SplitNode splitNode = splitNodes[0];
    QuantumInterval qInt = new QuantumInterval(null, null);
    placeAtLowestNode(splitNode, qInt);
  }

  /**
   * Attempt to match the last splitting Node to an endnode or to an internal node.
   * This provides a "clean" ending for the matched path.
   */
  private void placeLastNode()
  {
    SplitNode splitNode = splitNodes[splitNodes.length - 1];
    QuantumInterval qInt = new QuantumInterval(splitNodes[0].getPlace(), null);
    placeAtHighestNode(splitNode, qInt);
  }

  /**
   * Match a split node to the lowest node which is in tolerance and in
   * the valid interval.
   *
   * @param splitNode
   * @param qInt interval which match must be contained in
   * @return the match location, if the match was made, or <code>null</code>
   */
  private QuantumIndex placeAtLowestNode(SplitNode splitNode, QuantumInterval validInt)
  {
    for (int i = 0; i < qPath.getFlatPath().getNumNodes(); i++) {
      int nodeIndex = qPath.getFlatPath().getNodeVertexIndex(i);
      if (! validInt.isProperlyContained(nodeIndex))
        continue;
      QuantumIndex matchQI = placeVertex(splitNode, nodeIndex, nodeDistanceTolerance);
      if (matchQI != null) return matchQI;
    }
    return null;
  }

  /**
   * Match a split node to the highest node which is in tolerance and in
   * the valid interval.
   *
   * @param splitNode
   * @param qInt interval which match must be contained in
   * @return the match location, if the match was made, or <code>null</code>
   */
  private QuantumIndex placeAtHighestNode(SplitNode splitNode, QuantumInterval validInt)
  {
    for (int i = qPath.getFlatPath().getNumNodes() - 1; i >= 0; i--) {
      int nodeIndex = qPath.getFlatPath().getNodeVertexIndex(i);
      if (! validInt.isProperlyContained(nodeIndex))
        continue;
      QuantumIndex matchQI = placeVertex(splitNode, nodeIndex, nodeDistanceTolerance);
      if (matchQI != null) return matchQI;
    }
    return null;
  }

  /**
   * Match a split node to a vertex, if they are within tolerance
   *
   * @param splitNode
   * @param vertexIndex
   * @param distanceTolerance
   * @return the match location, if the match was made, or <code>null</code>
   */
  private QuantumIndex placeVertex(SplitNode splitNode,
                                   int vertexIndex,
                                   double distanceTolerance)
  {
    Coordinate splittingPt = splitNode.getSplittingPt();

    Coordinate targetVertex = qPath.getFlatPath().getCoordinate(vertexIndex);
    double dist = splittingPt.distance(targetVertex);
    if (dist < distanceTolerance) {
      QuantumIndex matchQI = qPath.getVertexIndex(vertexIndex);
      splitNode.setPlace(qPath.getVertexIndex(vertexIndex));
      return matchQI;
    }
    return null;
  }


}