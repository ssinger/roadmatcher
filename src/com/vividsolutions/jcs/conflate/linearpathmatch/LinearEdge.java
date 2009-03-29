package com.vividsolutions.jcs.conflate.linearpathmatch;

import com.vividsolutions.jts.geom.*;

/**
 * A directed edge of a {@link LinearPath} based on a {@link LineString} geometry.
 * The direction of the edge is provided by the underlying LineString.
 * A parent Object may be provided, to reference this edge back to a client
 * object model.
 *
 * @version 1.0
 */
public class LinearEdge
{
  private LineString line;
  private Object context;

  public LinearEdge(LineString line, Object context) {
    this.line = line;
    this.context = context;
  }

  public LineString getGeometry() { return line; }
  public Object getContext() { return context; }
}