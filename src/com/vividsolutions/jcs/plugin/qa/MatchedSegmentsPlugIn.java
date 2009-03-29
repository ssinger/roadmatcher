

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
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.util.feature.*;
import com.vividsolutions.jcs.conflate.boundarymatch.*;
import com.vividsolutions.jcs.qa.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;

public class MatchedSegmentsPlugIn extends ThreadedBasePlugIn {
  public static final String GAP_SIZE_LAYER_NAME = "Gap Size";

  private final static String LAYER0 = "Layer 1";
  private final static String LAYER1 = "Layer 2";
  private final static String DIST_TOL = "Distance Tolerance";
  private final static String ANGLE_TOL = "Angle Tolerance";
  private final static String ALLOW_SAME_ORIENT = "Test Segments with same orientation";

  private Layer layer0, layer1;
  private MatchedSegmentFinder.Parameters param = new MatchedSegmentFinder.Parameters();

  public MatchedSegmentsPlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(
        this, "QA", "Find Misaligned Segments...", null, new MultiEnableCheck()
      .add(context.getCheckFactory().createWindowWithLayerNamePanelMustBeActiveCheck())
        .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Find Misaligned Segments", true);
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

    monitor.report("Finding Misaligned Segments...");
    MatchedSegmentFinder msf = new MatchedSegmentFinder(
        layer0.getFeatureCollectionWrapper(),
        layer1.getFeatureCollectionWrapper(),
        param,
        monitor);


    FeatureCollection matchedSeg0 = msf.getMatchedSegments(0);
    Layer lyr = context.addLayer(StandardCategoryNames.QA, "Misaligned Seg-" + layer0.getName(),
              matchedSeg0);
    LayerStyleUtil.setLinearStyle(lyr, Color.red, 2, 4);
    lyr.fireAppearanceChanged();

    FeatureCollection matchedSeg1 = msf.getMatchedSegments(1);
    Layer lyr2 = context.addLayer(StandardCategoryNames.QA, "Misaligned Seg-" + layer1.getName(),
              matchedSeg1);
    LayerStyleUtil.setLinearStyle(lyr2, Color.green, 2, 4);
    lyr2.fireAppearanceChanged();

    FeatureCollection sizeInd = msf.getSizeIndicators();
    Layer lyrSize = context.getLayerManager().getLayer(GAP_SIZE_LAYER_NAME);

    if (lyrSize == null) {
      lyrSize = context.getLayerManager().addLayer(StandardCategoryNames.QA,
          GAP_SIZE_LAYER_NAME, sizeInd);
      LayerStyleUtil.setLinearStyle(lyrSize, Color.blue, 2, 4);
    }
    else {
      lyrSize.setFeatureCollection(sizeInd);
    }
    lyrSize.fireAppearanceChanged();
    lyrSize.setDescription("Gap Size Indicators (Distance Tol = " + param.distanceTolerance + ")");

    createOutput(context, matchedSeg0, matchedSeg1, sizeInd);

  }
  private void createOutput(PlugInContext context,
      FeatureCollection matchedSeg1,
      FeatureCollection matchedSeg2,
      FeatureCollection sizeInd)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1, "Misaligned Segments");
    context.getOutputFrame().addField("Layer 1: ", layer0.getName() );
    context.getOutputFrame().addField("Layer 2: ", layer1.getName() );
    context.getOutputFrame().addField("Distance Tolerance: ", "" + param.distanceTolerance);
    context.getOutputFrame().addField("Angle Tolerance: ", "" + param.angleTolerance);
    context.getOutputFrame().addText(" ");

    context.getOutputFrame().addField(
        "# Misaligned Segments in Layer 1: ", "" + matchedSeg1.size());
    context.getOutputFrame().addField(
        "# Misaligned Segments in Layer 2: ", "" + matchedSeg2.size());

    double[] minMax = FeatureStatistics.minMaxValue(sizeInd, "LENGTH");
    context.getOutputFrame().addField(
        "Min Gap Size: ", "" + minMax[0]);
    context.getOutputFrame().addField(
        "Max Gap Size: ", "" + minMax[1]);
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("MatchSegments.png")));
    dialog.setSideBarDescription(
        "Finds segments in two datasets which are misaligned."
        + "  Segments are misaligned if they different, are closer than the distance tolerance, have a similar angle, and their mutual projections overlap."
    );
    String fieldName = LAYER0;
    JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getCandidateLayer(0), null, context.getLayerManager());
    String fieldName1 = LAYER1;
    JComboBox addLayerComboBox1 = dialog.addLayerComboBox(fieldName1, context.getCandidateLayer(1), null, context.getLayerManager());
    dialog.addDoubleField(DIST_TOL, param.distanceTolerance, 4, "The Distance Tolerance is the maximum size of the gap between unaligned segments");
    dialog.addDoubleField(ANGLE_TOL, param.angleTolerance, 8, "The Angle Tolerance is the maximum angle between unaligned segments");
    dialog.addCheckBox(ALLOW_SAME_ORIENT, false, "If the datasets can overlap this option should be checked to ensure all misaligned segments are found");
  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer0 = dialog.getLayer(LAYER0);
    layer1 = dialog.getLayer(LAYER1);
    param.distanceTolerance = dialog.getDouble(DIST_TOL);
    param.angleTolerance = dialog.getDouble(ANGLE_TOL);
    boolean allowSameOrientation = dialog.getBoolean(ALLOW_SAME_ORIENT);
    if (allowSameOrientation)
      param.segmentOrientation = SegmentMatcher.EITHER_ORIENTATION;
  }

}
