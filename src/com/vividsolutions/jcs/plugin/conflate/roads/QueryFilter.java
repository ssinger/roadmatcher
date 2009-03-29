package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;

/**
 * A filter which controls what segments are displayed
 * in the {@link QueryToolboxPanel} window
 */
public abstract class QueryFilter extends Block {
  public abstract boolean include(SourceFeature feature);

  public Object yield(Object feature) {
    return Boolean.valueOf(include((SourceFeature) feature));
  }
}

