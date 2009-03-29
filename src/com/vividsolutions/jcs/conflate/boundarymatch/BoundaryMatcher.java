

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

import java.util.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.*;
import com.vividsolutions.jts.geom.*;

public class BoundaryMatcher {

  public static final int REFERENCE = 0;
  public static final int SUBJECT = 1;
  public static String ATTR_ADJ_DISTANCE = "AdjustedDistance";

  FeatureCollection[] inputFC = new FeatureCollection[2];
  FeatureUpdateRecorder[] updates =
      {new FeatureUpdateRecorder(), new FeatureUpdateRecorder()};
  List[] matchedEdges = new List[2];
  BoundaryFeatureFilter bmf;
  BoundaryMatcherParameters param;

  public BoundaryMatcher(
      FeatureCollection referenceFC,
      FeatureCollection subjectFC) {
    inputFC[REFERENCE] = referenceFC;
    inputFC[SUBJECT] = subjectFC;
  }

  public void match(BoundaryMatcherParameters param)
  {
    this.param = param;

DebugTimer.startStatic("BoundaryMatcher");
    // find polygons close to boundary
    bmf = new BoundaryFeatureFilter();
    bmf.filterBoundaryFeatures(inputFC, param.distanceTolerance);

    computeOverlaps(param.overlapPctTolerance / 100.0);
    computeAdjustedSubjectFeatures(param);
    if (param.insertRefVertices)
      computeAdjustedReferenceFeatures(param);

DebugTimer.logEventStatic("match edges end");
  }

  /**
   * Computes a new collection of features with updated features
   * replaced by their adjusted versions.
   * @param i
   * @param fc
   * @return a new collection containing both the adjusted features and any unaltered features
   *
   * @deprecated
   */
  public FeatureCollection getUpdatedFeatures(int i, FeatureCollection fc)
  {
    return updates[i].applyUpdates(fc);
  }
  /**
   * Computes a new collection of features with updated features
   * replaced by their adjusted versions.
   *
   * @param i the index of the input feature collection
   * @return a new collection containing both the adjusted features and any unaltered features
   */
  public FeatureCollection getUpdatedFeatures(int i)
  {
    return updates[i].applyUpdates(inputFC[i]);
  }

  /**
   * Computes the features that have actually been adjusted
   * @param i
   * @return a collection containing the adjusted features
   */
  public FeatureCollection getAdjustedFeatures(int i) {
    FeatureCollection fc = new FeatureDataset(inputFC[i].getFeatureSchema());
    // no adjustment made
    if (i == REFERENCE && ! param.insertRefVertices) return fc;

    Collection bfColl = bmf.getBoundaryFeatures(i);

    for (Iterator it = bfColl.iterator(); it.hasNext(); ) {
      BoundaryFeature bf = (BoundaryFeature) it.next();
      // don't adjust overlapping features
      if (bf.isOverlapping()) {
        continue;
      }
      //
      Feature f = null;
      if (bf.isAdjusted()) {
        Geometry g = bf.getAdjustedGeometry();
        f = new BasicFeature(fc.getFeatureSchema());
        f.setGeometry(g);
        fc.add(f);
        // record this feature as an update to the original
        updates[i].update(bf.getFeature(), f);
      }
    }
    return fc;
  }

  public FeatureCollection getOverlapping(int i) {
    FeatureCollection fc = new FeatureDataset(inputFC[i].getFeatureSchema());
    for (Iterator it = bmf.getBoundaryFeatures(i).iterator(); it.hasNext(); ) {
      BoundaryFeature bf = (BoundaryFeature) it.next();
      if (bf.isOverlapping) {
        fc.add(bf.getFeature());
      }
    }
    return fc;
  }

  public FeatureCollection getAdjustedVertexIndicators() {
    List ind = new ArrayList();
    for (Iterator i = bmf.getBoundaryFeatures(SUBJECT).iterator(); i.hasNext(); ) {
      BoundaryFeature bf = (BoundaryFeature) i.next();
      ind.addAll(bf.getAdjustedVertexIndicators());
    }
    return FeatureDatasetFactory.createFromGeometryWithLength(ind, ATTR_ADJ_DISTANCE);
  }

  public FeatureCollection getAdjustedEdgeIndicators(int i) {
    List edges = new ArrayList();
    for (Iterator ii = bmf.getBoundaryFeatures(i).iterator(); ii.hasNext(); ) {
      BoundaryFeature bf = (BoundaryFeature) ii.next();
      edges.addAll(bf.getAdjustedEdgeIndicators());
    }
    return FeatureDatasetFactory.createFromGeometry(edges);
  }

  private void computeOverlaps(
                    double overlapTolerance)
  {
    for (Iterator i = bmf.getBoundaryFeatures(REFERENCE).iterator(); i.hasNext(); ) {
      BoundaryFeature bf = (BoundaryFeature) i.next();
      bf.computeOverlaps(overlapTolerance);
    }
  }


  private void computeAdjustedSubjectFeatures(BoundaryMatcherParameters param) {
    SegmentMatcher segMatcher = new SegmentMatcher(param.distanceTolerance, param.angleTolerance);
    for (Iterator i = bmf.getBoundaryFeatures(SUBJECT).iterator(); i.hasNext(); ) {
      BoundaryFeature subFeat = (BoundaryFeature) i.next();
      // don't process overlapping features
      if (subFeat.isOverlapping()) continue;
      subFeat.computeShellMatches(segMatcher);
      subFeat.computeVertexMatches(segMatcher.getDistanceTolerance());
      subFeat.computeAdjusted();
    }
  }

  /**
   * Computes Reference dataset adjustments.
   * This routine depends on Subject dataset adjustments being computed first.
   */
  private void computeAdjustedReferenceFeatures(BoundaryMatcherParameters param)
  {
    for (Iterator i = bmf.getBoundaryFeatures(REFERENCE).iterator(); i.hasNext(); ) {
      BoundaryFeature refFeat = (BoundaryFeature) i.next();
      // don't process overlapping features
      if (refFeat.isOverlapping()) continue;
      refFeat.computeAdjusted();
    }
  }

}
