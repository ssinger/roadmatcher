
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
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;

import com.vividsolutions.jcs.conflate.boundarymatch.*;
import com.vividsolutions.jump.task.*;

/**
 * Aligns one coverage to another
 */
public class CoverageAligner
{
  public static final int REFERENCE = 0;
  public static final int SUBJECT = 1;


  public static class Parameters
  {
    /**
     * The distance tolerance below which segments and vertices are considered to match
     */
    public double distanceTolerance = 1.0;

    /**
     * The maximum angle between matching segments.
     */
    public double angleTolerance = 22.5;
  }

  private FeatureCollection subjectFC;
  private Parameters param;
  private FeatureCollection matchedFC;
  private TaskMonitor monitor;
  private Coverage[] cvg = new Coverage[2];
  private IndexedFeatureCollection refIndexedFC = null;

  public CoverageAligner(FeatureCollection referenceFC, FeatureCollection subjectFC, TaskMonitor monitor)
  {
    cvg[REFERENCE] = new Coverage(referenceFC);
    cvg[SUBJECT] = new Coverage(subjectFC);
    this.monitor = monitor;
  }

  public void process(Parameters param)
  {
    this.param = param;
    adjustNearFeatures();
  }

  public FeatureCollection getAdjustedFeatures()
  {
    return cvg[SUBJECT].getAdjustedFeatures();
  }

  public FeatureCollection getUpdatedFeatures()
  {
    return cvg[SUBJECT].getUpdates().applyUpdates(cvg[SUBJECT].getFeatures());
  }
  public FeatureCollection getAdjustmentIndicators()
  {
    return cvg[SUBJECT].getAdjustmentIndicators();
  }

  public void setReferenceIndex(IndexedFeatureCollection refIndexedFC)
  {
    this.refIndexedFC = refIndexedFC;
  }
  /**
   * Process all features in the FeatureCollection, computing adjustments
   * for them to match their neighbour features.
   * To ensure each feature is processed once only, the relation "isNearTo"
   * is traversed in breadth-first order, and each feature is processed as it
   * is encountered.
   *
   * @param fc the collection of Features forming a coverage to be processed.
   */
  private void adjustNearFeatures()
  {
    monitor.report("Adjusting features");
    SegmentMatcher segMatcher = new SegmentMatcher(param.distanceTolerance, param.angleTolerance, SegmentMatcher.EITHER_ORIENTATION);
    NearFeatureFinder nff = null;
    if (refIndexedFC != null)
      nff = new NearFeatureFinder(refIndexedFC);
    else
      nff = new NearFeatureFinder(cvg[REFERENCE].getFeatures());
    /**
     * MD - can we get away with only comparing matched features?
     * This would be faster, since fewer features are in the index.
     * (MD - actually doesn't appear to make much overall speed difference)
     * However, it may cause problems with coverage consistency
     * (non-matched features may still share vertices which are adjusted, and
     * thus must be adjusted themselves)
     */
    //NearFeatureFinder nff = new NearFeatureFinder(matchedFC);
    int featuresProcessed = 0;
    int totalFeatures = cvg[SUBJECT].getFeatures().size();
    for (Iterator i = cvg[SUBJECT].getFeatures().iterator(); i.hasNext(); ) {
      if (monitor.isCancelRequested()) return;
      Feature f = (Feature) i.next();
      featuresProcessed++;
      monitor.report(featuresProcessed, totalFeatures, "features");
      List nearFeat = nff.findNearFeatures(f, param.distanceTolerance);

      // currently only polygons are handled
      if (! (f.getGeometry() instanceof Polygon)) continue;
      if (CoverageGapRemover.hasMultiPolygonFeature(nearFeat)) continue;
      // don't bother processing if there's nothing to adjust to
      if (nearFeat.size() <= 0) continue;

      CoverageFeature cgf = cvg[SUBJECT].getCoverageFeature(f);
      // don't bother if already processed
      if (cgf.isProcessed())  continue;
      cgf.computeAdjustment(cvg[REFERENCE].getCoverageFeatureList(nearFeat), segMatcher);

    }
    cvg[SUBJECT].computeAdjustedFeatureUpdates();
  }




}
