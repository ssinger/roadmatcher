package com.vividsolutions.jcs.conflate.roads.match;

import java.util.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jcs.conflate.roads.nodematch.RoadNodeMatcher;
import com.vividsolutions.jcs.conflate.roads.pathmatch.RoadNetworkPathMatcher;

/**
 * Performs the process of automatically matching two {@link RoadNetwork}s
 */
public class RoadMatcherProcess
{
  private static int MATCH_ITERATIONS = 3;

  private static double MATCH_DISTANCE_TOLERANCE = 10.0;
  public static double OVERLAP_PERCENT = .3;
  public static double LENGTH_DIFF_PERCENT = 0.50;
  public static double OVERLAP_LENGTH_DIFF = 2 * MATCH_DISTANCE_TOLERANCE;

  private RoadNetwork[] source = new RoadNetwork[2];
  private RoadGraph[] sourceGraph = new RoadGraph[2];

  public RoadMatcherProcess(RoadNetwork source0, RoadNetwork source1)
  {
    source[0] = source0;
    source[1] = source1;

    sourceGraph[0] = source[0].getGraph();
    sourceGraph[1] = source[1].getGraph();
  }

  public void match(RoadMatchOptions matchOptions, TaskMonitor monitor)
  {
    for (int i = 0; i < MATCH_ITERATIONS; i++) {
      matchOnce(matchOptions, monitor);
      if (monitor.isCancelRequested()) break;
    }
  }

  public void matchOnce(RoadMatchOptions matchOptions, TaskMonitor monitor)
  {

    if (matchOptions.isStandaloneEnabled())
      computeStandaloneRoadSegments(matchOptions.getStandaloneOptions(), monitor);

    if (matchOptions.isEdgeMatchEnabled()) {
      computeNodeMatches(matchOptions.getEdgeMatchOptions().getDistanceTolerance(), monitor);
      computePathMatches(matchOptions.getEdgeMatchOptions(), monitor);
    }
  }

  private void computeStandaloneRoadSegments(StandaloneOptions options, TaskMonitor monitor)
  {
    computeStandaloneRoadSegments(0, options, monitor);
    computeStandaloneRoadSegments(1, options, monitor);
  }

  private void computeStandaloneRoadSegments(int index, StandaloneOptions options, TaskMonitor monitor)
  {
    monitor.report("Finding Standalone Segments in " + source[index].getName());
    StandaloneFinder finder = new StandaloneFinder(
        sourceGraph[index].getEdges(),
        sourceGraph[1 - index].getEdges()
        );
    finder.setDistanceTolerance(options.getDistanceTolerance());
    finder.process(monitor);
  }


  /**
   * Matches graph nodes based on topology and distance
   */
  private void computeNodeMatches(double distanceTolerance, TaskMonitor monitor)
  {
    monitor.report("Finding Node Matches");
    RoadNodeMatcher matcher
        = new RoadNodeMatcher(source[0], source[1]);
    matcher.setNodeDistanceTolerance(distanceTolerance);
    matcher.setMatchInlineNodes(true);
    matcher.match();
  }


  private void computePathMatches(EdgeMatchOptions options, TaskMonitor monitor)
  {
    monitor.report("Finding Path Matches");
    RoadNetworkPathMatcher pathMatcher
        = new RoadNetworkPathMatcher(source[0], source[1]);
    pathMatcher.setDistanceTolerance(options.getDistanceTolerance());
    pathMatcher.setRoadSegmentLengthTolerance(options.getLineSegmentLengthTolerance());
    pathMatcher.match(monitor);
  }

  public FeatureCollection getRoadSegmentMatchInd()
  {
    LineStringMatchIndicatorFactory indFact = new LineStringMatchIndicatorFactory();
    List matchInd = new ArrayList();

    for (Iterator i = sourceGraph[0].edgeIterator(); i.hasNext(); )
    {
      SourceRoadSegment edge = (SourceRoadSegment) i.next();
      SourceRoadSegment matchEdge = edge.getMatchingRoadSegment();
      if (matchEdge != null) {

        Geometry matchIndGeom = indFact.getIndicator(matchEdge.getLine(), edge.getLine());
        matchInd.add(matchIndGeom);
      }
    }
    return FeatureDatasetFactory.createFromGeometry(matchInd);
  }

  public FeatureCollection getNodeMatchVectors()
  {
    return getNodeMatchVectors(sourceGraph[1].nodeIterator());
  }

  private FeatureCollection getNodeMatchVectors(Iterator nodeIt)
  {
    String matchValueCol = "MATCH_VALUE";
    FeatureSchema matchIndSchema = new FeatureSchema();
    matchIndSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    matchIndSchema.addAttribute(matchValueCol, AttributeType.DOUBLE);
    FeatureDataset matchIndFC = new FeatureDataset(matchIndSchema);
    GeometryFactory fact = new GeometryFactory();

    //List matchInd = new ArrayList();
    for (; nodeIt.hasNext(); ) {
      RoadNode subNode = (RoadNode) nodeIt.next();
      RoadNode matchNode = subNode.getMatch();
      if (matchNode != null) {
        Coordinate[] coord = { subNode.getCoordinate(), matchNode.getCoordinate() };
        Geometry matchLine = fact.createLineString(coord);

        Feature feature = new BasicFeature(matchIndFC.getFeatureSchema());
        feature.setGeometry(matchLine);
        feature.setAttribute(matchValueCol, new Double(subNode.getMatchValue()));
        matchIndFC.add(feature);

        /*
        System.out.println("LINESTRING("
                           + node.getCoordinate().x
                           + " "
                           + node.getCoordinate().y
                           + ","
                           + matchNode.getCoordinate().x
                           + " "
                           + matchNode.getCoordinate().y
                           + ")"
                           );
        */
      }
    }
    return matchIndFC;
    //return FeatureDatasetFactory.createFromGeometryWithLength(matchInd, "length");
  }


}