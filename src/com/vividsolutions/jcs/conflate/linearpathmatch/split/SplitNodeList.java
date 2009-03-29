package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;

/**
 * Represents the split nodes induced by a
 * set of splitting nodes on a split path.
 * Every node in this list should be known to be on the path,
 * as determined by the distance tolerance.
 *
 * @version 1.0
 */
public class SplitNodeList
{
  private SplitNode[] splitNodes;

  public SplitNodeList(List splitNodeList)
  {
    splitNodes = (SplitNode[]) splitNodeList.toArray(new SplitNode[0]);
  }

  /**
   * Gets the list of the split nodes which actually fall on the path.
   * (This is a contiguous sequence).
   *
   * @return an array of the split nodes which fall on the path
   */
  public SplitNode[] getSplitNodes()
  {
    return splitNodes;
  }

  /**
   * Computes the array of splitNodes which are in range of the split path.
   */
  /*
  private void computeNodesOnPath()
  {
    int seqStart = -1;
    for (int i = 0; i < splitNodes.length; i++) {
      if (splitNodes[i].isOnPath()) {
        seqStart = i;
        break;
      }
    }

    int seqEnd = -1;
    for (int i = splitNodes.length - 1; i >= 0; i--) {
      if (splitNodes[i].isOnPath()) {
        seqEnd = i;
        break;
      }
    }
    if (seqStart < 0 || seqEnd < 0) {
      nodesOnPath = new SplitNode[0];
      return;
    }
    nodesOnPath = new SplitNode[seqEnd - seqStart + 1];
    int seqi = 0;
    for (int j = seqStart; j <= seqEnd; j++) {
      nodesOnPath[seqi++] = splitNodes[j];
    }
  }
  */

  /**
   * Test whether the matched nodes are all contiguous
   * (except possibly for the end node).
   */
  public boolean isMatchingContiguousExceptEnd()
  {
    boolean foundUnmatched = false;
    for (int i = 0; i < splitNodes.length - 1; i++) {
      if (! splitNodes[i].isPlaced())
        foundUnmatched = true;
      else {
        if (foundUnmatched == true)
          return false;
      }
    }
    return true;
  }
  /**
   * Tests whether every node is matched
   */
  public boolean isLastMatched()
  {
    if (splitNodes.length < 1)
      return true;
    return splitNodes[splitNodes.length - 1].isPlaced();
  }

  /**
   * Tests whether every node is matched
   */
  public boolean isMatchingComplete()
  {
    for (int i = 0; i < splitNodes.length; i++) {
      if (! splitNodes[i].isPlaced())
        return false;
    }
    return true;
  }

  public boolean isSplitLocationsStrictlyIncreasing()
  {
    QuantumIndex lastLocation = null;
    for (int i = 0; i < splitNodes.length; i++) {
      if (! splitNodes[i].isPlaced()) {
        continue;
      }
      QuantumIndex qi = splitNodes[i].getPlace();
      if (lastLocation != null) {
        //System.out.println(qi);
        if (qi.compareTo(lastLocation) <= 0)
          return false;
      }
      lastLocation = qi;
    }
    return true;
  }

}