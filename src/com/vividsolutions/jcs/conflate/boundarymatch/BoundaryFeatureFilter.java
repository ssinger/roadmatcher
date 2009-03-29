

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
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.IndexedFeatureCollection;
import com.vividsolutions.jump.util.DebugTimer;
import com.vividsolutions.jts.geom.*;

import java.util.*;

/**
 * Finds features from a FeatureCollection which are
 * within a specified distance of Features in another FeatureCollection,
 * and vice-versa.
 * <p>
 * The filter indexes the FeatureCollections to speed up processing.
 * It also computes the isNear relation in both directions using a spatial
 * index on only one of the FeatureCollections.  It does this by
 * recording the relationship in both directions for each pair of
 * near features found by iterating over one of the collections.
 */
public class BoundaryFeatureFilter {

  /**
   *  Expand an Envelope by a given distance (pos or neg)
   */
  public static Envelope expand(Envelope env, double distance) {
    /**
     *  If creating a negative buffer, check if Envelope becomes null (0-size)
     */
    if (distance < 0) {
      double minSize = 2.0 * -distance;
      if (env.getWidth() < minSize) {
        return new Envelope();
      }
      if (env.getHeight() < minSize) {
        return new Envelope();
      }
    }
    return new Envelope(
                env.getMinX() - distance,
                env.getMaxX() + distance,
                env.getMinY() - distance,
                env.getMaxY() + distance );

  }

  private Map[] bfMap = { new HashMap(), new HashMap() };
  private Collection[] bf = new Collection[2];  // cache for computed Collections

  public BoundaryFeatureFilter() {
  }

  /**
   *  Gets the BoundaryFeature corresponding to the Feature f.
   *  Creates a new BoundaryFeature if none exists.
   */
  public BoundaryFeature getBoundaryFeature(int datasetRole, Feature f) {
    BoundaryFeature bf = (BoundaryFeature) bfMap[datasetRole].get(f);
    if (bf == null) {
      bf = new BoundaryFeature(datasetRole, f);
      bfMap[datasetRole].put(f, bf);
    }
    return bf;
  }

  public Collection getBoundaryFeatures(int i)
  {
    if (bf[i] == null) {
      bf[i] = new ArrayList(bfMap[i].values());
    }

    return bf[i];
  }

  public void filterBoundaryFeatures(
      FeatureCollection[] fc,
      double maxDist
      ) {
    //<<TODO:AESTHETICS>> The DebugTimer should be enabled/disabled based on some
    //global variable, which could be set using a command-line argument.
    //We don't want DebugTimer output on by default. [Jon Aquino]
    //DebugTimer.logEventStatic("BoundaryFeatureFilter start run");
    filterBoundaryFeaturesWithIndex(fc, maxDist);
    //DebugTimer.logEventStatic("BoundaryFeatureFilter end run");
  }

  public void filterBoundaryFeaturesWithIndex(
      FeatureCollection[] fc,
      double maxDist
      ) {
    List boundaryFeatures = new ArrayList();
    //FeatureCollection indexedFC = fc[1];  // MD - time testing only
    FeatureCollection indexedFC = new IndexedFeatureCollection(fc[1]);
    DebugTimer.logEventStatic("BoundaryFeatureFilter build");
    for (Iterator i = fc[0].iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      List nearFeatList = findNearFeatures(f, indexedFC, maxDist);
      if (nearFeatList != null && nearFeatList.size() > 0) {
        BoundaryFeature bf = getBoundaryFeature(0, f);
        addNearFeatures(bf, nearFeatList);
      }
    }
  }

  private void addNearFeatures(BoundaryFeature bf, List nearFeatList)
  {
    for (int i = 0; i < nearFeatList.size(); i++) {
      Feature f = (Feature) nearFeatList.get(i);
      BoundaryFeature bfNear = getBoundaryFeature(1, f);
      // "near" is a symmetric relation,
      // so record that each feature is near the other
      bf.addNearFeature(bfNear);
      bfNear.addNearFeature(bf);
    }
  }

  /**
   *
   * @param f
   * @param targetFC
   * @param maxDist
   * @return null or empty list if no features found
   */
  public List findNearFeatures(
      Feature f,
      FeatureCollection targetFC,
      double maxDist) {
    // preliminary filter by envelope
    List nearEnvList = findNearByEnvelope(f, targetFC, maxDist);
    // refine search to use exact distance
    if (nearEnvList.size() == 0) return null;
    List nearDistList = findNearByDistance(f, nearEnvList, maxDist);

    return nearDistList;
  }

  public List findNearByEnvelope(
      Feature f,
      FeatureCollection fc,
      double maxDist) {
    Envelope fEnv = f.getGeometry().getEnvelopeInternal();
    Envelope searchEnv = expand(fEnv, maxDist);
    List result = fc.query(searchEnv);
//System.out.println("result size = " + result.size());
    return result;
  }

  /**
   *
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
