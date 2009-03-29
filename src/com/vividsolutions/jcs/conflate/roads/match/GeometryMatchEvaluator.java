package com.vividsolutions.jcs.conflate.roads.match;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Computes a match confidence value for a given Geometry pair.
 * Match values are in the range [0..1], with 1 indicating a better match
 * than 0.
 * A useful convention is that 0 indicates a match that is so poor it can be discarded
 * (or not recorded for further processing).
 */
public interface GeometryMatchEvaluator {

  double match(Geometry g0, Geometry g1);
}