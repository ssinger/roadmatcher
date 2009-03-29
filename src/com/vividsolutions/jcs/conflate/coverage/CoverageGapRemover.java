

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
import com.vividsolutions.jcs.qa.InternalMatchedSegmentFinder;
import com.vividsolutions.jcs.conflate.boundarymatch.*;
import com.vividsolutions.jump.task.*;
/**
 * Removes gaps and overlaps from FeatureDatasets that
 * are intended to have coverage topology.
 */
public class CoverageGapRemover
{
  public static boolean hasMultiPolygonFeature(List featureList)
  {
    for (Iterator i = featureList.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      if (f.getGeometry() instanceof MultiPolygon)
          return true;
    }
    return false;
  }

  public static class Parameters
  {
      public Parameters(){}
      public Parameters(double distanceTolerance, double angleTolerance) {
          this.distanceTolerance = distanceTolerance;
          this.angleTolerance = angleTolerance;
      }
    /**
     * The distance tolerance below which segments and vertices are considered to match
     */
    public double distanceTolerance = 1.0;

    /**
     * The maximum angle between matching segments.
     */
    public double angleTolerance = 22.5;
  }

  private Parameters param;
  private Coverage cvg;
  private FeatureCollection matchedFC;
  private TaskMonitor monitor;

  public CoverageGapRemover(FeatureCollection subjectFC, TaskMonitor monitor)
  {
    cvg = new Coverage(subjectFC);
    this.monitor = monitor;
  }

  public void process(Parameters param)
  {
    this.param = param;
    monitor.report("Matching segments");

    InternalMatchedSegmentFinder.Parameters msfParam
        = new InternalMatchedSegmentFinder.Parameters();
    msfParam.distanceTolerance = param.distanceTolerance;
    msfParam.angleTolerance = param.angleTolerance;
    InternalMatchedSegmentFinder msf = new InternalMatchedSegmentFinder(cvg.getFeatures(), msfParam);
    matchedFC = msf.getMatchedFeatures();

    if (monitor.isCancelRequested()) return;
    adjustNearFeatures(matchedFC);
  }

  public FeatureCollection getMatchedFeatures()
  {
    return matchedFC;
  }

  public FeatureCollection getAdjustedFeatures()
  {
    return cvg.getAdjustedFeatures();
  }

  public FeatureCollection getUpdatedFeatures()
  {
    return cvg.getUpdates().applyUpdates(cvg.getFeatures());
  }
  public FeatureCollection getAdjustmentIndicators()
  {
    return cvg.getAdjustmentIndicators();
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
  private void adjustNearFeatures(FeatureCollection matchedFC)
  {
    monitor.report("Adjusting features");
    SegmentMatcher segMatcher = new SegmentMatcher(param.distanceTolerance, param.angleTolerance);
    NearFeatureFinder nff = new NearFeatureFinder(cvg.getFeatures());
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
    int totalFeatures = matchedFC.size();
    for (Iterator i = matchedFC.iterator(); i.hasNext(); ) {
      if (monitor.isCancelRequested()) return;
      Feature f = (Feature) i.next();
      featuresProcessed++;
      monitor.report(featuresProcessed, totalFeatures, "features");
      List nearFeat = nff.findNearFeatures(f, param.distanceTolerance);

      // currently only polygons are handled
      if (! (f.getGeometry() instanceof Polygon)) continue;
      if (hasMultiPolygonFeature(nearFeat)) continue;

      CoverageFeature cgf = cvg.getCoverageFeature(f);
      // don't bother if already processed
      if (cgf.isProcessed())  continue;
      cgf.computeAdjustment(cvg.getCoverageFeatureList(nearFeat), segMatcher);

    }
    cvg.computeAdjustedFeatureUpdates();
  }

}
