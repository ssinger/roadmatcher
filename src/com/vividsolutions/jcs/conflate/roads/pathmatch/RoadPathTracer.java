package com.vividsolutions.jcs.conflate.roads.pathmatch;

import java.util.HashSet;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.linearpathmatch.LinearEdge;
import com.vividsolutions.jcs.conflate.linearpathmatch.PathTracer;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.conflate.roads.model.RoadNode;
import com.vividsolutions.jcs.conflate.roads.model.RoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.graph.Node;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.Angle;

/**
 * Traces a path through a {@link RoadNetwork}
 * starting at a given {@link DirectedEdge}
 * and creates @link LinearEdges corresponding to the path edges.
 * The path will only contain edges with a state of UNKNOWN.
 * The path will be traced through junctions as long as
 * there is a "nearly straight" path to follow
 * (within a defined angle tolerance).
 *
 * @version 1.0
 */
public class RoadPathTracer
    implements PathTracer
{
  public static final double MAX_STRAIGHTNESS_ANGLE_TOLERANCE = Math.PI / 6;

  private boolean isFirst = true;
  private DirectedEdge currDE;
  private HashSet visited=new HashSet();

  public RoadPathTracer(DirectedEdge de)
  {
    currDE = de;
  }

  public static LinearEdge toLinearEdge(DirectedEdge de)
  {
    LineString orientedLine = ((RoadSegment) de.getEdge()).getLine();
    if (! de.getEdgeDirection())
      orientedLine = LineStringUtil.reverse(orientedLine);
    return new LinearEdge(orientedLine, de);
  }

  /**
   * Finds the next edge in the candidate match path
   *
   * @return the next edge in this path, or
   *   <code>null</code> if no more edges can be traced in this path
   */
  public LinearEdge findNextEdge()
  {
    DirectedEdge de = findNextUnknownEdge();
    if (de == null)
      return null;
    return toLinearEdge(de);
  }

  private DirectedEdge findNextUnknownEdge()
  {
    DirectedEdge de = findNextGraphEdge();
    if (de != null) {
      SourceRoadSegment rs = (SourceRoadSegment) de.getEdge();
      if (rs.getState() != SourceState.UNKNOWN)
        return null;
    }
    return de;
  }

  private DirectedEdge findNextGraphEdge()
  {
    if (isFirst) {
      isFirst = false;
      return currDE;
    }
    // nothing to work with!
    if (currDE == null) return null;
    RoadNode endNode = (RoadNode) currDE.getToNode();
    // obviously can't extend a dead end
    if (endNode.getDegree() < 2)
      return null;


    DirectedEdge candidate = null;
    if (endNode.getDegree() == 2) {
      candidate = endNode.getOutEdges().getNextEdge(currDE.getSym());
    }
    else {  // degree is >= 3
      // don't extend past matched nodes
      if (endNode.hasMatch())
        return null;

      candidate = findNearlyStraightCandidate(currDE);
    }
    if(visited.contains(candidate)) {
    	return null;
    }
    else
    {
    	visited.add(candidate);
    }
    
    currDE = candidate;
    return currDE;
  }

  private DirectedEdge findNearlyStraightCandidate(DirectedEdge de)
  {
    DirectedEdge sym = de.getSym();
    double oppAngle = Angle.normalize(sym.getAngle() + Math.PI);
    Node node = de.getToNode();
    DirectedEdge nearlyStraightCandidate = findNearlyStraightCandidate(node, oppAngle);
    // The candidate selected obviously must be different to the input edge
    Assert.isTrue(nearlyStraightCandidate == null || nearlyStraightCandidate != sym, "candidate for nearly-straight path is same as input");
    return nearlyStraightCandidate;
  }

  public static DirectedEdge findNearlyStraightCandidate(Node node, double oppAngle) {
    double minDelta = 0.0;
    DirectedEdge oppDE = null;
    for (Iterator i = node.getOutEdges().iterator(); i.hasNext(); ) {
      DirectedEdge candidateDE = (DirectedEdge) i.next();
      double candidateAngle = candidateDE.getAngle();
      double angDelta = Angle.diff(oppAngle, candidateAngle);
      if (oppDE == null || angDelta < minDelta) {
        oppDE = candidateDE;
        minDelta = angDelta;
      }
    }
    Assert.isTrue(oppDE != null);    

    if (minDelta < MAX_STRAIGHTNESS_ANGLE_TOLERANCE)
      return oppDE;
    return null;
  }
}