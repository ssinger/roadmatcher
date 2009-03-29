package com.vividsolutions.jcs.geom;
import com.vividsolutions.jts.geom.*;
/**
 * Utility functions for {@link LineString}s
 * 
 * @version 1.0
 */
public class LineStringUtil {
	/**
	 * Creates a {@link LineString}which is the reverse of the input
	 * 
	 * @param line
	 *                   a {@link LineString}
	 * @return a {@link LineString}which is the reverse of the input
	 */
	public static LineString reverse(LineString line) {
		Coordinate[] revCoords = (Coordinate[]) line.getCoordinates().clone();
		CoordinateArrays.reverse(revCoords);
		LineString revLine = line.getFactory().createLineString(revCoords);
		return revLine;
	}
	public static Coordinate last(LineString line) {
		return line.getCoordinateN(line.getNumPoints() - 1);
	}
	public static Coordinate first(LineString line) {
		return line.getCoordinateN(0);
	}
}