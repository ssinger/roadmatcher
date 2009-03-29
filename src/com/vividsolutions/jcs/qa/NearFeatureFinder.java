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
package com.vividsolutions.jcs.qa;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.geom.*;

/**
 * Finds Features which are within a given distance of another FeatureCollection
 */
public class NearFeatureFinder {

  public NearFeatureFinder()
  {
  }

  public FeatureCollection getNearFeatures(FeatureCollection fc0, FeatureCollection fc1, double distance)
  {
    Set nearFeaturesSet = new TreeSet();
    FeatureCollection indexedFC = new IndexedFeatureCollection(fc1);
    for (Iterator i = fc0.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      List nearFeatList = findNearFeatures(f, indexedFC, distance);
      if (nearFeatList != null && nearFeatList.size() > 0) {
        nearFeaturesSet.addAll(nearFeatList);
      }
    }
    FeatureCollection nearFeatures = new FeatureDataset(fc0.getFeatureSchema());
    nearFeatures.addAll(nearFeaturesSet);
    return nearFeatures;
  }
  /**
   * Finds the features from the FeatureCollection <code>targetFC</code>
   * which are within <code>maxDist</code>
   * of the feature <code>f</code>.
   * For efficiency <code>targetFC</code> should be indexed.
   *
   * @param f
   * @param targetFC
   * @param maxDist
   * @return null or empty list if no features found
   */
  public List findNearFeatures(
      Feature f,
      FeatureCollection targetFC,
      double maxDist)
  {
    // preliminary filter by envelope
    List nearEnvList = findNearByEnvelope(f, targetFC, maxDist);
    // refine search to use exact distance
    if (nearEnvList.size() == 0) return null;
    List nearDistList = findNearByDistance(f, nearEnvList, maxDist);

    return nearDistList;
  }

  /**
   * Finds the features from the list <code>fList</code>
   * which are within <code>maxDist</code>
   * of the envelope of the feature <code>f</code>
   * @param f
   * @param fc
   * @param maxDist
   * @return null or empty list if no near features were found
   */
  public List findNearByEnvelope(
      Feature f,
      FeatureCollection fc,
      double maxDist) {
    Envelope fEnv = f.getGeometry().getEnvelopeInternal();
    Envelope searchEnv = EnvelopeUtil.expand(fEnv, maxDist);
    List result = fc.query(searchEnv);
//System.out.println("result size = " + result.size());
    return result;
  }

  /**
   *  Finds the features from the list <code>fList</code> which are within <code>maxDist</code>
   * of the feature <code>f</code>
   * @param f
   * @param fList
   * @param maxDist
   * @return null or empty list if no near features were found
   */
  public List findNearByDistance(
      Feature f,
      List fList,
      double maxDist) {
    List result = null;
    for (int i = 0; i < fList.size(); i++) {
      Feature f2 = (Feature) fList.get(i);
      Geometry g = f2.getGeometry();
      if (f.getGeometry().distance(g) <= maxDist) {
        if (result == null) result = new ArrayList();
        result.add(f2);
      }
    }
    return result;
  }

}
