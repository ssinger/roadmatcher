package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class FUTURE_LineString {
    public static Coordinate first(LineString lineString) {
        return lineString.getCoordinateN(0);
    }
    
    public static Coordinate last(LineString lineString) {
        return lineString.getCoordinateN(lineString.getNumPoints()-1);
    }
}
