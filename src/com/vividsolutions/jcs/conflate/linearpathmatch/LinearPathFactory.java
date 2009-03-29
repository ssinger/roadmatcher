package com.vividsolutions.jcs.conflate.linearpathmatch;

import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.*;
import java.util.Iterator;

/**
 * Provides various methods to create LinearPaths
 * from collections of linestrings of different types.
 * The collections of linestrings are
 * assumed to be contiguous and non-intersecting.
 * A method is provided to re-orient the coordinates of consecutive linestrings as necessary.
 *
 * @version 1.0
 */
public class LinearPathFactory
{
  public static LinearPath toLinearPath(FeatureCollection pathFC)
  {
    LineString[] undirLines = new LineString[pathFC.size()];
    int i = 0;
    for (Iterator it = pathFC.iterator(); it.hasNext(); ) {
      Feature f = (Feature) it.next();
      undirLines[i++] = (LineString) f.getGeometry();
    }
    LineString[] lines = directedLines(undirLines);
    LinearPath path = new LinearPath();
    for (int j = 0; j < lines.length; j++) {
      path.add(new LinearEdge(lines[j], null));
    }
    return path;
  }

  public static LineString[] toLineStringArray(FeatureCollection pathFC)
  {
    LineString[] lines = new LineString[pathFC.size()];
    int i = 0;
    for (Iterator it = pathFC.iterator(); it.hasNext(); ) {
      Feature f = (Feature) it.next();
      lines[i++] = (LineString) f.getGeometry();
    }
    return lines;
  }

  public static LinearPath toLinearPath(LineString[] lines)
  {
    LinearPath path = new LinearPath();
    for (int j = 0; j < lines.length; j++) {
      path.add(new LinearEdge(lines[j], null));
    }
    return path;
  }

  public static LineString[] directedLines(LineString[] undirLines)
  {
    LineString[] dirLines = new LineString[undirLines.length];
    Coordinate lastPt = undirLines[0].getCoordinateN(0);
    for (int i = 0; i < undirLines.length; i++) {
      Coordinate startPt = undirLines[i].getCoordinateN(0);
      LineString directedLine = undirLines[i];
      if (! startPt.equals(lastPt)) {
        Coordinate[] revCoords = (Coordinate[]) directedLine.getCoordinates().clone();
        CoordinateArrays.reverse(revCoords);
        directedLine = directedLine.getFactory()
            .createLineString(revCoords);
      }
      dirLines[i] = directedLine;
      lastPt = directedLine.getCoordinateN(directedLine.getNumPoints() - 1);
    }
    return dirLines;
  }

}