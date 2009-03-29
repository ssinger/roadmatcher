package com.vividsolutions.jcs.conflate.roads.match;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.*;
import com.vividsolutions.jts.index.strtree.*;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jump.geom.*;

/**
 * A spatial index on {@link RoadSegment}s
 */
public class RoadSegmentIndex {

  private SpatialIndex index = new STRtree();

  public RoadSegmentIndex(Collection roadSegments)
  {
    this(roadSegments.iterator());
  }

  public RoadSegmentIndex(Iterator edgeIt)
  {
    buildRoadEdgeIndex(edgeIt);
  }

  private void buildRoadEdgeIndex(Iterator edgeIt)
  {
    while (edgeIt.hasNext()) {
      RoadSegment edge = (RoadSegment) edgeIt.next();
      index.insert(edge.getLine().getEnvelopeInternal(), edge);
    }
  }

  public List query(RoadSegment edge, double queryBufferDistance)
  {
    Envelope queryEnv = EnvelopeUtil.expand(
        edge.getLine().getEnvelopeInternal(),
        queryBufferDistance);

    return index.query(queryEnv);
  }

}