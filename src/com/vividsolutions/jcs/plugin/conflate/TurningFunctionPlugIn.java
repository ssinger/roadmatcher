

/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jcs.plugin.conflate;

import com.vividsolutions.jump.workbench.ui.TableFrame;
import java.util.Iterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.util.List;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import com.vividsolutions.jump.util.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.geom.*;

public class TurningFunctionPlugIn extends AbstractPlugIn {

  public TurningFunctionPlugIn() {
  }

  public boolean execute(PlugInContext context) throws Exception {
    reportNothingToUndoYet(context);
    Feature feature = (Feature) context.getLayerViewPanel().getSelectionManager().createFeaturesFromSelectedItems().iterator().next();
    Geometry geom = (Geometry) feature.getGeometry().clone();
    geom.normalize();
    List coordinateArrays = CoordinateArrays.toCoordinateArrays(geom, false);
    execute(concatenate(coordinateArrays), feature, context);
    return true;
  }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this,
        new String[] {"Conflate", "Test"}, getName(), false, null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerViewPanelMustBeActiveCheck())
        .add(context.getCheckFactory().createExactlyNItemsMustBeSelectedCheck(1)));

  }

  private Coordinate[] concatenate(List coordinateArrays) {
    ArrayList all = new ArrayList();
    for (Iterator i = coordinateArrays.iterator(); i.hasNext(); ) {
      Coordinate[] array = (Coordinate[]) i.next();
      for (int j = 0; j < array.length; j++) {
        all.add(array[j]);
      }
    }
    return (Coordinate[]) all.toArray(new Coordinate[]{});
  }

  public double turn(Coordinate a, Coordinate b, Coordinate c) {
    double a1 = Angle.angle(a, b);
    double a2 = Angle.angle(b, c);
    double turn = 180 - Angle.toDegrees(Angle.angleBetween(b, a, c));
    if (Angle.getTurn(a1, a2) == Angle.CLOCKWISE) { turn *= -1; }
    return turn;
  }

  private void execute(Coordinate[] coordinates, Feature feature, PlugInContext context) {
    ArrayList turningData = new ArrayList();
    double cumulativeTurningAngle = 0;
    double cumulativeSegmentLength = 0;
    for (int i = 1; i < coordinates.length; i++) {
      Coordinate thirdCoord = i == coordinates.length - 1
                            ? coordinates[1]
                            : coordinates[i+1];
      turningData.add(new Coordinate(cumulativeSegmentLength, cumulativeTurningAngle));
      cumulativeSegmentLength += coordinates[i-1].distance(coordinates[i]);
      turningData.add(new Coordinate(cumulativeSegmentLength, cumulativeTurningAngle));
      cumulativeTurningAngle += turn(coordinates[i-1],
          coordinates[i], thirdCoord);
    }
   turningData.add(new Coordinate(cumulativeSegmentLength, cumulativeTurningAngle));
    display(turningData, feature, context);
  }

  private void display(List turningData, Feature feature, PlugInContext context) {
    TableFrame tableFrame = new TableFrame();
    tableFrame.setTitle("Turning Function (Feature #" + feature.getID() + ")");
    tableFrame.getModel().addColumn("ARC_LENGTH");
    tableFrame.getModel().addColumn("CUMULATIVE_TURNING_ANGLE");
    for (Iterator i = turningData.iterator(); i.hasNext(); ) {
      Coordinate datum = (Coordinate) i.next();
      tableFrame.getModel().addRow(new Object[] { new Double(datum.x), new Double(datum.y) });
    }
    context.getWorkbenchFrame().addInternalFrame(tableFrame);
  }
}
