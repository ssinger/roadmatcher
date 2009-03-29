/*
 *  The JCS Conflation Suite (JCS) is a library of Java classes that
 *  can be used to build automated or semi-automated conflation solutions.
 *
 *  Copyright (C) 2002 Vivid Solutions
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  For more information, contact:
 *
 *  Vivid Solutions
 *  Suite #1A
 *  2328 Government Street
 *  Victoria BC  V8T 5G5
 *  Canada
 *
 *  (250)385-6040
 *  jcs.vividsolutions.com
 */

package com.vividsolutions.jcs.plugin.qa;

import java.awt.Color;
import javax.swing.*;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.ColorUtil;
import com.vividsolutions.jump.util.feature.*;
import com.vividsolutions.jcs.qa.offsetcorner.OffsetBoundaryCornerFinder;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;

public class OffsetBoundaryCornerFinderPlugIn
    extends ThreadedBasePlugIn
{
  private final static String LAYER1 = "Layer 1";
  private final static String LAYER2 = "Layer 2";
  private final static String DIST_TOL = "Boundary Distance Tolerance";
  private final static String OFFSET_TOL = "Corner Offset Tolerance";
  private final static String MAX_CORNER_ANGLE = "Maximum Corner Angle";


  private Layer layer1, layer2;
  private OffsetBoundaryCornerFinder.Parameters param = new OffsetBoundaryCornerFinder.Parameters();

  public String getName() { return "Offset Boundary Corner Finder"; }

  public OffsetBoundaryCornerFinderPlugIn() { }

  public void initialize(PlugInContext context)
      throws Exception
  {
    context.getFeatureInstaller().addMainMenuItem(
          this, "QA", "Find Offset Boundary Corners...", null, new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context)
      throws Exception
  {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), getName(), true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (!dialog.wasOKPressed()) { return false; }
    getDialogValues(dialog);
    return true;
  }

  public void run(TaskMonitor monitor, PlugInContext context)
       throws Exception
  {
    monitor.allowCancellationRequests();
    OffsetBoundaryCornerFinder finder = new OffsetBoundaryCornerFinder(
        layer1.getFeatureCollectionWrapper(),
        layer2.getFeatureCollectionWrapper(),
        param
        );
    finder.compute(monitor);
    FeatureCollection indicators = finder.getOffsetIndicators();

    if (monitor.isCancelRequested()) return;
    if (indicators.size() > 0) {
      createLayers(context, indicators);
    }
    createOutput(context, indicators);
  }

  private void createLayers(PlugInContext context,
                            FeatureCollection indicators)
  {
      String offsetLayerName = "Offset Indicators";
      Layer lyr3 = context.addLayer(StandardCategoryNames.QA,
                                offsetLayerName, indicators);
      LayerStyleUtil.setLinearStyle(lyr3, Color.red, 2, 6);
      lyr3.setDescription(offsetLayerName);
      lyr3.fireAppearanceChanged();
  }

  private void createOutput(PlugInContext context,
                            FeatureCollection indicators)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1, getName());
    context.getOutputFrame().addField(LAYER1 + ": ", layer1.getName() );
    context.getOutputFrame().addField(LAYER2 + ": ", layer2.getName() );
    context.getOutputFrame().addField(DIST_TOL + ": ", param.boundaryDistanceTolerance + "" );
    context.getOutputFrame().addField(OFFSET_TOL + ": ", param.offsetTolerance + "" );
    context.getOutputFrame().addField(MAX_CORNER_ANGLE + ": ", param.maxCornerAngle + "" );
    context.getOutputFrame().addText(" ");

    context.getOutputFrame().addField(
        "# Offset Corners: ", "" + indicators.size());

    double[] minMax = FeatureStatistics.minMaxValue(indicators, "LENGTH");
    context.getOutputFrame().addField(
        "Min Offset Size: ", "" + minMax[0]);
    context.getOutputFrame().addField(
        "Max Offset Size: ", "" + minMax[1]);
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
  {
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("OffsetBoundaryCorner.png")));
    dialog.setSideBarDescription("Finds corners on the boundary between two datasets which are offset from each other."    );
    dialog.addLayerComboBox(LAYER1, context.getLayerManager().getLayer(0), context.getLayerManager());
    dialog.addLayerComboBox(LAYER2, context.getLayerManager().getLayer(1), context.getLayerManager());
    dialog.addDoubleField(DIST_TOL, param.boundaryDistanceTolerance, 8, "Determines which points are on the boundary of the datasets");
    dialog.addDoubleField(OFFSET_TOL, param.offsetTolerance, 8, "The maximum distance between corners to be reported as an offset");
    dialog.addDoubleField(MAX_CORNER_ANGLE, param.maxCornerAngle, 8, "The maximum angle between adjacent segments which will labelled as a corner (in degrees) ");
  }

  private void getDialogValues(MultiInputDialog dialog)
  {
    layer1 = dialog.getLayer(LAYER1);
    layer2 = dialog.getLayer(LAYER2);
    param.boundaryDistanceTolerance = dialog.getDouble(DIST_TOL);
    param.offsetTolerance = dialog.getDouble(OFFSET_TOL);
    param.maxCornerAngle = dialog.getDouble(MAX_CORNER_ANGLE);
  }

}
