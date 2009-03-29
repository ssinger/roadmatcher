package com.vividsolutions.jcs.conflate.linearpathmatch;

import com.vividsolutions.jts.geom.*;

/**
 * Implements a stream stream of {@link LinearEdges} produced by
 * tracing a path through a network according to a given strategy.
 * The stream is buffered (by one edge) and lazily evaluated.
 *
 * @version 1.0
 */
public class PathIterator
{
  private PathTracer tracer;
  private LinearEdge bufferEdge = null;
  private boolean bufferFull = false;

  public PathIterator(PathTracer tracer)
  {
    this.tracer = tracer;
  }

  public boolean hasNext()
  {
    if (bufferFull) return true;
    next();
    return bufferFull;
  }

  public LinearEdge next()
  {
    if (bufferFull) {
      bufferFull = false;
      return bufferEdge;
    }
    bufferEdge = tracer.findNextEdge();
    if (bufferEdge != null)
      bufferFull = true;
    return bufferEdge;
  }

  public LinearEdge lookahead()
  {
    if (bufferFull) {
      return bufferEdge;
    }
    bufferEdge = tracer.findNextEdge();
    if (bufferEdge != null)
      bufferFull = true;
    return bufferEdge;
  }

}