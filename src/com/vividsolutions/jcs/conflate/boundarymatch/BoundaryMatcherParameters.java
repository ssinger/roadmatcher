

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

/**
 * The parameters provided to control the BoundaryMatching process.
 */
public class BoundaryMatcherParameters {

  /**
   * The distance below which segments and vertices are considered to match
   */
  public double distanceTolerance = 1.0;

  /**
   * The maximum angle between matching segments.
   */
  public double angleTolerance = 22.5;

  /**
   * Controls whether vertices are inserted into  shells in the Reference dataset
   */
  public boolean insertRefVertices = true;

   /**
    * Controls whether vertices may be deleted from the Subject dataset if
    * they are not present in the Reference dataset
    */
  public boolean deleteSubVertices = false;
  /**
   * The percentage by which features must overlap in order to be removed
   * from the boundary matching process.
   */
  public double overlapPctTolerance = 50.0;

  public BoundaryMatcherParameters()
  {
  }
}
