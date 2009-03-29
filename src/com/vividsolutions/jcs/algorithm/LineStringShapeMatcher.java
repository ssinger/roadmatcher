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

package com.vividsolutions.jcs.algorithm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.algorithm.LineStringWidth;
import com.vividsolutions.jump.geom.Angle;


/**
 * A collection of shape-matching algorithms for LineStrings.
 */
public class LineStringShapeMatcher
{

  public static final double STRAIGHT_WIDTH_PERCENT = .20;

  /**
   * Computes whether a LineString is "approximately straight".
   * This is the case if the line has a width of less than STRAIGHT_WIDTH_PERCENT
   * percentage of it's length
   *
   * @param line
   * @return <code>true</code> if the linestring is approximately straight
   */
  public static boolean isApproximatelyStraight(LineString line)
  {
    double width = LineStringWidth.maxWidth(line);
    return width / line.getLength() < STRAIGHT_WIDTH_PERCENT;
  }

  public static LineSegment directionLineSegment(LineString line)
  {
    return new LineSegment(line.getCoordinateN(0),
                           line.getCoordinateN(line.getNumPoints() - 1));
  }

  public static final double MAX_ANGLE_DIFF = Math.PI / 3;

  public static double lengthDifferencePercent(LineString line0, LineString line1)
  {
    double len0 = line0.getLength();
    double len1 = line1.getLength();
    double min = Math.min(len0, len1);
    double max = Math.max(len0, len1);
    return min / max;
  }

  /**
   * Computes whether two linestrings are "orientation compatible".
   * This is the case if they have a relative
   * angle of less than 60 degrees.
   *
   * @param line0
   * @param line1
   * @return
   */
  public static boolean isOrientationCompatible(LineString line0, LineString line1)
  {
    LineSegment seg0 = directionLineSegment(line0);
    LineSegment seg1 = directionLineSegment(line1);
    double ang0 = seg0.angle();
    double ang1 = seg1.angle();

    double angDiff = Angle.diff(ang0, ang1);
    double angInvDiff = Angle.diff(ang0, Angle.normalize(ang1 + Math.PI));

    double minAngDiff = Math.min(angDiff, angInvDiff);
    return minAngDiff < MAX_ANGLE_DIFF;
  }

  private LineStringShapeMatcher()
  {

  }
}
