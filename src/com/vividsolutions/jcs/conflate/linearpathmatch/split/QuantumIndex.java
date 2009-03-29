package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents a location along a LinearPath discretized
 * by a minimum segment length.
 * Note that an index only has meaning in the context of
 * its parent QuantizedPath.
 * A QuantumIndex supports a sentinel value, which refers to the last
 * vertex (node) in a path.  This value is indicated by a segmentIndex
 * one greater than the last valid segment index, and a quantumIndex of 0.
 */
public class QuantumIndex
    implements Comparable
{

  private int segmentIndex;
  private int quantumIndex;
  private QuantizedPath qPath;  // the path this location is on
  private Coordinate pt;

  public QuantumIndex(int segmentIndex,
                      int quantumIndex,
                      QuantizedPath qPath)
  {
    this.segmentIndex = segmentIndex;
    this.quantumIndex = quantumIndex;
    this.qPath = qPath;
  }

  public int getSegmentIndex() { return segmentIndex; }
  public int getQuantumIndex() { return quantumIndex; }
  public QuantizedPath getPath() { return qPath; }

  public boolean isVertex()
  {
    return quantumIndex == 0;
  }
  public boolean isFirst()
  {
    return segmentIndex == 0 && quantumIndex == 0;
  }

  public boolean isLast()
  {
    return segmentIndex >= qPath.getFlatPath().getNumPoints() - 1;
  }

  /**
   * Computes the index of the next quantum in order along this path.
   *
   * @param qi the starting quantum index
   * @return the index of the next quantum
   *
   * @exception if this quantum is the last in the path
   */
  public QuantumIndex next()
  {
    int segIndex = getSegmentIndex();
    int qIndex = getQuantumIndex();

    if (isLast())
      throw new IllegalStateException();

    if (qIndex < maxQuantumIndex(segIndex))
      return new QuantumIndex(segIndex, qIndex + 1, qPath);

    // no more quanta in this segment - have to increment segment index
    int nextSegIndex = segIndex + 1;
    return new QuantumIndex(nextSegIndex, 0, qPath);
  }

  /**
   * Computes the index of the next quantum in order along this path.
   *
   * @param qi the starting quantum index
   * @return the index of the previous quantum
   *
   * @exception if this quantum is the first in the path
   */
  public QuantumIndex prev()
  {
    int segIndex = getSegmentIndex();
    int qIndex = getQuantumIndex();

    if (isFirst())
      throw new IllegalStateException();

    if (qIndex > 0)
      return new QuantumIndex(segIndex, qIndex - 1, qPath);

    // no more quanta in this segment - have to decrement segment index
    int prevSegIndex = segIndex - 1;
    return new QuantumIndex(prevSegIndex, maxQuantumIndex(prevSegIndex), qPath);
  }

  public Coordinate getCoordinate()
  {
    if (pt == null)
      pt = qPath.getCoordinate(this);
    return pt;
  }

  /**
   * Tests whether the argument is the next quantum in sequence after this one.
   *
   * @param qi
   * @return <code>true</code> if <code>qi</code> is the next quantum in sequence
   */
  public boolean isNext(QuantumIndex qi)
  {
    if (qi.segmentIndex - segmentIndex == 0) {
      if (qi.quantumIndex - quantumIndex == 1)
        return true;
      return false;
    }
    if (qi.segmentIndex - segmentIndex == 1) {
      if (qi.quantumIndex == 0
          && quantumIndex == maxQuantumIndex(segmentIndex))
        return true;
    }
    return false;
  }

  private int maxQuantumIndex(int segmentIndex)
  {
    return qPath.segQuantaCount(segmentIndex) - 1;
  }
  /**
   *  Compares this QuantumIndex with the specified QuantumIndex for order.
   *
   *@param  o  the <code>QuantumIndex</code>
   *      with which this <code>QuantumIndex</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer
   *      as this <code>QuantumIndex</code>
   *      is less than, equal to, or greater than '
   * the specified <code>QuantumIndex</code>
   */
  public int compareTo(Object o)
  {
    QuantumIndex other = (QuantumIndex) o;
    if (segmentIndex < other.segmentIndex) return -1;
    if (segmentIndex > other.segmentIndex) return 1;
    if (quantumIndex < other.quantumIndex) return -1;
    if (quantumIndex > other.quantumIndex) return 1;
    return 0;
  }

  public String toString()
  {
    return segmentIndex + "-" + quantumIndex;
  }
}