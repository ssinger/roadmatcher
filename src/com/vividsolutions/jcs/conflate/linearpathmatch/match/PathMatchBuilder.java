package com.vividsolutions.jcs.conflate.linearpathmatch.match;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;

/**
 * Builds a path match by buffering directed edges which start at nodes which
 * are within a given distance tolerance.
 *
 * @version 1.0
 */
public class PathMatchBuilder
{
  private double distanceTolerance = 0.0;

  private CandidatePath[] candidatePath = new CandidatePath[2];
  private PathMatch match;
  private boolean isComputed = false;

  /**
   * Contains the indexes for the subMatches established during
   * the path matching.  Each entry is an int[2] containing
   * the count of PathEdges in each matched subpath
   */
  private List subMatches = new ArrayList();

  public PathMatchBuilder(PathTracer pathTracer0, PathTracer pathTracer1)
  {
    candidatePath[0] = new CandidatePath(pathTracer0);
    candidatePath[1] = new CandidatePath(pathTracer1);
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  public boolean hasMatch() {
    getMatch();
    return match != null;
  }

  public PathMatch getMatch()
  {
    computeMatch();
    return match;
  }

  private void computeMatch()
  {
    if (isComputed) return;
    isComputed = true;
    candidatePath[0].setDistanceTolerance(distanceTolerance);
    candidatePath[1].setDistanceTolerance(distanceTolerance);
    // a candidate path could be empty, in which case there is no match
    if (candidatePath[0].isEmpty()) return;
    if (candidatePath[1].isEmpty()) return;

    matchPaths();
    // check if a non-null match was found
    if (candidatePath[0].hasEdges() && candidatePath[1].hasEdges())
      match = new PathMatch(candidatePath[0].getPath(),
                            candidatePath[1].getPath(), subMatches);
  }


  /**
   * Builds a PathMatch based on the maximum separation distance tolerance.
   * The initial points must be within distanceTolerance of each other.
   */
  private void matchPaths()
  {
    // true if path[i] is matched by part of path[1 - i]
    boolean[] isMatched = new boolean[2];

    // paths only match if start points are within distance tolerance
    if (candidatePath[0].getGeometry().getCoordinateN(0).distance(
        candidatePath[1].getGeometry().getCoordinateN(0) )
        > distanceTolerance)
    {
      return;
    }
    // extend paths as long as they match and are "reasonable"
    while (true) {
      // check if paths are within distance tolerance of each other
      isMatched[1] = candidatePath[0].hasSubpathMatching(candidatePath[1]);
      isMatched[0] = candidatePath[1].hasSubpathMatching(candidatePath[0]);

      /**
       * If neither is contained, paths are incompatible
       * (i.e. have veered away from each other).
       * The match is now as long as possible, so exit
       */
      if (! (isMatched[0] || isMatched[1]))
          break;

      // extend contained paths, if possible
      boolean isExtended = false;
      if (isMatched[0]) {
        candidatePath[0].commit();
        candidatePath[1].commit();
        isExtended |= extend(0);
      }
      if (isMatched[1]) {
        candidatePath[0].commit();
        candidatePath[1].commit();
        isExtended |= extend(1);
      }

      /**
       * At this point at least one path contains the other,
       * and the path(s) has been commited.
       * Save this configuration as a valid match
       */
      saveSubPath();

      /**
       * If neither candidate was extended then one of them is blocked.
       * It is not possible to extend the match further, so exit
       */
      if (! isExtended)
        break;
    }
  }

  /**
   * Extends the given path, if possible
   * and the additional linestring is reasonable.
   *
   * @param pathIndex
   * @return <code>true</code> if the path was extended
   */
  private boolean extend(int pathIndex)
  {
    LinearEdge nextEdge = candidatePath[pathIndex].lookahead();
    if (nextEdge == null) return false;
    if (isValidExtension(nextEdge))
      return candidatePath[pathIndex].extend();
    return false;
  }

  /**
   * Tests whether the argument is a geometrically reasonable extension
   * to a candidate path.
   * @param edge
   * @return
   */
  private boolean isValidExtension(LinearEdge edge)
  {
    LineString line = edge.getGeometry();
    Coordinate[] pts = line.getCoordinates();
    // line must not be a loop
    if (pts[0].equals(pts[pts.length - 1]))
      return false;

    // could check to see that line does not loop back to within distanceTolerance of itself?

    // otherwise, line is ok as an extension
    return true;
  }

  private void saveSubPath()
  {
    int[] subMatch = new int[]
    {
      candidatePath[0].getPath().size(),
      candidatePath[1].getPath().size()
    };
    subMatches.add(subMatch);
  }

}