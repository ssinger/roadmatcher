package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.List;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.match.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jcs.debug.*;

/**
 * Given a PathMatch, determines the splits in each path and matches the resulting
 * split paths together.  Allows utilizing submatches (if present) to
 * avoid splitting errors.
 *
 * @version 1.0
 */
public class PathZipper
{

  private static LinearPath[] getPaths(LinearPath[] path, int[] subPathSize)
  {
    return new LinearPath[]
    {
      sameOrSmallerPath(path[0], subPathSize[0]),
      sameOrSmallerPath(path[1], subPathSize[1]),
    };
  }

  /**
   * Optimize creation of smaller LinearPaths, by returning the original
   * if the size is the same.
   *
   * @param path the LinearPath to make smaller
   * @param subPathSize the size of the desired subpath
   * @return the same or a different smaller LinearPath
   */
  private static LinearPath sameOrSmallerPath(LinearPath path, int subPathSize)
  {
    if (path.size() == subPathSize)
      return path;
    else
      return new LinearPath(path, subPathSize);
  }

  private double distanceTolerance = 0.0;
  private double segmentLengthTolerance = 0.0;
  private boolean useSubmatches = true;

  private PathMatch pathMatch;
  private boolean isComputed = false;
  private SplitPath[] splitPaths;
  private SplitPathMatcher splitter;

  public PathZipper(PathMatch pathMatch)
  {
//    Debug.println(pathMatch.getGeometry());
    this.pathMatch = pathMatch;
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  public void setSegmentLengthTolerance(double segmentLengthTolerance)
  {
    this.segmentLengthTolerance = segmentLengthTolerance;
  }

  public void setUseSubmatches(boolean useSubmatches)
  {
    this.useSubmatches = useSubmatches;
  }
  public boolean isValid()
  {
    compute();
    return splitPaths != null;
  }

  public SplitPath[] getSplitPaths()
  {
    compute();
    return splitPaths;
  }

  public Geometry getGeometry()
  {
    if (splitter == null) return null;
    return splitter.getGeometry();
  }

  private static final String SPLIT = "PathSplit";

  private void compute()
  {
    if (isComputed) return;
    isComputed = true;

    if (useSubmatches)
      computeUsingSubmatches();
    else
      computeFullMatchOnly();
  }

  private void computeFullMatchOnly()
  {
    splitter = split(pathMatch.getPaths());
    if (! splitter.isValid())
      return;
    splitPaths = splitter.getSplitPaths();
  }

  private void computeUsingSubmatches()
  {
    List subMatchIndex = pathMatch.getSubmatches();
    int subMatchi = subMatchIndex.size() - 1;
    splitter = null;
    while (true) {
      splitter = null;
      if (subMatchi < 0)
        break;
      int[] subMatch = (int[]) subMatchIndex.get(subMatchi--);
      LinearPath[] path = getPaths(pathMatch.getPaths(), subMatch);
      splitter = split(path);
      if (splitter.isValid()) {
        break;
      }
      splitter = null;
    }

    if (splitter == null)
      return;

    splitPaths = splitter.getSplitPaths();
  }

  private SplitPathMatcher split(LinearPath[] path)
  {
    SplitPathMatcher splitter = new SplitPathMatcher(path[0], path[1]);
    splitter.setDistanceTolerance(distanceTolerance);
    splitter.setSegmentLengthTolerance(segmentLengthTolerance);

    if (! splitter.isValid()) {
      Debug.println("INVALID Split   Paths:");
      Debug.println(splitter.getGeometry());
    }
    else {
//      Debug.println(splitter.getGeometry());
      //DebugFeature.add(SPLIT, splitter.getGeometry(), "");
    }
    return splitter;
  }
}