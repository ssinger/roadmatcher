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

//<<<<<<< GeometryShortSegmentRemover.java
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.LinearRing;
//import com.vividsolutions.jcs.geom.*;
//=======
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;

public class GeometryShortSegmentRemover {

  private Geometry geom;
  private double minLength;
  private double maxDisplacement;
  private boolean isModified = false;
  private int segmentsRemovedCount = 0;

  public GeometryShortSegmentRemover(Geometry geom, double minLength, double maxDisplacement)
  {
    this.geom = geom;
    this.minLength = minLength;
    this.maxDisplacement = maxDisplacement;
  }

  public Geometry getResult()
  {
    GeometryEditor geomEdit = new GeometryEditor();
    /**
     * GeometryEditor always creates a new geometry even if the original one wasn't modified.
     * Explicitly check for modifications and return the original if no mods were made
     */
    Geometry newGeom = geomEdit.edit(geom, new ShortSegmentRemoverCoordinateOperation());
    if (! isModified)
      return geom;
    return newGeom;
  }

  public boolean isModified() { return isModified; }

  public int getSegmentsRemovedCount() { return segmentsRemovedCount; }

  private class ShortSegmentRemoverCoordinateOperation
      extends GeometryEditor.CoordinateOperation
  {
    public Coordinate[] edit(Coordinate[] coordinates, Geometry geom)
    {
      boolean isRing = geom instanceof LinearRing;

      ShortSegmentRemover shortSegRemover = new ShortSegmentRemover(coordinates, isRing, minLength, maxDisplacement);
      if (shortSegRemover.isModified()) {
        isModified = true;
        segmentsRemovedCount += shortSegRemover.getSegmentsRemovedCount();
      }

      return shortSegRemover.getUpdatedCoordinates();
    }
  }
}
