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

package com.vividsolutions.jcs.conflate.roads.match;

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.polygonize.Polygonizer;
import com.vividsolutions.jcs.algorithm.linearreference.LocatePoint;
import com.vividsolutions.jcs.conflate.roads.ErrorMessages;
import com.vividsolutions.jump.task.DummyTaskMonitor;

public class LineStringMatchIndicatorFactory {

  public static boolean isValidQuad(Coordinate[] quadPts)
  {
    lineInt.computeIntersection(quadPts[1], quadPts[2], quadPts[3], quadPts[4]);
    if (lineInt.hasIntersection())
      return false;
    lineInt.computeIntersection(quadPts[0], quadPts[1], quadPts[2], quadPts[3]);
    if (lineInt.hasIntersection())
      return false;
    return true;
  }
  public static Coordinate[] computeLargestQuad(Coordinate[] quadPts)
  {
    double area = Math.abs(CGAlgorithms.signedArea(quadPts));
    // make new quad with one line flipped
    Coordinate[] quadPts2 = (Coordinate[]) quadPts.clone();
    Coordinate temp = quadPts2[2];
    quadPts2[2] = quadPts2[3];
    quadPts2[3] = temp;

    // if not valid, return original
    if (! isValidQuad(quadPts2))
      return quadPts;

    double area2 = Math.abs(CGAlgorithms.signedArea(quadPts2));

    if (area2 > area)
      return quadPts2;
    return quadPts;
  }

  public Coordinate[] closestPair(Coordinate[] pts1, Coordinate[] pts2)
  {
    double minDistance = Double.MAX_VALUE;
    Coordinate[] result = new Coordinate[2];
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        double dist = pts1[i].distance(pts2[j]);
        if (dist < minDistance) {
          result[0] = pts1[i];
          result[1] = pts2[j];
          minDistance = dist;
        }
      }
    }
    return result;
  }

  private static final GeometryFactory fact = new GeometryFactory();
  private static final LineIntersector lineInt = new RobustLineIntersector();

  public LineStringMatchIndicatorFactory() {
  }

  public Geometry getIndicator(LineString line1, LineString line2)
  {
    return getLinesIndicator(line1, line2);
    /*
    if (line1.getNumPoints() < 3 && line2.getNumPoints() < 3)
      return edgeMatchBox(line1, line2);
    else
      return edgeMatchPolygons(line1, line2);
    */
  }
  public Geometry getQuadIndicator(LineString line1, LineString line2)
  {
    Coordinate[] edge1Pts = line1.getCoordinates();
    Coordinate[] edge2Pts = line2.getCoordinates();
    Coordinate[] pts = new Coordinate[5];
    pts[0] = edge1Pts[0];
    pts[1] = edge1Pts[edge1Pts.length - 1];
    pts[2] = edge2Pts[0];
    pts[3] = edge2Pts[edge2Pts.length - 1];
    pts[4] = edge1Pts[0];

    makeValidQuad(pts);

    //Coordinate[] largestQuad = pts;
    Coordinate[] largestQuad = computeLargestQuad(pts);

    return fact.createPolygon(fact.createLinearRing(largestQuad), null);
  }

  public static void makeValidQuad(Coordinate[] quadPts)
  {
    // flip the points of one line if the line directions are opposite
    // (e.g. the box is actually a bow-tie)
    // if the line connectors cross, flip one of the lines
    if (! isValidQuad(quadPts)) {
      Coordinate temp = quadPts[2];
      quadPts[2] = quadPts[3];
      quadPts[3] = temp;
    }
    // if the lines cross, flip one of the connectors
    if (! isValidQuad(quadPts)) {
      Coordinate temp = quadPts[1];
      quadPts[1] = quadPts[2];
      quadPts[2] = temp;
    }
  }

  public Geometry getPolygonIndicator(LineString line1, LineString line2)
  {
    Coordinate[] edge1Pts = line1.getCoordinates();
    Coordinate[] edge2Pts = line2.getCoordinates();

    // find closest endpoint pair
    Coordinate[] closestPair = closestPair(
        new Coordinate[] { edge1Pts[0], edge1Pts[edge1Pts.length - 1] },
        new Coordinate[] { edge2Pts[0], edge2Pts[edge2Pts.length - 1] });

    Coordinate[] furthestPair = new Coordinate[2];
    furthestPair[0] = closestPair[0] == edge1Pts[0] ? edge1Pts[edge1Pts.length - 1] : edge1Pts[0];
    furthestPair[1] = closestPair[1] == edge2Pts[0] ? edge2Pts[edge2Pts.length - 1] : edge2Pts[0];

    LineString closingLine1 = fact.createLineString(closestPair);
    LineString closingLine2 = fact.createLineString(furthestPair);

    MultiLineString closingML = fact.createMultiLineString(
        new LineString[] {closingLine1, closingLine2} );
    MultiLineString edgeML = fact.createMultiLineString(
        new LineString[] {line1, line2} );

    // by taking the union of the two geometries we are in effect noding them together
    Geometry union = closingML.union(edgeML);

    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(union);
    polygonizer.polygonize(new DummyTaskMonitor());
    // create MultiPolygons rather than GeometryCollections so they can be save to a shapefile
    List polygons = polygonizer.getPolygons();
    if (polygons == null) {
      System.out.println(ErrorMessages.lineStringMatchIndicatorFactory_nullPolygonList);
      System.out.println(union);
    }
    return fact.buildGeometry(polygons);

    //return fact.createGeometryCollection(fact.toGeometryArray(polygonizer.getPolygons()));
  }

  /**
   * Computes whether lines have same direction, based on the proximity
   * of the start point of one line to the endpoints of the other
   * @param line1 a linestring
   * @param line2 a linestring
   * @return <code>true</code> if the start points of both lines are closest
   */
  public static boolean isStartPointsClosest(LineString line1, LineString line2)
  {
    Coordinate startPt = line1.getCoordinateN(0);
    Coordinate pt0 = line2.getCoordinateN(0);
    Coordinate ptn = line2.getCoordinateN(line2.getNumPoints() - 1);
    if (startPt.distance(pt0) < startPt.distance(ptn))
      return true;
    return false;
  }
  public LineString orientLine(LineString line, Coordinate testStartPt)
  {
    if (! line.getCoordinateN(0).equals(testStartPt)) {
      LineString revLine = (LineString) line.clone();
      CoordinateArrays.reverse(revLine.getCoordinates());
      return revLine;
    }
    return line;
  }
  public Geometry getLinesIndicator(LineString line0, LineString line1)
  {
    Coordinate[] edge0Pts = line0.getCoordinates();
    Coordinate[] edge1Pts = line1.getCoordinates();

    // find closest endpoint pair
    Coordinate[] closestEndPts = closestPair(
        new Coordinate[] { edge0Pts[0], edge0Pts[edge0Pts.length - 1] },
        new Coordinate[] { edge1Pts[0], edge1Pts[edge1Pts.length - 1] });

    // remaining endpoints
    Coordinate[] otherEndPts = new Coordinate[2];
    otherEndPts[0] = closestEndPts[0] == edge0Pts[0] ? edge0Pts[edge0Pts.length - 1] : edge0Pts[0];
    otherEndPts[1] = closestEndPts[1] == edge1Pts[0] ? edge1Pts[edge1Pts.length - 1] : edge1Pts[0];

    Coordinate[] startPts = new Coordinate[] {  closestEndPts[0], closestEndPts[1] };
    // if the connecting lines cross, interchange the pairs of endpoints
    lineInt.computeIntersection(closestEndPts[0], closestEndPts[1],
                                otherEndPts[0], otherEndPts[1]);
    if (lineInt.hasIntersection()) {
      startPts[1] = otherEndPts[1];
    }

    LineString orientedLine0 = orientLine(line0, startPts[0]);
    LineString orientedLine1 = orientLine(line1, startPts[1]);;

    return createLinesIndicatorGeometry(orientedLine0, orientedLine1);
  }
  public Geometry createLinesIndicatorGeometry(LineString line0, LineString line1)
  {
    double len0 = line0.getLength();
    double len1 = line1.getLength();

    double intervalLen = 10.0;
    double minLen = Math.min(len0, len1);
    int nLines = (int) ((minLen * 0.8) / intervalLen);
    if (nLines < 2) nLines = 2;

    List indLines = new ArrayList();
    for (int i = 0; i <= nLines; i++) {
      double distFrac = .1 + 0.8 * (i / (double) nLines);
      double dist0 = len0 * distFrac;
      double dist1 = len1 * distFrac;

      Coordinate[] indPts = new Coordinate[2];
      indPts[0] = LocatePoint.pointAlongLine(line0, dist0);
      indPts[1] = LocatePoint.pointAlongLine(line1, dist1);
      indLines.add(fact.createLineString(indPts));
    }

    return fact.createMultiLineString(fact.toLineStringArray(indLines));
  }
}
