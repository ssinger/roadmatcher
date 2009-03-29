package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.*;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Determines the split nodes which will be actually be used to split a path.
 * The heuristic used is to find the extremal points(s) in the list of splitting
 * points which are within the distance tolerance of the path to be split.
 * They define the range of splitting points which will be used to split the path.
 *
 * @version 1.0
 */
public class NodesOnPath {

  private Coordinate[] splitPts;
  private SplitNode[] candidateSplitNode;
  private double[] distanceFromPath;
  private QuantizedPath qPath;
  private double nodeDistanceTolerance;
  private List splitNodes;

  public NodesOnPath(Coordinate[] splitPts, QuantizedPath qPath, double nodeDistanceTolerance)
  {
    this.splitPts = splitPts;
    this.qPath = qPath;
    this.nodeDistanceTolerance = nodeDistanceTolerance;
    computeNodes();
  }

  public List getNodesOnPath()
  {
    return splitNodes;
  }

  /**
   * Computes all nodes which lie on the path.
   */
  private void computeNodes()
  {
    candidateSplitNode = new SplitNode[splitPts.length];
    distanceFromPath = new double[splitPts.length];
    findCandidateSplitNodes();

    int firstIndexOnPath = 0;
    for (int i = 0; i < splitPts.length; i++) {
      if (distanceFromPath[i] < nodeDistanceTolerance) {
        firstIndexOnPath = i;
        break;
      }
    }
    int lastIndexOnPath = 0;
    for (int i = splitPts.length - 1; i >= 0; i--) {
      if (distanceFromPath[i] < nodeDistanceTolerance) {
        lastIndexOnPath = i;
        break;
      }
    }
    splitNodes = new ArrayList();
    for (int i = firstIndexOnPath; i <= lastIndexOnPath; i++) {
      splitNodes.add(candidateSplitNode[i]);
    }
  }

  private void findCandidateSplitNodes()
  {
    Coordinate[] closestPt = new Coordinate[1];
    QuantumIndex highestQI = null;
    /**
     * Find split nodes for all points, ensuring their locations are non-decreasing
     */
    for (int i = 0; i < splitPts.length; i++) {
      Coordinate splittingPt = splitPts[i];
      QuantumIndex closestQI = qPath.findClosestQuantum(splittingPt, highestQI, closestPt);
      distanceFromPath[i] = splittingPt.distance(closestQI.getCoordinate());
      highestQI = closestQI;
      candidateSplitNode[i] = new SplitNode(i, splittingPt, closestQI, closestPt[0]);
    }
  }

  private void OLDfindNodesNearPath()
  {
    boolean foundNodeOnPath = false;
    boolean prevNodeOnPath = false;
    int i = 0;
    Coordinate[] closestPt = new Coordinate[1];
    QuantumIndex highestQI = null;
    /**
     * Once a node on the path is found, stop as soon as another is found which is not on the path
     */
    while (i < splitPts.length && (! foundNodeOnPath || prevNodeOnPath)) {
      Coordinate splittingPt = splitPts[i];
      QuantumIndex closestQI = qPath.findClosestQuantum(splittingPt, highestQI, closestPt);
      boolean isOnPath = splittingPt.distance(closestQI.getCoordinate()) <= nodeDistanceTolerance;
      if (isOnPath) {
        SplitNode splitNode = new SplitNode(i, splittingPt, closestQI, closestPt[0]);
        splitNodes.add(splitNode);
        highestQI = closestQI;
        foundNodeOnPath = true;
      }
      i++;
      prevNodeOnPath = isOnPath;
    }
  }
/*
    private void OLDcomputeNodes()
  {
    splitNodes = new ArrayList();
    int firstIndexOnPath = findFirstIndexOnPath();
    int lastIndexOnPath = findLastIndexOnPath();
    if (firstIndexOnPath < 0)
      return;

    Coordinate[] closestPt = new Coordinate[1];
    for (int i = firstIndexOnPath; i <= lastIndexOnPath; i++) {
     // compute the point on the splitpath closest to the splitting point
      Coordinate splittingPt = splitPts[i];
      QuantumIndex closestQI = qPath.findClosestQuantum(splittingPt, closestPt);
      SplitNode splitNode = new SplitNode(i, splittingPt, closestQI, closestPt[0]);
      splitNodes.add(splitNode);
    }
  }


  private int findFirstIndexOnPath()
  {
    int firstIndexOnPath = 0;
    for (int i = 0; i < splitPts.length; i++) {
     // compute the point on the splitpath closest to the splitting point
      Coordinate splittingPt = splitPts[i];
      QuantumIndex closestQI = qPath.findClosestQuantum(splittingPt);
      boolean isOnPath = splittingPt.distance(closestQI.getCoordinate()) <= nodeDistanceTolerance;
      if (isOnPath) {
        return i;
      }
    }
    return -1;
  }


  private int findLastIndexOnPath()
  {
    int lastIndexOnPath = 0;
    for (int i = splitPts.length - 1; i >= 0; i--) {
     // compute the point on the splitpath closest to the splitting point
      Coordinate splittingPt = splitPts[i];
      QuantumIndex closestQI = qPath.findClosestQuantum(splittingPt);
      boolean isOnPath = splittingPt.distance(closestQI.getCoordinate()) <= nodeDistanceTolerance;
      if (isOnPath) {
        return i;
      }
    }
    return -1;
  }
  */
}