package com.vividsolutions.jcs.conflate.roads.match;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.*;
import com.vividsolutions.jts.util.Debug;
import com.vividsolutions.jts.index.strtree.*;
import com.vividsolutions.jcs.algorithm.linearreference.LengthSubstring;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jump.task.TaskMonitor;

/**
 * Finds RoadSegments in a source dataset which have no
 * RoadSegments in the other source near them, and hence are guaranteed
 * to be standalone road segments.
 * Marks the RoadSegments found as standalone.
 */
public class StandaloneFinder {

  private int standaloneCount = 0;
  private double distanceTolerance = 0.0;
  private boolean ignoreIntersectionsAtEnds = true;

  private Collection srcEdges;
  private Collection queryEdges;
  private RoadSegmentIndex index;

  public StandaloneFinder(Collection srcEdges, Collection queryEdges)
  {
    this.srcEdges = srcEdges;
    this.queryEdges = queryEdges;
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  public void process(TaskMonitor monitor)
  {
    index = new RoadSegmentIndex(queryEdges);

    int total = srcEdges.size();
    int count = 0;
    for (Iterator i = srcEdges.iterator(); i.hasNext(); ) {
      count++;
      monitor.report(count, total, "segments tested");

      SourceRoadSegment roadSegment = (SourceRoadSegment) i.next();
      // only process road segments which are unknown
      if (roadSegment.getState() != SourceState.UNKNOWN)
        continue;
      double queryBufferDist = distanceTolerance;
      List nearEdges = index.query(roadSegment, queryBufferDist);
      LineString queryLine = (LineString) roadSegment.getLine();
      /**
       * If ignoring intersections at ends and line is long enough,
       * use a trimmed version of the line to do the standalone query
       */
      if (ignoreIntersectionsAtEnds) {
        queryLine = trimmedLine(queryLine, distanceTolerance);
      }
      // if line is null, can't check its standalone status
      // (this might happen if line is trimmed)
      if (queryLine.isEmpty())
        continue;
      checkStandalone(roadSegment, queryLine, nearEdges);
    }
    //Debug.println("standalones found = " + standaloneCount);
  }

  private LineString trimmedLine(LineString srcLine, double distanceTolerance)
  {
    double len = srcLine.getLength();
    double startLen = 2 * distanceTolerance;
    double endLen = len - 2 * distanceTolerance;
    if (endLen < startLen) {
      startLen = len / 2;
      endLen = startLen;
    }
    LineString trimLine = LengthSubstring.getSubstring(srcLine,
        startLen,
        endLen);
    return trimLine;
  }

  private void checkStandalone(SourceRoadSegment roadSegment,
                               LineString queryLine,
                               Collection queryRoadSegments)
  {
    for (Iterator i = queryRoadSegments.iterator(); i.hasNext(); ) {
      SourceRoadSegment testSeg = (SourceRoadSegment) i.next();
      LineString testLine =  (LineString) testSeg.getLine();
      // only consider UNKNOWNs as possible matches
      if (testSeg.getState() != SourceState.UNKNOWN)
        continue;

      if (queryLine.isWithinDistance(testLine, distanceTolerance))
          return;
    }
    // no road segments close - mark as standalone
    roadSegment.setState(SourceState.STANDALONE, null);
    standaloneCount++;
  }

}