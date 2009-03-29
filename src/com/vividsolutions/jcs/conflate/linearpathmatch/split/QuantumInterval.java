package com.vividsolutions.jcs.conflate.linearpathmatch.split;

/**
 * Represents an interval between two {@link QuantumIndex}es.
 * along a {@link QuantizedPath}.
 * One or both ends may be <code>null</code>, which indicates that the
 * interval extends to the end of the path in that direction.
 *
 * @version 1.0
 */

public class QuantumInterval {

  private QuantumIndex q0;
  private QuantumIndex q1;

  public QuantumInterval()
  {
    this(null, null);
  }

  public QuantumInterval(QuantumIndex q0, QuantumIndex q1)
  {
    this.q0 = q0;
    this.q1 = q1;
  }

  public QuantumIndex getBound(int boundIndex)
  {
    if (boundIndex == 0) return q0;
    if (boundIndex == 1) return q1;
    throw new IllegalArgumentException("invalid bound index");
  }

  public void setBound(int boundIndex, QuantumIndex q)
  {
    if (boundIndex == 0) {
      q0 = q;
      return;
    }
    if (boundIndex == 1) {
      q1 = q;
      return;
    }
    throw new IllegalArgumentException("invalid bound index");
  }

  public boolean isProperlyContained(int segmentIndex)
  {
    if (q0 != null) {
      if (segmentIndex <= q0.getSegmentIndex())
        return false;
    }
    if (q1 != null) {
      if (segmentIndex > q1.getSegmentIndex())
        return false;
      if (segmentIndex == q1.getSegmentIndex() && q1.getQuantumIndex() == 0)
        return false;
    }
    return true;
  }

  public boolean isProperlyContained(QuantumIndex qi)
  {
    if (q0 != null) {
      if (qi.compareTo(q0) <= 0)
        return false;
    }
    if (q1 != null) {
      if (qi.compareTo(q1) >= 0)
        return false;
    }
    return true;
  }
}