package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A location on a {@link SplitPath} where a split will take place.
 *
 * @version 1.0
 */
public class SplitNode {

  private int sourceEdgeIndex;
  private Coordinate splittingPoint;
  private QuantumIndex placedLocation = null;
  private QuantumIndex closestLocation;
  private Coordinate closestExactLocation = null;
  private Coordinate splitCoordinate = null;

/* NOT USED
  public SplitNode(int index, Coordinate splittingPoint)
  {
    this.sourceEdgeIndex = index;
    this.splittingPoint = splittingPoint;
  }
*/

  public SplitNode(int index, Coordinate splittingPoint, QuantumIndex closestQI, Coordinate closestExactLocation)
  {
    this.sourceEdgeIndex = index;
    this.splittingPoint = splittingPoint;
    this.closestLocation = closestQI;
    this.closestExactLocation = closestExactLocation;
  }

  public Coordinate getSplittingPt() { return splittingPoint; }

  public int getSourceEdgeIndex() { return sourceEdgeIndex; }

  public QuantumIndex getPlace()   {     return placedLocation;   }

  public void setPlace(QuantumIndex placedLocation)  {    this.placedLocation = placedLocation;  }

  public boolean isPlaced() { return placedLocation != null; }

  /**
   * Returns the current split coordinate.
   * This is the one explicitly set by {@link SnapToClosestExactLocation}
   * if any,
   * or else the coordinate computed by a previous placement step.
   *
   * @return the coordinate on the split path that this split node causes
   */
  public Coordinate getSplitCoordinate()
  {
    if (splitCoordinate != null)
      return splitCoordinate;
    return placedLocation.getCoordinate();
  }

  public Coordinate getClosestExactLocation() { return closestExactLocation; }

  public void placeAtClosestExactLocation()
      {
      splitCoordinate = closestExactLocation;
  }

  public QuantumIndex getClosestLocation()   {     return closestLocation;   }

  //public void setClosestLocation(QuantumIndex closestLocation)  {    this.closestLocation = closestLocation;  }

  /**
   * Tests whether this splitnode has been placed at the same location
   * as its closest location.
   *
   * @return <code>true</code> if the placed location is the same as the closest location
   */
  public boolean isPlacedAtClosest()
  {
    return placedLocation.compareTo(closestLocation) == 0;
  }
}