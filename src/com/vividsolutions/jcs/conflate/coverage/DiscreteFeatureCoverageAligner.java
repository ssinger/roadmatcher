
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
 * Aligns a dataset of discrete (not necessarily related) features to a coverage.
 */
public class DiscreteFeatureCoverageAligner
{

  private FeatureCollection[] inputFC;
  private FeatureCollection updatedFC;
  private FeatureCollection adjustmentIndFC;
  private FeatureCollection adjustedFC;
  private TaskMonitor monitor;

  public DiscreteFeatureCoverageAligner(FeatureCollection referenceFC,
                                        FeatureCollection subjectFC,
                                        TaskMonitor monitor)
  {
    inputFC = new FeatureCollection[] { referenceFC, subjectFC};
    updatedFC = new FeatureDataset(subjectFC.getFeatureSchema());
    this.monitor = monitor;
  }

  public void process(CoverageAligner.Parameters param)
  {
    monitor.report("Aligning Features...");

    int featuresProcessed = 0;
    int totalFeatures = inputFC[1].getFeatures().size();

    IndexedFeatureCollection refIndexFC = new IndexedFeatureCollection(inputFC[0]);

    for (Iterator i = inputFC[1].iterator(); i.hasNext(); ) {

      if (monitor.isCancelRequested()) return;
      featuresProcessed++;
      monitor.report(featuresProcessed, totalFeatures, "features");

      Feature f = (Feature) i.next();
      align(param, f, refIndexFC);
    }
  }

  public FeatureCollection getAdjustedFeatures()
  {
    return adjustedFC;
  }

  public FeatureCollection getUpdatedFeatures()
  {
    return updatedFC;
  }

  public FeatureCollection getAdjustmentIndicators()
  {
    return adjustmentIndFC;
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
  private void align(CoverageAligner.Parameters param, Feature f, IndexedFeatureCollection refIndexFC)
  {
    FeatureCollection subjectFC = new FeatureDataset(f.getSchema());
    subjectFC.add(f);
    CoverageAligner cvgAligner = new CoverageAligner(inputFC[0], subjectFC, new DummyTaskMonitor());
    cvgAligner.setReferenceIndex(refIndexFC);
    cvgAligner.process(param);
    updatedFC.addAll(cvgAligner.getUpdatedFeatures().getFeatures());

    // add adjusted feature to collection (if any)
    FeatureCollection featureAdjustedFC = cvgAligner.getAdjustedFeatures();
    if (featureAdjustedFC.size() > 0) {
      if (adjustedFC == null) {
        adjustedFC = new FeatureDataset(featureAdjustedFC.getFeatureSchema());
      }
      adjustedFC.addAll(featureAdjustedFC.getFeatures());
    }

    // add adjustment indicators to collection (if any)
    FeatureCollection featureAdjustmentIndFC = cvgAligner.getAdjustmentIndicators();
    if (featureAdjustmentIndFC.size() > 0) {
      if (adjustmentIndFC == null) {
        adjustmentIndFC = new FeatureDataset(featureAdjustmentIndFC.getFeatureSchema());
      }
      adjustmentIndFC.addAll(featureAdjustmentIndFC.getFeatures());
    }
  }

}
