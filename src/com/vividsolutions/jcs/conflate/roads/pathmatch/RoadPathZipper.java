package com.vividsolutions.jcs.conflate.roads.pathmatch;

import java.util.*;
import com.vividsolutions.jcs.conflate.roads.model.*;
import com.vividsolutions.jcs.conflate.roads.nodematch.*;
import com.vividsolutions.jcs.debug.DebugFeature;
import com.vividsolutions.jcs.graph.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.match.*;
import com.vividsolutions.jcs.conflate.linearpathmatch.split.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * Zippers together a predetermined match of path through two road networks.
 *
 * @version 1.0
 */
public class RoadPathZipper {

  private double distanceTolerance = 0.0;
  private double segmentLengthTolerance = 0.0;

  private PathMatch pathMatch;
  private PathZipper zipper = null;
  private boolean isValid = false;
  private RoadSplitPath[] roadSplitPath = new RoadSplitPath[2];

  public RoadPathZipper(PathMatch pathMatch) {
    this.pathMatch = pathMatch;
  }

  public void setDistanceTolerance(double distanceTolerance)
  {
    this.distanceTolerance = distanceTolerance;
  }

  public void setSegmentLengthTolerance(double roadSegmentLengthTolerance)
  {
    this.segmentLengthTolerance = roadSegmentLengthTolerance;
  }

  public RoadSplitPath[] getSplitPaths() { return roadSplitPath; }

  public double getDistanceTolerance() {    return distanceTolerance;  }

  public Geometry getGeometry() { return zipper.getGeometry(); }

  public boolean isValid() { return isValid; }

  public void zipper(boolean useSubmatches)
  {
    zipper = null;
    try {
      zipper = new PathZipper(pathMatch);
      zipper.setUseSubmatches(useSubmatches);
      zipper.setDistanceTolerance(distanceTolerance);
      zipper.setSegmentLengthTolerance(segmentLengthTolerance);
      if (! zipper.isValid()) {
        return;
      }

      // at this point the split has been determined to be valid
      SplitPath[] splitPaths = zipper.getSplitPaths();
      roadSplitPath[0] = new RoadSplitPath(splitPaths[0]);
      roadSplitPath[1] = new RoadSplitPath(splitPaths[1]);

      roadSplitPath[0].updateSource();
      roadSplitPath[1].updateSource();
    }
    // this code is for debugging only
    catch (AssertionFailedException ex) {
      ex.printStackTrace();
      //System.out.println("EXCEPTION: " + ex.getMessage());
      if (zipper != null) System.out.println(zipper.getGeometry());
      throw ex;
    }
    roadSplitPath[0].matchSplits(roadSplitPath[1]);
    isValid = true;
  }

}