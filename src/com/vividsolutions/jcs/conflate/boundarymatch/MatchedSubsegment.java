

/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jcs.conflate.boundarymatch;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Models the portion of a Subject
 * segment which matches all or part of a segment in a Reference shell.
 */
public class MatchedSubsegment
  implements Comparable
{
/**
 * Computes the position of the matched segment
 * along the parent segment.
 * The position of the Matched segment along to the Parent segment
 * is determined by the projection of the Matched seg on the Parent seg.
 * (Note this is the inverse of computing the actual adjusted value of the subsegment,
 * which is determined by the projection of the Parent on the Matched.)
 */
  private static double computePosition(LineSegment matchSeg, LineSegment parentSeg)
  {
    double position0 = parentSeg.projectionFactor(matchSeg.p0);
    double position1 = parentSeg.projectionFactor(matchSeg.p1);
    double minPosition = position0;
    if (position1 < position0) minPosition = position1;
    return minPosition;
  }

  private MatchedSegment matchSeg;
  private double position;
  private SegmentProjecter segProj;

  /**
   * Creates a subsegment which represents the portion of a matching
   * segment which matches all or part of the parent segment.
   *
   * @param parentSeg the subject (parent) segment
   * @param matchSeg the reference segment that has been matched to the parent segment
   */
  public MatchedSubsegment(LineSegment parentSeg, MatchedSegment matchSeg, double distanceTolerance)
  {
    this.matchSeg = matchSeg;
    position = computePosition(matchSeg.getSegment(), parentSeg);
    segProj = new SegmentProjecter(parentSeg, matchSeg.getSegment(), distanceTolerance);
  }

  /**
   * the Reference segment matched to this subsegment.
   *
   * @return a MatchedSegment from the Reference feature matching this segment
   */
  public MatchedSegment getMatchedSegment() { return matchSeg; }

  public boolean isEndPointAdjusted(int i) { return segProj.isEndPointImage(i); }

  public Coordinate getAdjustedCoordinate(int i)  {    return segProj.getCoordinate(i);  }

  /**
   *  Compares this SourceSubsegment with the specified SourceSubsegment for order.
   *  Uses the position along the Subject segment as the basis of
   *  the comparison.
   *
   *@param  o  the <code>SourceSubsegment</code>
   *      with which this <code>SourceSubsegment</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer
   *      as this <code>SourceSubsegment</code>
   *      is less than, equal to, or greater than '
   * the specified <code>SourceSubsegment</code>
   */
  public int compareTo(Object o)
  {
    MatchedSubsegment other = (MatchedSubsegment) o;
    if (position < other.position) return -1;
    if (position > other.position) return 1;
    return 0;
  }

}
