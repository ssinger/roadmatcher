

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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Debug;
import com.vividsolutions.jump.geom.LineSegmentUtil;
import com.vividsolutions.jump.geom.*;
/**
 * A SegmentMatcher computes information about whether two boundary
 * LineSegments match
 */
public class SegmentMatcher
{
  // the possible relative orientations of matched segments
  public static final int SAME_ORIENTATION = 1;
  public static final int OPPOSITE_ORIENTATION = 2;
  public static final int EITHER_ORIENTATION = 3;

  public static boolean isCloseTo(Coordinate coord,
                            LineSegment seg,
                            double tolerance)
  {
    if (coord.distance(seg.getCoordinate(0)) < tolerance) {
      return true;
    }
    if (coord.distance(seg.getCoordinate(1)) < tolerance) {
      return true;
    }
    return false;
  }


  //public static final double ANGLE_TOLERANCE = Math.PI / 8;   // 22.5 degrees
  public static final double PI2 = 2.0 * Math.PI;

  /**
   * Computes an equivalent angle in the range 0 <= ang < 2*PI
   *
   * @param angle the angle to be normalized
   * @return the normalized equivalent angle
   */
  public static double normalizedAngle(double angle)
  {
    if (angle < 0.0) return PI2 + angle;
    if (angle >= PI2) angle -= PI2;
    return angle;
  }

  /**
   * Computes the delta in the angles between two line segments.
   */
  public static double angleDiff(LineSegment seg0, LineSegment seg1)
  {
    double a0 = normalizedAngle(seg0.angle());
    double a1 = normalizedAngle(seg1.angle());
    double delta = Math.abs(a0 - a1);
    return delta;
  }
  /**
   * Computes the delta in the angles between a line segment and the inverse of another line seg.
   */
  public static double angleInverseDiff(LineSegment seg0, LineSegment seg1)
  {
    return angleDiff(seg0.angle(), seg1.angle() + Math.PI);
  }

  /**
   * Computes the angle difference between two angles.
   * @param angle1
   * @param angle2
   * @return the angle difference.  This will always be <= PI.
   */
  public static double angleDiff(double angle0, double angle1)
  {
    double norm0 = normalizedAngle(angle0);
    double norm1 = normalizedAngle(angle1);
    double angleDiff = Math.abs(norm0 - norm1);
    if (angleDiff > Math.PI)
      return PI2 - angleDiff;
    return angleDiff;
  }

  // temp storage for point args
  private LineSegment line0 = new LineSegment();
  private LineSegment line1 = new LineSegment();

  private double distanceTolerance;
  private double angleTolerance;
  private double angleToleranceRad;
  private int segmentOrientation;

  public SegmentMatcher(double distanceTolerance, double angleTolerance)
  {
    this(distanceTolerance, angleTolerance, OPPOSITE_ORIENTATION);
  }

  public SegmentMatcher(double distanceTolerance, double angleTolerance, int segmentOrientation)
  {
    this.distanceTolerance = distanceTolerance;
    this.angleTolerance = angleTolerance;
    angleToleranceRad = Angle.toRadians(angleTolerance);

    this.segmentOrientation = segmentOrientation;
  }

  public double getDistanceTolerance() { return distanceTolerance; }

  public boolean isMatch(
      Coordinate p00,
      Coordinate p01,
      Coordinate p10,
      Coordinate p11
      )
  {
    line0.p0 = p00;
    line0.p1 = p01;
    line1.p0 = p10;
    line1.p1 = p11;
    return isMatch(line0, line1);
  }

  /**
   * Computes whether two segments match.
   * This matching algorithm uses the following conditions to determine if
   * two line segments match:
   * <ul>
   * <li> The segments have similar slope.
   * I.e., the difference in slope between the two segments is less than
   * the angle tolerance (this test is made irrespective of orientation)
   * <li> The segments have a mutual overlap
   * (e.g. they both have a non-null projection on
   * the other)
   * <li> The Hausdorff distance between the mutual projections of the segments
   * is less than the distance tolerance.  This ensures that matched segments
   * are close along their entire length.
   * </ul>
   * <p>
   * This relation is symmetrical.
   *
   * @param seg1
   * @param seg2
   * @return <code>true</code> if the segments match
   */
  public boolean isMatch(LineSegment seg1, LineSegment seg2)
  {
//Debug.print(seg1 + " - " + seg2);
    boolean isMatch = true;
    double dAngle = angleDiff(seg1, seg2);
    double dAngleInv = angleInverseDiff(seg1, seg2);
    switch (segmentOrientation) {
      case OPPOSITE_ORIENTATION:
        if (dAngleInv > angleToleranceRad) {
          isMatch = false;
          return isMatch;
        }
        break;
      case SAME_ORIENTATION:
        if (dAngle > angleToleranceRad) {
          isMatch = false;
          return isMatch;
        }
        break;
      case EITHER_ORIENTATION:
        if (dAngle > angleToleranceRad
           && dAngleInv > angleToleranceRad) {
//Debug.println("angle too large");
          isMatch = false;
          return isMatch;
        }
        break;
    }

    LineSegment projSeg1 = LineSegmentUtil.project(seg1, seg2);
    LineSegment projSeg2 = LineSegmentUtil.project(seg2, seg1);
    if (projSeg1 == null || projSeg2 == null) {
      isMatch = false;
      return isMatch;
    }

    if (LineSegmentUtil.hausdorffDistance(projSeg1, projSeg2) > distanceTolerance) {
//Debug.println("distance too great");
      isMatch = false;
    }
//Debug.print(isMatch, "MATCHED!");
    return isMatch;
  }

  /**
   * Test whether there is an overlap between the segments in either direction.
   * A segment overlaps another if it projects onto the segment.
   */
  public boolean hasMutualOverlap(LineSegment src, LineSegment tgt)
  {
    if (projectsOnto(src, tgt)) return true;
    if (projectsOnto(tgt, src)) return true;
    return false;
  }

  public boolean projectsOnto(LineSegment seg1, LineSegment seg2)
  {
    double pos0 = seg2.projectionFactor(seg1.p0);
    double pos1 = seg2.projectionFactor(seg1.p1);
    if (pos0 >= 1.0 && pos1 >= 1.0) return false;
    if (pos0 <= 0.0 && pos1 <= 0.0) return false;
    return true;
  }

}
