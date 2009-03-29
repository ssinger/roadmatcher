

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
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.*;
import java.util.*;

public class BoundaryFeature {

  private Feature feature;
  private int datasetRole;
  private Coordinate[] shellPts;// copy of the points of the polygon shell, oriented CW
  private double featureArea;  // feature area, cached for use during overlap comparisons
  private MatchedShell matchedShell;
  private List nearFeat = new ArrayList();// a list of BoundaryFeatures "near" this one

  boolean isOverlapping = false;

  public BoundaryFeature(int datasetRole, Feature feature)
  {
    this.datasetRole = datasetRole;
    setFeature(feature);
    featureArea = feature.getGeometry().getArea();
  }


  public Coordinate[] getShellCoordinates() {
    return shellPts;
  }

  public Coordinate getShellCoordinate(int i) {
    return shellPts[i];
  }

  public Feature getFeature() {
    return feature;
  }

  public boolean isOverlapping() {
    return isOverlapping;
  }

  /**
   * Return the MatchedShell object for this feature, creating it if necessary.
   *
   * @return the MatchedShell for this feature
   */
  public MatchedShell getMatchedShell() {
    if (matchedShell == null) {
      switch (datasetRole) {
      case BoundaryMatcher.REFERENCE:
        matchedShell = new MatchedShellReference(getShellCoordinates());
        break;
      case BoundaryMatcher.SUBJECT:
        matchedShell = new MatchedShellSubject(getShellCoordinates());
        break;
      }
    }
    return matchedShell;
  }

  public void addNearFeature(BoundaryFeature bf) {
    nearFeat.add(bf);
  }

  /**
   * If this BoundaryFeature overlaps any neighbour BoundaryFeature
   * by more than a given percentage, flag them both as overlapping.
   *
   * @param overlapTolerance the maximum percentage of overlap allowed
   */
  public void computeOverlaps(double overlapTolerance) {
    for (int i = 0; i < nearFeat.size(); i++) {
      BoundaryFeature bf = (BoundaryFeature) nearFeat.get(i);

      double intArea = 0.0;
      Geometry intGeom = null;
      try {
        intGeom = feature.getGeometry().intersection(bf.feature.getGeometry());
        intArea = intGeom.getArea();
      }
      // check for JTS problems (these should be fixed now - MD)
      catch (Exception ex) {
        System.out.println("BoundaryFeature#computeOverlaps: JTS problem found in intersection method! ");
        System.out.println(ex.getMessage());
        System.out.println("Input geometries:");
        System.out.println(feature.getGeometry().toString());
        System.out.println(bf.feature.getGeometry().toString());
      }
      boolean overlapFound =
          intArea / featureArea > overlapTolerance
           || intArea / bf.featureArea > overlapTolerance;
      /**
       *  If the overlap for either polygon exceeds tolerance,
       * mark them both as overlapping
       */
      if (overlapFound) {
        isOverlapping = true;
        bf.isOverlapping = true;
      }
    }
  }

  public List getAdjustedVertexIndicators()
  {
    MatchedShell ms = getMatchedShell();
    List coordArrays = null;
    coordArrays = ms.computeAdjustedVertexIndicators();

    Geometry g = (Geometry) feature.getGeometry();
    GeometryFactory fact = new GeometryFactory(g.getPrecisionModel(), g.getSRID());
    return CoordinateArrays.fromCoordinateArrays(coordArrays, fact);
  }

  public List getAdjustedEdgeIndicators()
  {
    MatchedShell ms = getMatchedShell();
    List coordArrays = null;
    coordArrays = ms.computeAdjustedEdgeIndicators();

    Geometry g = (Geometry) feature.getGeometry();
    GeometryFactory fact = new GeometryFactory(g.getPrecisionModel(), g.getSRID());
    return CoordinateArrays.fromCoordinateArrays(coordArrays, fact);
  }

  public boolean isAdjusted()
  {
    if (matchedShell != null && matchedShell.isAdjusted()) return true;
    return false;
  }

  /**
   * Creates a new geometry incorporating the adjusted shell.  Any holes in
   * the original geometry are cloned and added to the new geometry.
   *
   * @return an adjusted version of the geometry for this Feature
   */
  public Geometry getAdjustedGeometry()
  {
    Coordinate[] coord = matchedShell.getAdjusted();

    Polygon g = (Polygon) feature.getGeometry();
    GeometryFactory fact = new GeometryFactory(g.getPrecisionModel(), g.getSRID());
    Geometry adjGeom = fact.createPolygon(fact.createLinearRing(coord), cloneHoles(g));

    return adjGeom;
  }

  private LinearRing[] cloneHoles(Polygon poly)
  {
    int nHoles = poly.getNumInteriorRing();
    LinearRing[] holes = null;
    if (nHoles > 0) {
      holes = new LinearRing[nHoles];
      for (int i = 0; i < nHoles; i++) {
        LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
        LinearRing clone = (LinearRing) hole.clone();
        holes[i] = hole;
      }
    }
    return holes;
  }

  public void computeShellMatches(SegmentMatcher segMatcher)
  {
    MatchedShell shell = getMatchedShell();
    for (int i = 0; i < nearFeat.size(); i++) {
      BoundaryFeature bf = (BoundaryFeature) nearFeat.get(i);
      shell.computeSegmentMatches(bf.getMatchedShell(), segMatcher);
    }
    shell.updateAdjustedVertices();
  }

  public void computeAdjusted()
  {
    MatchedShell ms = getMatchedShell();
    ms.computeAdjustedShell();
  }

  private void setFeature(Feature feature) {
    this.feature = feature;

// MD - can we generalize this to work with Multipolygons as well?
    // make sure we are working with a CW shell
    Polygon poly = (Polygon) feature.getGeometry();
    LineString shell = poly.getExteriorRing();
    Coordinate[] coord = shell.getCoordinates();
    shellPts = (Coordinate[]) coord.clone();
    RobustCGAlgorithms cga = new RobustCGAlgorithms();
    if (cga.isCCW(shellPts)) {
      CoordinateArrays.reverse(shellPts);
    }
  }

  /**
   * Computes vertex matches for this Feature, comparing
   * it with all neighbour features.  (If a neighbour feature
   * is flagged as overlapping, however, it is skipped)
   *
   * @param sm the SegmentMatcher used to check for matches
   */
  public void computeVertexMatches(double distanceTolerance) {
    MatchedShell shell = getMatchedShell();
    for (int i = 0; i < nearFeat.size(); i++) {
      BoundaryFeature bf = (BoundaryFeature) nearFeat.get(i);
      // don't match with overlapping features
      if (bf.isOverlapping()) {
        continue;
      }
      shell.computeVertexMatches(bf.getMatchedShell(), distanceTolerance);
    }
  }

}
