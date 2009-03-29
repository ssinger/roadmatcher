

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
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.task.*;

/**
 * Keeps a count of
 * distinct LineSegments (as a Map from LineSegments to counts).
 * It can be used to retrieve a list of unique segments (up to point order).
 * LineSegments are normalized before being counted
 * (so the segment comparison is independent of point order).
 * <p>
 * Zero-length segments can be ignored if required.  This is useful
 * for handling geometries with repeated points.
 */
public class FeatureSegmentCounter {

  private List segmentList = new ArrayList();
  private Map segmentMap = new TreeMap();
  private LineSegment querySegment = new LineSegment();
  private boolean countZeroLengthSegments = true;
  private TaskMonitor monitor;
  private Envelope fence = null;
  private LineSegmentEnvelopeIntersector lineEnvInt;

  /**
   * Creates a new counter, allowing control over
   * whether zero-length segments are counted.
   *
   * @param countZeroLengthSegments if <code>false</code>, zero-length segments will be ignored
   */
  public FeatureSegmentCounter(boolean countZeroLengthSegments, TaskMonitor monitor) {
    this.countZeroLengthSegments = countZeroLengthSegments;
    this.monitor = monitor;
  }

  public void setFence(Envelope fence)
  {
    this.fence = fence;
    lineEnvInt = new LineSegmentEnvelopeIntersector();
  }

  public void add(FeatureCollection fc)
  {
    monitor.allowCancellationRequests();
    monitor.report("Adding features to feature-segment counter");
    int totalFeatures = fc.size();
    int j = 0;
    for (Iterator i = fc.iterator(); i.hasNext() && ! monitor.isCancelRequested(); ) {
      Feature feature = (Feature) i.next();
      j++;
      monitor.report(j, totalFeatures, "features");
      add(feature);
    }
  }
  public void add(Feature f)
  {
    Geometry g = f.getGeometry();
    // skip if using fence and feature is not in fence
    if (fence != null && ! g.getEnvelopeInternal().intersects(fence))
      return;

    List coordArrays = CoordinateArrays.toCoordinateArrays(g, true);
    for (Iterator i = coordArrays.iterator(); i.hasNext(); ) {
      Coordinate[] coord = (Coordinate[]) i.next();
      for (int j = 0; j < coord.length - 1; j++) {
        // skip if using fence AND seg is not in fence
        if (fence != null)
          if (! lineEnvInt.touches(coord[j], coord[j + 1], fence))
            continue;
        FeatureSegment fs = new FeatureSegment(f, j, coord[j], coord[j + 1]);
        add(fs);
        segmentList.add(fs);
      }
    }
  }

  public void add(LineSegment seg)
  {
    add(seg.p0, seg.p1, seg);
  }

  public void add(Coordinate p0, Coordinate p1, LineSegment originalSegment)
  {
    // check for zero-length segment
    boolean isZeroLength = p0.equals(p1);
    if (! countZeroLengthSegments && isZeroLength)
      return;

    LineSegment lineseg = new LineSegment(p0, p1);
    lineseg.normalize();

    FeatureSegmentCount count = (FeatureSegmentCount) segmentMap.get(lineseg);
    if (count == null) {
      segmentMap.put(lineseg, new FeatureSegmentCount(1, originalSegment));
    }
    else {
      count.increment();
    }
  }

  public Collection values() { return segmentMap.values(); }

  public List getSegments() { return segmentList; }
  /**
   *
   * @return a List of unique LineSegments or FeatureSegments
   */
  public List getUniqueSegments()
  {
    List unique = new ArrayList();
    for (Iterator i = segmentMap.values().iterator(); i.hasNext(); ) {
      FeatureSegmentCount count = (FeatureSegmentCount) i.next();
      if (count.getCount() == 1)
        unique.add(count.getSegment());
    }
    return unique;
  }

  public LineSegment getSegment(LineSegment seg)
  {
    return getSegment(seg.p0, seg.p1);
  }

  public LineSegment getSegment(Coordinate p0, Coordinate p1)
  {
    querySegment.setCoordinates(p0, p1);
    querySegment.normalize();
    FeatureSegmentCount count = (FeatureSegmentCount) segmentMap.get(querySegment);
    return count.getSegment();
  }

  public int getCount(LineSegment seg)
  {
    return getCount(seg.p0, seg.p1);
  }

  public int getCount(Coordinate p0, Coordinate p1)
  {
    querySegment.setCoordinates(p0, p1);
    querySegment.normalize();
    FeatureSegmentCount count = (FeatureSegmentCount) segmentMap.get(querySegment);
    return count.getCount();
  }

  public class FeatureSegmentCount {

    private int count = 0;
    private LineSegment seg;

    public FeatureSegmentCount(int value, LineSegment seg) {
      this.count = value;
      this.seg = seg;
    }

    public int getCount() { return count; }
    public LineSegment getSegment() { return seg; }

    public void increment()
    {
      count++;
    }
}}
