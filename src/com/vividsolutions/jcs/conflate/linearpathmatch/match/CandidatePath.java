package com.vividsolutions.jcs.conflate.linearpathmatch.match;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jcs.algorithm.VertexHausdorffDistance;

/**
 * Models a candidate path (and sequence of linestrings)
 * for use during the process of matching two paths.
 *
 * @version 1.0
 */
public class CandidatePath
{
  private double distanceTolerance = 10.0;

  private PathIterator pathIt;
  private CoordinateList coordList = new CoordinateList();
  private LineString candidateLine = null;
  private Geometry candidateBuffer;
  private LinearEdge candidateEdge;
  private LinearPath path = new LinearPath();

  public CandidatePath(PathTracer pathTracer)
  {
    this.pathIt = new PathIterator(pathTracer);
    extend();
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  public LinearPath getPath()
  {
    return path;
  }

  public void commit()
  {
    if (candidateEdge != null)
      path.add(candidateEdge);
    candidateEdge = null;
  }

  public boolean extend()
  {
    commit();
    if (! pathIt.hasNext())
      return false;

    candidateEdge = pathIt.next();
    updateCandidateGeometry(candidateEdge.getGeometry());
    return true;
  }

  public LinearEdge lookahead()   {   return pathIt.lookahead();  }

  public LinearEdge getCandidateEdge()  {    return candidateEdge;  }

  public boolean hasSubpathMatching(CandidatePath testPath)
  {
    return hasTrimmedHausdorffMatch(testPath);
    //return hasBufferMatch(path);
  }

  /**
   * Computes whether two paths match using
   * a strategy based on the Hausdorff distance between the
   * candidate path (trimmed to fit the test path) and the test path.
   *
   * @param testPath the path to test for a match
   * @return <code>true</code> if the paths match
   */
  public boolean hasTrimmedHausdorffMatch(CandidatePath testPath)
  {
    LineString thisLine = getGeometry();
    // paths are assumed to be within tolerance at their start
    LineString otherLine = testPath.getGeometry();
    Coordinate otherEndPt = otherLine.getCoordinateN(otherLine.getNumPoints() - 1);
    LineString trimmedLine = LineStringTrimmer.trimEnd(thisLine, otherEndPt, distanceTolerance);
    if (trimmedLine.isEmpty())
      return false;
    double dist = VertexHausdorffDistance.distance(trimmedLine, otherLine);

    return dist <= distanceTolerance;
  }

  /**
   * Use a buffer around the candidate path to test whether
   * the test path matches.
   * <p>
   * <b>WARNING:</b>
   * This strategy has a known problem where the candidate path
   * has a "horseshoe curve" of width less than twice the buffer distance.
   * In this case a "shortcut" across the horseshoe will cause a match,
   * even though it is far from the end of the horseshoe.  Use the
   * Hausdorff based strategy above.
   *
   * @param testPath the path to test for a match
   * @return <code>true</code> if the paths match
   */
  public boolean hasBufferMatch(CandidatePath testPath)
  {
    Geometry buffer = getBuffer();
    // debugging
//    System.out.println("Testing path containment");
//    System.out.println(buffer);
//    System.out.println(path.getGeometry());
    return buffer.contains(testPath.getGeometry());
  }

  public Geometry getBuffer()
  {
    if (candidateBuffer == null) {
//      try {
        candidateBuffer = candidateLine.buffer(distanceTolerance);
//      }
//      catch (Exception ex)    {
//        System.out.println("Buffer error for line: " + candidateLine);
//      }
//    System.out.println(candidateBuffer);
    }
    return candidateBuffer;
  }

  public boolean isEmpty() {
    return candidateEdge == null && ! hasEdges();
  }

  public boolean hasEdges() { return path.size() > 0; }

  public LineString getGeometry() { return candidateLine; }

  private void updateCandidateGeometry(LineString line)
  {
    coordList.add(line.getCoordinates(), false);

    Coordinate [] pts = coordList.toCoordinateArray();
    candidateLine = line.getFactory().createLineString(pts);
    // line has changed, so flush buffer cache
    candidateBuffer = null;
  }
}