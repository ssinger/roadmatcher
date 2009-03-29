package com.vividsolutions.jcs.geom;

import java.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * Builds a MultiLineString incrementally.
 *
 * @version 1.0
 */
public class MultiLineStringBuilder {

  private GeometryFactory geomFact;
  private List lines = new ArrayList();
  private CoordinateList coordList = null;
  private boolean ignoreInvalidLines = false;
  private Coordinate lastPt = null;

  public MultiLineStringBuilder(GeometryFactory geomFact) {
    this.geomFact = geomFact;
  }

  /**
   * Allows invalid lines to be ignored rather than causing Exceptions.
   * An invalid line is one which has only one unique point.
   *
   * @param ignoreShortLines <code>true</code> if short lines are to be ignored
   */
  public void setIgnoreInvalidLines(boolean ignoreInvalidLines)
  {
    this.ignoreInvalidLines = ignoreInvalidLines;
  }

  /**
   * Adds a point to the current line.
   *
   * @param pt the Coordinate to add
   */
  public void add(Coordinate pt)
  {
    add(pt, true);
  }

  /**
   * Adds a point to the current line.
   *
   * @param pt the Coordinate to add
   */
  public void add(Coordinate pt, boolean allowRepeatedPoints)
  {
    if (coordList == null)
      coordList = new CoordinateList();
    coordList.add(pt, allowRepeatedPoints);
    lastPt = pt;
  }

  public Coordinate getLastCoordinate() { return lastPt; }

  /**
   * Terminate the current LineString.
   */
  public void endLine()
  {
    if (coordList == null) {
      return;
    }
    if (ignoreInvalidLines && coordList.size() < 2) {
      coordList = null;
      return;
    }
    Coordinate[] pts = coordList.toCoordinateArray();
    coordList = null;
    LineString line = null;
    try {
      line = geomFact.createLineString(pts);
    }
    catch (IllegalArgumentException ex) {
      // exception is due to too few points in line.
      // only propagate if not ignoring short lines
      if (! ignoreInvalidLines)
        throw ex;
    }

    if (line != null) lines.add(line);
  }

  public Geometry getGeometry()
  {
    // end last line in case it was not done by user
    endLine();
    return geomFact.buildGeometry(lines);
  }
}