package com.vividsolutions.jcs.conflate.roads.match;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.*;
import com.vividsolutions.jts.index.strtree.*;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jcs.algorithm.*;

public class RoadSegmentsMutualBestMatcher {

  public static double MAX_LENGTH_DIFF_PERCENT = 0.50;

  private static void buildRoadEdgeIndex(Iterator edgeIt, SpatialIndex index)
  {
    while (edgeIt.hasNext()) {
      RoadSegment edge = (RoadSegment) edgeIt.next();
      index.insert(edge.getLine().getEnvelopeInternal(), edge);
    }
  }

  public static List query(RoadSegment edge, double queryBufferDistance, SpatialIndex index)
  {
    Envelope queryEnv = EnvelopeUtil.expand(
        edge.getLine().getEnvelopeInternal(),
        queryBufferDistance);

    return index.query(queryEnv);
  }

  private Collection[] edges = new Collection[2];
  private SpatialIndex roadEdgeIndex = new STRtree();
  private GeometryMatchEvaluator geomMatchEval;

  public RoadSegmentsMutualBestMatcher(Collection edges0, Collection edges1,
                                       GeometryMatchEvaluator geomMatchEval
                                       )
  {
    edges[0] = edges0;
    edges[1] = edges1;
    this.geomMatchEval = geomMatchEval;
  }

  public void match()
  {
    match(0, 1);
    match(1, 0);
  }

  public void match(int fromIndex, int toIndex)
  {
    roadEdgeIndex = new STRtree();
    buildRoadEdgeIndex(edges[toIndex].iterator(), roadEdgeIndex);

    for (Iterator i = edges[fromIndex].iterator(); i.hasNext(); ) {
      SourceRoadSegment edge = (SourceRoadSegment) i.next();
      double queryBufferDist = edge.getLine().getLength();
      List nearEdges = query(edge, queryBufferDist, roadEdgeIndex);
      computeMatchValues(edge, nearEdges);
    }
  }


  private void computeMatchValues(SourceRoadSegment edge, List nearEdges)
  {
    for (Iterator i = nearEdges.iterator(); i.hasNext(); ) {
      SourceRoadSegment testEdge = (SourceRoadSegment) i.next();

      LineString line = (LineString) edge.getLine();
      LineString testLine =  (LineString) testEdge.getLine();

      double matchValue = geomMatchEval.match(line, testLine);

      if (matchValue == 0.0)
        continue;

      edge.getCandidateMatches().setValue(testEdge, matchValue);
      testEdge.getCandidateMatches().setValue(edge, matchValue);
    }
  }

  public void findMutualBestMatches()
  {
    for (Iterator i = edges[0].iterator(); i.hasNext(); ) {
      SourceRoadSegment edge = (SourceRoadSegment) i.next();
      SourceRoadSegment matchEdge = getBestMatchEdge(edge);
      if (matchEdge == null) continue;

      if (edge == getBestMatchEdge(matchEdge)) {
        //Not sure what the next line is used for [Jon Aquino 2004-04-19]
        double value = getBestMatchValue(edge);
        SourceRoadSegment.createMatch(edge, matchEdge);
      }
    }
  }
 

  private static double getBestMatchValue(SourceRoadSegment edge)
  {
    CandidateMatches matchList = edge.getCandidateMatches();
    MatchValue mv = matchList.getBestMatch();
    if (mv == null) return 0.0;
    return mv.getValue();
  }

  private static SourceRoadSegment getBestMatchEdge(SourceRoadSegment edge)
  {
    CandidateMatches matchList = edge.getCandidateMatches();
    MatchValue mv = matchList.getBestMatch();
    if (mv == null) return null;
    return (SourceRoadSegment) mv.getMatch();
  }


}