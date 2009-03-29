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
package com.vividsolutions.jcs.simplify;

import java.util.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.task.*;

public class FeatureShortSegmentRemover {

  private FeatureCollection features;
  private FeatureCollection adjustedFC;
  private double minLength;
  private double maxDisplacement;
  private int updateCount = 0;
  private int segmentsRemovedCount = 0;

  public FeatureShortSegmentRemover(FeatureCollection features, double minLength, double maxDisplacement)
  {
    this.features = features;
    this.minLength = minLength;
    this.maxDisplacement = maxDisplacement;
  }

  public int getUpdateCount()
  {
    return updateCount;
  }

  public int getSegmentsRemovedCount() { return segmentsRemovedCount; }

  public FeatureCollection process(TaskMonitor monitor)
  {
    FeatureUpdateRecorder updates =  new FeatureUpdateRecorder();
    int totalSegments = features.size();
    int count = 0;
    for (Iterator i = features.iterator(); i.hasNext(); ) {
      monitor.report(++count, totalSegments, "features");
      Feature f = (Feature) i.next();
      Geometry geom = f.getGeometry();
      GeometryShortSegmentRemover remover = new GeometryShortSegmentRemover(geom, minLength, maxDisplacement);
      Geometry newGeom = remover.getResult();
      if (remover.isModified()) {
        // don't update geometry if it's not valid
        if (newGeom.isValid()) {
          Feature newFeat = f.clone(false);
          newFeat.setGeometry(newGeom);
          // record this feature as an update to the original
          updates.update(f, newFeat);
          segmentsRemovedCount += remover.getSegmentsRemovedCount();
        }
      }
    }
    updateCount = updates.getCount();

    return updates.applyUpdates(features);
  }
}
