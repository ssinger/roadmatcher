package com.vividsolutions.jcs.conflate.roads.model;

import java.util.*;

/**
 * A comparator for RoadNode match values.
 *
 * @version 1.0
 */

public class RoadNodeMatchValueComparator
    implements Comparator
{
    public int compare(Object o1, Object o2)
    {
      RoadNode r1 = (RoadNode) o1;
      RoadNode r2 = (RoadNode) o2;
      return Double.compare(r1.getMatchValue(), r2.getMatchValue());
    }
}
