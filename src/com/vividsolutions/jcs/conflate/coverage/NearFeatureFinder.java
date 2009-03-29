

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
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jts.geom.*;


/**
 * Finds all features in a FeatureCollection which are
 * within a specified distance of a given feature.
 * <p>
 * A spatial index is used to speed up the proximity querying.
 */
public class NearFeatureFinder {

  private FeatureCollection indexedFC;

  public NearFeatureFinder(FeatureCollection inputFC)
  {
     indexedFC = new IndexedFeatureCollection(inputFC);
  }

  public NearFeatureFinder(IndexedFeatureCollection inputFC)
  {
     indexedFC = inputFC;
  }

  /**
   * Finds all features within a given distance of a query feature.
   *
   * @param f the query feature
   * @param maxDist the query distance
   * @return a List of features found, or an empty list if no features found
   */
  public List findNearFeatures(
      Feature f,
      double maxDist) {
    // preliminary filter by envelope
    List nearEnvList = findNearByEnvelope(f, maxDist);
    // refine search to use exact distance
    if (nearEnvList.size() == 0) return new ArrayList();
    return findNearByDistance(f, nearEnvList, maxDist);
  }

  private List findNearByEnvelope(
      Feature f,
      double maxDist) {
    Envelope fEnv = f.getGeometry().getEnvelopeInternal();
    Envelope searchEnv = EnvelopeUtil.expand(fEnv, maxDist);
    List result = indexedFC.query(searchEnv);
//System.out.println("result size = " + result.size());
    return result;
  }

  /**
   * Finds all features from the list fList which are within the given distance
   * of a given feature.
   *
   * @param f the query feature
   * @param fList the list of features to test
   * @param maxDist the distance tolerance
   * @return a List of features found, or an empty list if no features found
   */
  private List findNearByDistance(
      Feature f,
      List fList,
      double maxDist) {
    List result = new ArrayList();
    for (int i = 0; i < fList.size(); i++) {
      Feature f2 = (Feature) fList.get(i);
      Geometry g = f2.getGeometry();
      if (f.getGeometry().distance(g) <= maxDist) {
        result.add(f2);
      }
    }
    return result;
  }

}
