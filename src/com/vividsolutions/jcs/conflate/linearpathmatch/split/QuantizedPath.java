package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jcs.algorithm.linearreference.LocatePoint;

/**
 * Discretizes a {@link FlatPath} quantized by a Minimum Segment Length.
 * Allows referring to locations along the path by discrete {@link QuantumIndex}es.
 * Quantizing a path during splitting allows maintaining the invariant:
 * <ul>
 * <li>Introduced nodes must be separated from path vertices by at least the
 * Minimum Segment Length
 * </ul>
 */
public class QuantizedPath
{
  private double minimumSegmentLength;
  private FlatPath flatPath;
  private int[] segQuantaCounts;

  public QuantizedPath(LinearPath path, double minimumSegmentLength)
  {
    this.minimumSegmentLength = minimumSegmentLength;
    build(path);
  }

  public double getMinimumSegmentLength() { return minimumSegmentLength; }

  public FlatPath getFlatPath()  { return flatPath;  }

  public QuantumIndex getVertexIndex(int segIndex)
  {
    return new QuantumIndex(segIndex, 0, this);
  }

  public int segQuantaCount(int segIndex) { return segQuantaCounts[segIndex]; }

  private void build(LinearPath path)
  {
    flatPath = new FlatPath(path);

    segQuantaCounts = new int[flatPath.getNumPoints()];
    for (int i = 0; i < segQuantaCounts.length - 1; i++) {
      Coordinate p0 = flatPath.getCoordinate(i);
      Coordinate p1 = flatPath.getCoordinate(i + 1);
      segQuantaCounts[i] = quantaCount(p0, p1);
    }
    segQuantaCounts[segQuantaCounts.length - 1] = 0;
  }

  public QuantumIndex findClosestQuantum(Coordinate pt)
  {
    return findClosestQuantum(pt, null);
  }

  public QuantumIndex findClosestQuantum(Coordinate pt, Coordinate[] closestPtReturn)
  {
    return findClosestQuantum(pt, null, closestPtReturn);
  }

  /**
   * Finds the {@link QuantumIndex} for the point closest to the given
   * {@link Coordinate} <i>after</i> the given minimum quantum location, if any
   *
   * @param pt
   * @param closestPtReturn
   * @param minQuantum
   * @return
   */
  public QuantumIndex findClosestQuantum(Coordinate pt,
      QuantumIndex minQuantum,
      Coordinate[] closestPtReturn)
  {
    int minsegIndex = 0;
    if (minQuantum != null)
      minsegIndex = minQuantum.getSegmentIndex();
    int[] segIndexOut = new int[1];
    Coordinate closestPt = flatPath.findClosestPoint(pt, minsegIndex, segIndexOut);
    if (closestPtReturn != null)
      closestPtReturn[0] = closestPt;
    return getNormalizedQuantum(segIndexOut[0], closestPt);
  }

  private QuantumIndex getNormalizedQuantum(int segIndex, Coordinate closestPt)
  {
    int qIndex = 0;
    // compute quantum offset within segment
    if (segIndex < flatPath.getNumPoints() - 1) {
      Coordinate p0 = flatPath.getCoordinate(segIndex);
      Coordinate p1 = flatPath.getCoordinate(segIndex + 1);
      qIndex = quantumOffset(p0, p1, segQuantaCounts[segIndex], closestPt, true);
      // adjust segment index & quantum offset if point is at end of segment
      if (qIndex == segQuantaCounts[segIndex]) {
        qIndex = 0;
        segIndex++;
      }
    }
    return new QuantumIndex(segIndex, qIndex, this);
  }

  private int quantaCount(Coordinate p0, Coordinate p1)
  {
    double segLen = p1.distance(p0);
    int numQuanta = (int) (segLen / minimumSegmentLength);
    return numQuanta;
  }

  /**
   * Computes the coordinate corresponding to a given quantum index
   *
   * @param qi the quantum index of the desired coordinate
   * @return the coordinate of that quantum
   */
  public Coordinate getCoordinate(QuantumIndex qi)
  {
    return quantumCoordinate(qi.getSegmentIndex(), qi.getQuantumIndex());
  }

  private Coordinate quantumCoordinate(int segmentIndex, int quantumIndex)
  {
    int quantaCount = segQuantaCounts[segmentIndex];
    if (segmentIndex == flatPath.getNumPoints() - 1)
      return flatPath.getCoordinate(flatPath.getNumPoints() - 1);
    Coordinate p0 = flatPath.getCoordinate(segmentIndex);
    Coordinate p1 = flatPath.getCoordinate(segmentIndex + 1);
    return quantumCoordinate(p0, p1, segQuantaCounts[segmentIndex], quantumIndex);
  }

  /**
   * Following methods assume that the quantum size is constant over the entire segment
   * (and of course larger than the min seg len).
   * Note that each segment will have a different quantum size.
   * The alternative is to use the min seg len as the preferred quantum size.
   * In this case one or two quanta per segment may need to be larger.
   */

  private Coordinate quantumCoordinate(Coordinate p0, Coordinate p1, int quantaCount, int quantumOffset)
  {
    // optimize simple case
    if (quantumOffset == 0)
      return p0;
    // compute the point position using a variable quantum size
    // (alternative is to use fixed quantum size = min seg len)
    double quantumFrac = quantumOffset / (double) quantaCount;
    return LocatePoint.pointAlongSegmentByFraction(p0, p1, quantumFrac);
  }

  /**
   * Computes the quantum offset
   * within a segment to a given point.
   * The quantum may be chosen in two ways:
   * <ul>
   * <li>the nearest, if rounding is used
   * <li>the nearest before, if rounding is not used
   * </ul>
   * The offset may be equal to the quantaCount, if the
   * point is near the end of the segment.
   *
   * @param p0
   * @param p1
   * @param quantaCount
   * @param pt
   * @return
   */
  private int quantumOffset(Coordinate p0, Coordinate p1, int quantaCount, Coordinate pt,
                            boolean doRounding)
  {
    double lenToPt = p0.distance(pt);
    double segLen = p1.distance(p0);
    double quantumLen = segLen / quantaCount;
    double indexFrac = lenToPt / quantumLen;
    if (doRounding)
      indexFrac += 0.5;
    int index = (int) indexFrac;
    /**
     * the index of a quantum in a segment must always be
     * <= the quantaCount for that segment.
     */
    Assert.isTrue(index <= quantaCount, "found invalid quantum offset");
    return index;
  }
}