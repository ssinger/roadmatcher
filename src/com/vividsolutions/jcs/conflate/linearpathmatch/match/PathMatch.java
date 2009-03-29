package com.vividsolutions.jcs.conflate.linearpathmatch.match;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;

public class PathMatch {

  private LinearPath[] path = new LinearPath[2];
  /**
   * Contains the indexes for the subMatches established during
   * the path matching.  Each entry is an int[2].
   * The entries in the list are in increasing order of size.
   * The last entry in the list is the entire match.
   */
  private List subMatches;

  public PathMatch(LinearPath path0, LinearPath path1)
  {
    path[0] = path0;
    path[1] = path1;
    // since no subMatches were provided, just create the single
    // subMatches corresponding to the entire paths
    subMatches = new ArrayList();
    subMatches.add(new int[] {
      path[0].size(),
      path[1].size()
    } );
  }

  public PathMatch(LinearPath path0, LinearPath path1, List subMatches)
  {
    path[0] = path0;
    path[1] = path1;
    this.subMatches = subMatches;
  }

  public LinearPath[] getPaths() { return path; }
  public LinearPath getPath(int index) { return path[index]; }

  /**
   * Return the list of submatches for this path match, if any.
   *
   * @return a {@link List} of <code>int[2]</code>
   */
  public List getSubmatches() { return subMatches; }

  public Geometry getGeometry() {
    LineString line0 = path[0].getLinearGeometry();
    LineString line1 = path[1].getLinearGeometry();
    return line0.getFactory().createMultiLineString(new LineString[] { line0, line1 });
  }
}