package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.LinearPath;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jcs.debug.Debug;

/**
 * Splits two matched @link LinearPaths} according to
 * their respective noding,
 * and matches the resulting {@link SplitPaths}.
 *
 * @version 1.0
 */
public class SplitPathMatcher
{
  private double distanceTolerance = 0.0;
  private double segmentLengthTolerance = 0.0;

  private LinearPath[] path = new LinearPath[2];
  // the resulting split paths
  private SplitPath[] splitPath = null;

  public SplitPathMatcher(LinearPath path0, LinearPath path1) {
    path[0] = path0;
    path[1] = path1;
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  public void setSegmentLengthTolerance(double segmentLengthTolerance)
  {
    this.segmentLengthTolerance = segmentLengthTolerance;
  }

  public SplitPath[] getSplitPaths()
  {
    if (splitPath == null)
      computeSplitPaths();

    return splitPath;
  }

  private void computeSplitPaths()
  {
    splitPath = new SplitPath[2];
    splitPath[0] = new SplitPath(path[0]);
    splitPath[1] = new SplitPath(path[1]);
    splitPath[0].split(path[1].getNodes(), distanceTolerance, segmentLengthTolerance);
    splitPath[1].split(path[0].getNodes(), distanceTolerance, segmentLengthTolerance);

    /**
     * The matching assumes the split is valid, so check it first
     */
    if (! (splitPath[0].isSplitValid() &&
        splitPath[1].isSplitValid()))
      return;

    updateInitialUnknownSplittingEdges(splitPath[0], splitPath[1]);
    updateInitialUnknownSplittingEdges(splitPath[1], splitPath[0]);

//    System.out.println("path 0 = " + splitPath[0].getGeometry());
//    splitPath[0].printSplitEdgesAttributes();
//    System.out.println("path 1 = " + splitPath[1].getGeometry());
//    splitPath[1].printSplitEdgesAttributes();

    // match the split edges created by the splitting
    matchSplitEdges();
  }

  /**
   * Attempts to resolve any unknown splitting edge indexes in the split edges
   * in the start of the list for the argument splitpath.
   * Splitting edge indexes are unknown if the start node is
   * a path node which did not match to a splitting node and is not contained
   * in a splitting edge.  This can happen because:
   * <ol>
   * <li>The node is on portion of the orginal path
   * which did not match to any portion of the splitting path
   * <li>The node did match to the splitting path
   * but in the middle of a long splitting edge, so that it could not
   * be matched to a node on the splitting edge.
   * </ol>
   * This method attempts to find the edge which contains this node as a splitting
   * node on the matching path.  This can be done in Case 2, which determines
   * the splitting edge.  In Case 1 the node will be marked as UNMATCHED.
   *
   * @param splitPath
   * @param splittingEdges
   */
  private void updateInitialUnknownSplittingEdges(SplitPath splitPath0, SplitPath splitPath1)
  {
    List splittingEdges = splitPath1.getSplitEdges();
    List targetEdges = splitPath0.getSplitEdges();
    for (Iterator i = targetEdges.iterator(); i.hasNext(); ) {
      SplitEdge targetEdge = (SplitEdge) i.next();
      if (targetEdge.getSplittingEdgeIndex() != SplitEdge.UNKNOWN)
        return;

      int sourceEdgeIndex = targetEdge.getSourceEdgeIndex();
      int splittingEdgeIndex = getFirstMatchingSplittingEdge(sourceEdgeIndex, splittingEdges);
      if (splittingEdgeIndex < 0) {
        splittingEdgeIndex = SplitEdge.UNMATCHED;
      }
      targetEdge.setSplittingEdgeIndex(splittingEdgeIndex);
    }
  }

  private int getFirstMatchingSplittingEdge(int edgeIndex, List splittingEdges)
  {
    for (int i = 0; i < splittingEdges.size(); i++) {
      SplitEdge splittingEdge = (SplitEdge) splittingEdges.get(i);
      if (splittingEdge.getOriginalSplittingEdgeIndex() == edgeIndex) {
        return splittingEdge.getSourceEdgeIndex();
      }
    }
    return -1;
  }
  /**
   * Matches the split edges in the paths.
   * One path may unmatched edges at its end,
   * and one may have unmatched edges at the start
   * (but they must not be the same path).
   * Apart from that, the split edges should match one-to-one sequentially.
   */
  private void matchSplitEdges()
  {
    int matchEdgeCount = 0;
    // initially we may not be matching edges, if one path is longer than the other
    List edges0 = splitPath[0].getSplitEdges();
    List edges1 = splitPath[1].getSplitEdges();
    int i0 = 0;
    int i1 = 0;
    // skip unmatched edges at start
    while (i0 < edges0.size()
           && ((SplitEdge) edges0.get(i0)).getSplittingEdgeIndex() == SplitEdge.UNMATCHED) {
      i0++;
    }
    while (i1 < edges1.size()
           && ((SplitEdge) edges1.get(i1)).getSplittingEdgeIndex() == SplitEdge.UNMATCHED) {
      i1++;
    }

    while (i0 < edges0.size() && i1 < edges1.size()) {
      SplitEdge edge0 = (SplitEdge) edges0.get(i0);
      SplitEdge edge1 = (SplitEdge) edges1.get(i1);

        match(edge0, edge1);
        i0++;
        i1++;
        matchEdgeCount++;
    }
    if (matchEdgeCount == 0)
      Debug.println("ERROR - found zero match count!");
  }

  private void match(SplitEdge edge0, SplitEdge edge1)
  {
    edge0.setMatch(edge1);
    edge1.setMatch(edge0);

    // Verify the correctness of the split edge indices
    boolean correctMatch01 = edge0.getSplittingEdgeIndex() == edge1.getSourceEdgeIndex();
    boolean correctMatch10 = edge1.getSplittingEdgeIndex() == edge0.getSourceEdgeIndex();
    if (! (correctMatch01 && correctMatch10)) {

      Debug.println("Src paths:");
      Debug.println(splitPath[0].getQuantizedPath().getFlatPath().getPath().getGeometry());
      Debug.println(splitPath[1].getQuantizedPath().getFlatPath().getPath().getGeometry());

      String matchIndexStr = null;
      if (! correctMatch01) {
        matchIndexStr = "path 0 splitedge " + edge0.getEdgeIndex()
                      + " - "
                      + "splitting: " + edge0.getSplittingEdgeIndex()
                      + ","
                      + "other src: " + edge1.getSourceEdgeIndex();
      }
      else {
        matchIndexStr = "path 1 splitedge " + edge1.getEdgeIndex()
                      + " : "
                      + "splitting: " + edge1.getSplittingEdgeIndex()
                      + ","
                      + "other src: " + edge0.getSourceEdgeIndex();
      }
      Debug.println("found matched split edge parent index mismatch: "
                         + matchIndexStr);
      Debug.println(splitPath[0].getGeometry());
      Debug.println(splitPath[1].getGeometry());
    }
  }

  private boolean checkGeometryMatch(SplitEdge edge0, SplitEdge edge1)
  {
    Geometry line0 = edge0.getGeometry();
    Geometry line1 = edge1.getGeometry();
    boolean match = line0.buffer(distanceTolerance).contains(line1)
                    && line1.buffer(distanceTolerance).contains(line0);

    Debug.println("geometry match = " + match);

    System.out.println(line0);
    System.out.println(line1);
    return match;
  }

  public boolean isValid()
  {
    getSplitPaths();
    if (! splitPath[0].isSplitValid()) return false;
    if (! splitPath[1].isSplitValid()) return false;

//    if (! splitPath[0].isMatchValid()) return false;
//    if (! splitPath[1].isMatchValid()) return false;

    return true;
  }


  public Geometry getGeometry()
  {
    return getLines();
    //return getMatchInds();
  }

  public Geometry getLines()
  {
    getSplitPaths();
    Geometry splitGeom0 = splitPath[0].getGeometry();
    Geometry splitGeom1 = splitPath[1].getGeometry();
    Geometry geom = splitGeom0.getFactory().createGeometryCollection(
        new Geometry[] { splitGeom0, splitGeom1 } );

    return geom;
  }

}