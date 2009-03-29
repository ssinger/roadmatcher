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

package com.vividsolutions.jcs.conflate.roads.match;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a matching between one object and another.
 * The match has a value between 0 and 1.
 */
public class MatchValue
    implements Comparable, Serializable
{

  private Object match;
  private double value;

  public MatchValue(Object match, double value)
  {
    this.match = match;
    this.value = value;
  }

  public Object getMatch() { return match; }
  public double getValue() { return value; }
  public void setValue(double value) { this.value = value; }

  /**
   *  Compares this object with the specified object for order.
   *
   *@param  o  the <code>MatchValue</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>MatchValue</code>
   *      is less than, equal to, or greater than the specified <code>MatchValue</code>
   */
  public int compareTo(Object o) {
    MatchValue other = (MatchValue) o;
    if (value < other.value) return -1;
    else if (value > other.value) return 1;
    else return 0;
  }


}
