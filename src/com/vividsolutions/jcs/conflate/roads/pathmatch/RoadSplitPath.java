package com.vividsolutions.jcs.conflate.roads.pathmatch;

import java.util.*;

import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.split.*;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.graph.DirectedEdge;
import com.vividsolutions.jcs.geom.LineStringUtil;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.debug.*;

public class RoadSplitPath {

  private List splitEdges;
  private SourceRoadSegment[] splitRoadSegment;
  private Map roadSegmentMap = new HashMap();
  private Map splitRSMap = new HashMap();

  // audit trail of what changed
  private List added = new ArrayList();
  private List matched = new ArrayList();
  private List deleted = new ArrayList();

  public RoadSplitPath(SplitPath splitPath)
  {
    splitEdges = splitPath.getSplitEdges();
    splitRoadSegment = new SourceRoadSegment[splitEdges.size()];
    buildRoadSegmentMap();
  }

  private void buildRoadSegmentMap()
  {
    int i = 0;
    for (Iterator it = splitEdges.iterator(); it.hasNext(); ) {
      SplitEdge splitEdge = (SplitEdge) it.next();
      LinearEdge e = splitEdge.getParent();
      SourceRoadSegment rs = (SourceRoadSegment) ((DirectedEdge) e.getContext()).getEdge();
      splitRoadSegment[i++] = rs;
      addToMap(rs, splitEdge);
    }
  }

  private void addToMap(SourceRoadSegment rs, SplitEdge split)
  {
    List splitList = (List) roadSegmentMap.get(rs);
    if (splitList == null) {
      splitList = new ArrayList();
      roadSegmentMap.put(rs, splitList);
    }
    splitList.add(split);
  }

  /**
   * Returns the new road segments created by splitting.
   *
   * @return a List of {@link SourceRoadSegment}s
   */
  public List getAdded() { return added; }

  /**
   * Returns the road segments replaced by new split road segments.
   *
   * @return a List of {@link SourceRoadSegment}s
   */
  public List getDeleted() { return deleted; }

  /**
   * Returns the road segments in this path which were matched.
   * The list will contain both newly created split road segments
   * and already existing road segments which were matched.
   *
   * @return a List of {@link SourceRoadSegment}s
   */
  public List getMatched() { return matched; }

  private void addDeleted(RoadSegment rs)  {    deleted.add(rs);  }
  private void addMatched(RoadSegment rs)  {    matched.add(rs);  }
  private void addAdded(RoadSegment rs)  {    added.add(rs);  }
  private void addAdded(RoadSegment[] rs)
  {
    for (int i = 0; i < rs.length; i++) {
      addAdded(rs[i]);
    }
  }
  /**
   * Updates the source road segments with the results of splitting this path.
   */
  public void updateSource()
  {
    Collection keys = roadSegmentMap.keySet();
    for (Iterator it = keys.iterator(); it.hasNext(); ) {
      SourceRoadSegment rs = (SourceRoadSegment) it.next();
      List splitEdges = (List) roadSegmentMap.get(rs);
      updateSource(rs, splitEdges);
    }
  }

  /**
   * Updates a source road segment with the corresponding split edge(s)
   * in the path
   * @param rs the original source road segment in the path
   * @param splitEdges the edge(s) it was split into
   */
  private void updateSource(SourceRoadSegment rs, List splitEdges)
  {
    /**
     * If the road has been split, split it in the orginal network,
     * and record the resulting new RoadSegments for use in matching
     */
    if (splitEdges.size() > 1) {
      performSplits(rs, splitEdges);
    }
    else {
      /**
       * If the road was not split, just map the splitEdge to it directly
       */
      splitRSMap.put(splitEdges.get(0), rs);
    }
  }

  /**
   * Splits the source road segments according to the splitEdges in the input list.
   * Reverses the geometry of the split edges if the original directed path
   * was in the reverse direction of the underlying geometry.
   * @param rs
   * @param splitEdges
   */
  private void performSplits(SourceRoadSegment rs, List splitEdges)
  {
    LineString[] lines;
    if (isReversedGeometry(rs, splitEdges)) {
      lines = getReversedLines(splitEdges);
      SourceRoadSegment[] splitRS = rs.split(lines);
      addDeleted(rs);
      addAdded(splitRS);
      // save mapping from splitEdge to new splitRoadSegment for use in matching
      for (int i = 0; i < splitRS.length; i++) {
        splitRSMap.put(splitEdges.get(i), splitRS[splitRS.length - 1 - i]);
      }
    }
    else {
      lines = getLines(splitEdges);
      SourceRoadSegment[] splitRS = rs.split(lines);
      addDeleted(rs);
      addAdded(splitRS);
      // save mapping from splitEdge to new splitRoadSegment for use in matching
      for (int i = 0; i < splitRS.length; i++) {
        splitRSMap.put(splitEdges.get(i), splitRS[i]);
      }
    }
//    Geometry allLines = lines[0].getFactory().createMultiLineString(lines);
//    Debug.println(allLines);
  }

  private static boolean isReversedGeometry(SourceRoadSegment rs, List splitEdges)
  {
    SplitEdge split = (SplitEdge) splitEdges.get(0);
    DirectedEdge de = (DirectedEdge) split.getParent().getContext();
    boolean isForward = de.getEdgeDirection();
    return ! isForward;
  }

  private static LineString[] getLines(List splitEdges)
  {
    LineString[] lines = new LineString[splitEdges.size()];
    int i = 0;
    for (Iterator it = splitEdges.iterator(); it.hasNext(); ) {
      SplitEdge split = (SplitEdge) it.next();
      lines[i++] = (LineString) split.getGeometry();
    }
    return lines;
  }

  private static LineString[] getReversedLines(List splitEdges)
  {
    LineString[] lines = new LineString[splitEdges.size()];
    int i = lines.length - 1;
    for (Iterator it = splitEdges.iterator(); it.hasNext(); ) {
      SplitEdge split = (SplitEdge) it.next();
      LineString line = (LineString) split.getGeometry();
      lines[i--] = LineStringUtil.reverse(line);
    }
    return lines;
  }

  private SourceRoadSegment getSourceRoad(SplitEdge e)
  {
    return (SourceRoadSegment) splitRSMap.get(e);
  }

  /**
   * Matches the SplitRoadSegments between two paths which
   * are the result of PathSplitting
   * <p>
   * Note: run once only for each pair of matching paths
   *
   * @param matchingPath the patch to match
   */
  public int matchSplits(RoadSplitPath matchingPath)
  {
    int matchCount = 0;
    for (Iterator it = splitEdges.iterator(); it.hasNext(); ) {
      SplitEdge split = (SplitEdge) it.next();
      SourceRoadSegment thisRS = getSourceRoad(split);
      SplitEdge matchSplit = split.getMatch();
      // there may not be a match for the final split edge
      if (matchSplit != null) {
        SourceRoadSegment matchRS = matchingPath.getSourceRoad(matchSplit);
        SourceRoadSegment.createMatch(thisRS, matchRS);
        addMatched(thisRS);
        matchingPath.addMatched(matchRS);
        matchCount++;
      }
    }
    return matchCount;
  }
}