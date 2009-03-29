

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

package com.vividsolutions.jcs.plugin.qa;

import java.awt.Color;
import javax.swing.*;

import com.vividsolutions.jcs.qa.diff.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.task.*;

public class DiffSegmentsPlugIn extends ThreadedBasePlugIn {

  private final static String LAYER1 = "Layer 1";
  private final static String LAYER2 = "Layer 2";
  private final static String USE_TOLERANCE = "Use Distance Tolerance";
  private final static String DISTANCE_TOL = "Distance Tolerance";

  private Layer layer1, layer2;
  private boolean useTolerance = false;
  private double distanceTolerance = 0.0;

  public DiffSegmentsPlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this, new String[] {"QA"},
        getName() + "...", false, null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerViewPanelMustBeActiveCheck())
        .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Diff Segments", true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (! dialog.wasOKPressed()) { return false; }
    getDialogValues(dialog);
    return true;
  }

  public void run(TaskMonitor monitor, PlugInContext context)
       throws Exception
  {
    FeatureCollection[] diffFC;
    if (! useTolerance) {
      DiffSegments diff = new DiffSegments(monitor);
      diff.setSegments(0, layer1.getFeatureCollectionWrapper() );
      diff.setSegments(1, layer2.getFeatureCollectionWrapper() );

      diffFC = new FeatureCollection[2];
      diffFC[0] = diff.computeDiffEdges(0);
      diffFC[1] = diff.computeDiffEdges(1);
    }
    else {
      DiffSegmentsWithTolerance diff = new DiffSegmentsWithTolerance(
          layer1.getFeatureCollectionWrapper(),
          layer2.getFeatureCollectionWrapper(),
          distanceTolerance);

      diffFC  = diff.diff();
    }
    createLayers(context, diffFC);
    createOutput(context, diffFC);
  }

  private void createLayers(PlugInContext context, FeatureCollection[] diffFC)
  {
    Layer lyr = context.addLayer(StandardCategoryNames.QA, "Segment Diffs - " + layer1.getName(),
              diffFC[0] );
    LayerStyleUtil.setLinearStyle(lyr, Color.red, 2, 4);
    lyr.fireAppearanceChanged();

    Layer lyr2 = context.addLayer(StandardCategoryNames.QA, "Segment Diffs - " + layer2.getName(),
              diffFC[1] );
    LayerStyleUtil.setLinearStyle(lyr2, Color.blue, 2, 4);
    lyr2.fireAppearanceChanged();
  }

  private void createOutput(PlugInContext context, FeatureCollection[] diffFC)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1, "Diff Segments");
    context.getOutputFrame().addField("Layer 1: ", layer1.getName() );
    context.getOutputFrame().addField("Layer 2: ", layer2.getName() );
    context.getOutputFrame().addText(" ");
    if (useTolerance) {
      context.getOutputFrame().addField("Distance Tolerance: ",
                                      "" + distanceTolerance );
    }
    context.getOutputFrame().addField(
        "# Unmatched Segments in Layer 1: ", "" + diffFC[0].size());
    context.getOutputFrame().addField(
        "# Unmatched Segments in Layer 2: ", "" + diffFC[1].size());
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("DiffSegments.png")));
    dialog.setSideBarDescription("Finds line segments which occur in Layer 1 or Layer 2 but not both.");
    //Set initial layer values to the first and second layers in the layer list.
    //In #initialize we've already checked that the number of layers >= 2. [Jon Aquino]
    dialog.addLayerComboBox(LAYER1, context.getLayerManager().getLayer(0), context.getLayerManager());
    dialog.addLayerComboBox(LAYER2, context.getLayerManager().getLayer(1), context.getLayerManager());
    dialog.addCheckBox(USE_TOLERANCE, useTolerance,
                       "Match segments if all points are within a Distance Tolerance");
    dialog.addDoubleField(DISTANCE_TOL, distanceTolerance, 8, "The Distance Tolerance specifies how close segments must be to match");
  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer1 = dialog.getLayer(LAYER1);
    layer2 = dialog.getLayer(LAYER2);
    useTolerance = dialog.getBoolean(USE_TOLERANCE);
    distanceTolerance = dialog.getDouble(DISTANCE_TOL);
  }

}
