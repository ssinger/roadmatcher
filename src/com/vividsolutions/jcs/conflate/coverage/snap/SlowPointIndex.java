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
package com.vividsolutions.jcs.conflate.coverage.snap;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jcs.feature.*;

public class SlowPointIndex {

  private List indexPts = new ArrayList();

  public SlowPointIndex()
  {
  }

  public void add(FeatureCollection fc)
  {
    add(fc.getFeatures());
  }

  public void add(List featureList)
  {
    for (Iterator i = featureList.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      Geometry g = f.getGeometry();
      add(g.getCoordinates());
    }
  }

  public void add(Coordinate[] pts)
  {
    for (int i = 0; i < pts.length; i++) {
      add(pts[i]);
    }
  }

  public void add(Coordinate pt)
  {
    indexPts.add(pt);
  }

  public List query(Envelope queryEnv)
  {
    List result = new ArrayList();
    for (Iterator i = indexPts.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      if (queryEnv.contains(p))
          result.add(p);
    }
    return result;
  }
}
