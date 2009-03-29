

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

package com.vividsolutions.jcs.conflate.coverage;

import java.util.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.boundarymatch.SegmentMatcher;

/**
 * Contains information about a Feature which has or participates
 * in one or more coverage gaps.
 * (E.g. has one or more segments along its edge which form a gap or overlap
 * with another feature).
 */
public class CoverageFeature {

  private Feature feature;
  private boolean isProcessed = false;
  private boolean isAdjusted = false;
  private Shell shell;

  public CoverageFeature(Feature feature, VertexMap vmap) {
    this.feature = feature;
    Polygon poly = (Polygon) feature.getGeometry();
    shell = new Shell();
    shell.initialize((LinearRing) poly.getExteriorRing(), vmap);
  }

  public Feature getFeature() { return feature; }

  public boolean isAdjusted() { return isAdjusted; }
  public boolean isProcessed() { return isProcessed; }

  /**
   * Creates a new geometry incorporating the adjusted shell.  Any holes in
   * the original geometry are cloned and added to the new geometry.
   *
   * @return an adjusted version of the geometry for this Feature
   */
  public Geometry getAdjustedGeometry()
  {
    Coordinate[] coord = shell.getAdjusted();

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
  /**
   * Computes the adjustments to this feature.
   *
   * @param nearFeatures a list of CoverageGapFeatures that are close to this feature
   * @param segMatcher the SegmentMatcher to use, initialized with the distance tolerance
   */
  public void computeAdjustment(List nearFeatures, SegmentMatcher segMatcher)
  {
    isProcessed = true;

    for (Iterator i = nearFeatures.iterator(); i.hasNext(); ) {
      CoverageFeature cgf = (CoverageFeature) i.next();
      if (cgf == this)
        continue;
      shell.match(cgf.shell, segMatcher);
    }
    // testing - shells are always adjusted even if they have conflicting vertices
    //if (! shell.isConflict())
      isAdjusted = shell.isAdjusted();
  }


}
