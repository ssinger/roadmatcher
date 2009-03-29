package com.vividsolutions.jcs.jump;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;

public class FUTURE_CoordinateArrays {
	private static class MyCoordinateArrays extends CoordinateArrays {
		public static void myScroll(Coordinate[] coordinates, Coordinate firstCoordinate) {
			CoordinateArrays.scroll(coordinates, firstCoordinate);
		}
	}
	public static void scroll(Coordinate[] coordinates, Coordinate firstCoordinate) {
		MyCoordinateArrays.myScroll(coordinates, firstCoordinate);
	}
}
