package com.vividsolutions.jcs.conflate.linearpathmatch;

/**
 * Finds edges in a path.
 *
 * @version 1.4
 */
public interface PathTracer {

  /**
 * This method implements the strategy for finding the next edge in the path
 * @return the next {@link LinearEdge} in the path, if any, or <code>null</code>
 */
  public LinearEdge findNextEdge();

}