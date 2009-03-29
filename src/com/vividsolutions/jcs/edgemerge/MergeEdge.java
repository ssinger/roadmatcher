package com.vividsolutions.jcs.edgemerge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jcs.graph.*;

public class MergeEdge
    extends Edge
{
  private Feature feature;
  private boolean isVisited;

  public MergeEdge(Feature feature) {
    this.feature = feature;
  }

  public boolean isVisited() { return isVisited; }
  public void setVisited(boolean isVisited) { this.isVisited = isVisited; }

  public Geometry getGeometry() { return feature.getGeometry(); }

}