package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jcs.debug.Debug;

/**
 * Allows a {@link LinearPath} to be split at node locations,
 * according to optimality and correctness heuristics.
 *
 * <h2>Validity constraints</h2>
 * In order to produce well-formed topology in split paths, the path
 * must satisfy the following validity constraints.
 *
 * <ol>
 * <li>splits are at strictly increasing points along the path
 * ( this implies that no two consecutive splits are at the same point)
 * <li>[optional] splits do not create sections or line segments
 * shorter than the segment length tolerance
 * </ol>
 *
 * In addition, other heuristic constraints on splitnode location
 * are used:
 * <ul>
 * <li>Splits should be positioned at existing nodes, if they are close enough
 * <li>The ends of paths are more significant than the middle,
 * so endnodes should be matched first if possible, followed by nodes further
 * towards the middle of the paths
 * <li>Splits should be positioned at existing vertices,
 * if they are close enough (e.g. within the segment length tolerance)
 * <li>otherwise, splits should be positioned at the point closest to
 * the original split node.
 * </ul>
 *
 * @version 1.0
 */

public class SplitPath
{
  private LinearPath path;
  private double nodeDistanceTolerance;
  private double minimumSegmentLength;
  private QuantizedPath qPath;
  private boolean isValid = true;

  private SplitNodeList splitNodes;

  /**
   * a list of the {@link SplitEdge}s in this path
   */
  private List splitEdges = null;

  public SplitPath(LinearPath path)
  {
    this.path = path;
  }

  public SplitNode[] getSplitNodes() { return splitNodes.getSplitNodes(); }

  public QuantizedPath getQuantizedPath() { return qPath; }

  public List getSplitEdges() { return splitEdges; }

  /**
   * Splits this path at the argument nodes.
   *
   * @param splittingNodes the points at which to split this path
   * @param distanceTolerance the match distance tolerance
   * @param minSegmentLen
   */
  public void split(Coordinate[] splittingNodes,
                    double nodeDistanceTolerance,
    double minimumSegmentLength)
  {
    this.nodeDistanceTolerance = nodeDistanceTolerance;
    this.minimumSegmentLength = minimumSegmentLength;
    qPath = new QuantizedPath(path, minimumSegmentLength);

    buildSplitNodes(splittingNodes, nodeDistanceTolerance);
//    if (splitNodes.getSplitNodes().length < 2) {
//      setInvalid("too few split nodes");
//      return;
//    }

    placeSplitNodes();
    if (! isValid)
      return;

    SplitEdgesBuilder splitEdgesBuilder = new SplitEdgesBuilder(path.getGeometryFactory());
    splitEdges = splitEdgesBuilder.buildEdges(this);
  }

  private void buildSplitNodes(Coordinate[] splittingNodes,
                    double nodeDistanceTolerance)
  {
    NodesOnPath nop = new NodesOnPath(splittingNodes, qPath, nodeDistanceTolerance);
    splitNodes = new SplitNodeList(nop.getNodesOnPath());
  }

  private void placeSplitNodes()
  {
    new PlaceForward(this, nodeDistanceTolerance);
    if (! splitNodes.isLastMatched()) {
      setInvalid("last splitnode is unmatched after PlaceForward");
      return;
    }
    if (! splitNodes.isMatchingContiguousExceptEnd()) {
      setInvalid("discontiguous matching after PlaceForward");
      return;
    }
    // this is required, to place any nodes close to the end of the splitpath
    new PlaceBackward(getSplitNodes());
    if (! splitNodes.isMatchingComplete()) {
      setInvalid("matching is not complete");
      return;
    }
    // this heuristic is optional (but recommended)
    new SnapToNodes(this, nodeDistanceTolerance);
    // this heuristic is optional (but recommended)
    new PlaceAtClosestPoint(this);
    // this should never happen - check could be removed
    if (! splitNodes.isSplitLocationsStrictlyIncreasing()) {
      setInvalid("found non-increasing split locations");
      return;
    }

  }

  private void setInvalid(String msg)
  {
    isValid = false;
    Debug.println("INVALID SPLIT: " + msg);
  }

  /**
   * Tests whether this split path has been split in a valid manner.
   *
   * @return <code>true</code> if the split path is valid
   *
   */
  public boolean isSplitValid()
  {
    return isValid;
  }

  public Geometry getGeometry()
  {
    if (splitEdges == null)
      return path.getGeometry();
    GeometryFactory geomFact = path.getGeometryFactory();
    List lines = new ArrayList();
    for (Iterator i = splitEdges.iterator(); i.hasNext(); ) {
      SplitEdge edge = (SplitEdge) i.next();
      lines.add(edge.getGeometry());
    }
    return geomFact.createMultiLineString(geomFact.toLineStringArray(lines));
  }

  public void printSplitEdgesAttributes()
  {
    for (Iterator i = splitEdges.iterator(); i.hasNext(); ) {
      SplitEdge e = (SplitEdge) i.next();
      System.out.println("splitedge[" + e.getEdgeIndex() + "] "
                         + " src edge: " + e.getSourceEdgeIndex()
                         + " splitting edge: " + e.getSplittingEdgeIndex());
    }
  }
}