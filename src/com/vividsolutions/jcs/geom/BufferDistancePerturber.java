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
package com.vividsolutions.jcs.geom;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.AssertionFailedException;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * Improves the robustness of buffer computation by using small
 * perturbations of the buffer distance.  Also used enhanced precision.
 */
public class BufferDistancePerturber {

  public static Geometry safeBuffer(Geometry geom, double distance)
  {
    Geometry buffer = null;
    try {
      buffer = EnhancedPrecisionOp.buffer(geom, distance);
    }
    catch (AssertionFailedException ex) {
      // eat the exception
    }
    return buffer;
  }

  private double distance;
  private double maximumPerturbation;

  public BufferDistancePerturber(double distance, double maximumPerturbation)
  {
    this.distance = distance;
    this.maximumPerturbation = maximumPerturbation;
  }

  /**
   * Attempts to compute a buffer using small perturbations of the buffer distance
   * if necessary.  If this routine is unable to perform the buffer computation correctly
   * the orginal buffer exception will be propagated.
   *
   * @param geom the Geometry to compute the buffer for
   * @return the buffer of the input Geometry
   */
  public Geometry buffer(Geometry geom)
  {
    Geometry buffer = safeBuffer(geom, distance);
    if (isBufferComputedCorrectly(geom, buffer))
      return buffer;
    else {
System.out.println("buffer robustness error found");
System.out.println(geom);
    }
    buffer = safeBuffer(geom, distance + maximumPerturbation);
    if (isBufferComputedCorrectly(geom, buffer)) return buffer;

    return geom.buffer(distance - maximumPerturbation);
  }

  /**
   * Check various assertions about the geometry and the buffer to
   * try to determine whether the JTS buffer function failed to compute
   * the buffer correctly.  These are heuristics only - this may not catch all errors
   *
   * @param geom the geometry
   * @param buffer the buffer computed by JTS
   * @return <code>true</code> if the buffer seems to be correct
   */
  private boolean isBufferComputedCorrectly(Geometry geom, Geometry buffer)
  {
    if (buffer == null) return false;
    // sometimes buffer() computes empty geometries
    if (! geom.isEmpty() && buffer.isEmpty()) return false;
    // sometimes buffer() computes a very small geometry as the buffer
    if (! buffer.getEnvelopeInternal().contains(geom.getEnvelopeInternal())) return false;
    return true;
  }
}
