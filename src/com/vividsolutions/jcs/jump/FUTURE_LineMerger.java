package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public class FUTURE_LineMerger extends LineMerger {
    public FUTURE_LineMerger FUTURE_add(Geometry geometry) {
        super.add(geometry);
        return this;
    }
}
