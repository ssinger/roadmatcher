package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Places split nodes at their closest point on the split path, if possible.
 * This algorithm maintains the minimum segment length invariant, which
 * means that in some cases node places cannot be changed.
 * <p>
 * This algorithm uses a simple heuristic to decide which nodes can be
 * placed at their closest point without coming too close to neighbour nodes or
 * segment endpoints.  In situations where several splitting points
 * are close together (especially if they are closer than the minimum
 * segment length), this may result in some not being snapped.
 * This should be a rare occurence, however.
 *
 * @version 1.0
 */
public class PlaceAtClosestPoint
{
  private SplitNode[] splitNodes;
  private QuantizedPath qPath;
  private FlatPath flatPath;

  public PlaceAtClosestPoint(SplitPath splitPath)
  {
    splitNodes = splitPath.getSplitNodes();
    qPath = splitPath.getQuantizedPath();
    flatPath = qPath.getFlatPath();
    snap();
  }

  private void snap()
  {
    // nothing to do!
    if (splitNodes.length <= 0)
      return;
    for (int i = 0; i < splitNodes.length; i++) {
      snap(i);
    }
  }

  private void snap(int i)
  {
    SplitNode splitNode = splitNodes[i];
    // don't move if this is placed at a vertex
    if (splitNode.getPlace().isVertex())
      return;
    // don't change if place is different to closest place
    if (! splitNode.isPlacedAtClosest())
      return;

    /**
     * Since this node is not at a vertex,
     * the closest exact location lies
     * in the segment given by the closest location.
     * An important implication of this is that we can check for
     * the distance from other nodes on the segment by a
     * simple point-point distance computation.
     */
    int splitNodeSeg = splitNode.getPlace().getSegmentIndex();
    Coordinate closestExactCoord = splitNode.getClosestExactLocation();

    /**
     * Need to check distance from up to four neighbour points:
     * - the segment endpoints
     * - the neighbouring splitnodes on the segment (if any)
     */
    Coordinate[] coordsToTest= new Coordinate[4];
    coordsToTest[0] = flatPath.getCoordinate(splitNodeSeg);
    if (splitNodeSeg + 1 < flatPath.getNumPoints())
      coordsToTest[1] = flatPath.getCoordinate(splitNodeSeg + 1);
    coordsToTest[2] = findNeighbourLocationInSegment(i - 1, splitNodeSeg);
    coordsToTest[3] = findNeighbourLocationInSegment(i + 1, splitNodeSeg);

    if (isInTolerance(closestExactCoord, coordsToTest, qPath.getMinimumSegmentLength())) {
      splitNode.placeAtClosestExactLocation();
    }
  }

  private Coordinate findNeighbourLocationInSegment(int neighbourSplitNodeIndex, int segmentIndex)
  {
    if (neighbourSplitNodeIndex >= 0
        && neighbourSplitNodeIndex < splitNodes.length
        && splitNodes[neighbourSplitNodeIndex].getPlace().getSegmentIndex() == segmentIndex )
      return splitNodes[neighbourSplitNodeIndex].getSplitCoordinate();
    return null;
  }

  private static boolean isInTolerance(Coordinate coord, Coordinate[] coordsToTest, double minDistance)
  {
    for (int i = 0; i < coordsToTest.length; i++) {
      if (coordsToTest[i] != null
          && coordsToTest[i].distance(coord) < minDistance)
        return false;
    }
    return true;
  }
}