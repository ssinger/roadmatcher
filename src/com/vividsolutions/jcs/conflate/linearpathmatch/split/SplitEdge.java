package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;

/**
 * An edge of a the final geometry of a {@link SplitPath}.
 * This edge may correspond to either:
 * <ul>
 * <li> a section of an edge that has been split
 * <li> an original edge which has not been split
 * </ul>
 *
 * @version 1.0
 */
public class SplitEdge
{
  public static final int UNKNOWN = -999;
  public static final int UNMATCHED = -900;

  private LinearEdge parent;
  private Geometry line;
  private int edgeIndex;
  private int sourceEdgeIndex;
  private int splittingEdgeIndex;
  private int originalSplittingEdgeIndex;
  private SplitEdge matchEdge;

  public SplitEdge(int edgeIndex, LinearEdge parent, Geometry line, int sourceEdgeIndex, int splitterEdgeIndex)
  {
    this.edgeIndex = edgeIndex;
    this.parent = parent;
    this.line = line;
    this.sourceEdgeIndex = sourceEdgeIndex;
    this.splittingEdgeIndex = splitterEdgeIndex;
    originalSplittingEdgeIndex = splitterEdgeIndex;
  }

  public LinearEdge getParent() { return parent; }

  public Geometry getGeometry() { return line; }

  public int getEdgeIndex() { return edgeIndex; }

  /**
   * Returns the path index of the edge which caused this split edge to be created.
   * Maybe be out of range of the splitter edges array,
   * if this edge is before or after the splitting path.
   *
   * @return the splitter edge index
   */
  public int getSplittingEdgeIndex() { return splittingEdgeIndex; }

  public void setSplittingEdgeIndex(int splittingEdgeIndex)
  {
    this.splittingEdgeIndex = splittingEdgeIndex;
  }

  public int getOriginalSplittingEdgeIndex() { return originalSplittingEdgeIndex; }

  public int getSourceEdgeIndex() { return sourceEdgeIndex; }

  public void setMatch(SplitEdge matchEdge)
  {
    this.matchEdge = matchEdge;
  }
  public SplitEdge getMatch() { return matchEdge; }
  public boolean hasMatch() { return matchEdge != null; }
}