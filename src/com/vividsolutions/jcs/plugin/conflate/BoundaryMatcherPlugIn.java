

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

import java.awt.Color;
import java.util.Iterator;

import com.vividsolutions.jcs.conflate.boundarymatch.*;
import com.vividsolutions.jump.feature.*;

import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;


import com.vividsolutions.jump.task.*;
import javax.swing.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;
import javax.swing.ImageIcon;

public class BoundaryMatcherPlugIn extends ThreadedBasePlugIn {

  private static void computeAdjustmentStats(FeatureCollection adjVertexInd, double[] minMax)
  {
    minMax[0] = 0.0;
    minMax[1] = 0.0;
    int adjDistanceIndex = -1;
    int j = 0;
    for (Iterator i = adjVertexInd.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      if (adjDistanceIndex == -1) {
        adjDistanceIndex = f.getSchema().getAttributeIndex(BoundaryMatcher.ATTR_ADJ_DISTANCE);
      }
      double adjDistance = f.getDouble(adjDistanceIndex);
      if (j == 0 || adjDistance < minMax[0]) minMax[0] = adjDistance;
      if (j == 0 || adjDistance > minMax[1]) minMax[1] = adjDistance;
      j++;
    }
  }

  private final static String REF_LAYER = "Reference Layer";
  private final static String SUB_LAYER = "Subject Layer";
  private final static String REF_INSERT_VERT = "Insert Vertices in Reference";
  // hide this until it is implemented
  //private final static String SUB_DELETE_VERT = "SUB_DELETE_VERT";
  private final static String DIST_TOL = "Distance Tolerance";
  //private final static String ANG_TOL = "Angle Tolerance";

  private BoundaryMatcherParameters bmParam = new BoundaryMatcherParameters();
  private Layer refLyr, subLyr;
  private String outputLayerName = "output";
  private BoundaryMatcher bm;

  public BoundaryMatcherPlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this, new String[] {"Conflate", "Test"},
        "Boundary Match...", false, null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerViewPanelMustBeActiveCheck())
        .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Boundary Match", true);
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

    bm = new BoundaryMatcher(
        refLyr.getFeatureCollectionWrapper(),
        subLyr.getFeatureCollectionWrapper());

    monitor.report("Conflating Boundaries...");
    bm.match(bmParam);
    createOutput(context);
  }

  private void createOutput(PlugInContext context)
         throws Exception {

    FeatureCollection adj0 = bm.getAdjustedFeatures(0);
    FeatureCollection adj1 = bm.getAdjustedFeatures(1);

    addLayer(StandardCategoryNames.RESULT_REFERENCE, refLyr.getName(),
        bm.getUpdatedFeatures(0), true, context);
    addLayer(StandardCategoryNames.RESULT_SUBJECT, subLyr.getName(),
        bm.getUpdatedFeatures(1), true, context);
/* OLD CODE
    addLayer(StandardCategoryNames.RESULT_REFERENCE, refLyr.getName(),
        bm.getUpdatedFeatures(0, refLyr.getFeatureCollection()), true);
    addLayer(StandardCategoryNames.RESULT_SUBJECT, subLyr.getName(),
        bm.getUpdatedFeatures(1, subLyr.getFeatureCollection()), true);
*/

    addLayer(StandardCategoryNames.QA, "overlap-" + refLyr.getName(), bm.getOverlapping(0), false, context);
    addLayer(StandardCategoryNames.QA, "overlap-" + subLyr.getName(), bm.getOverlapping(1), false, context);

    //<<TODO:NAMING>> Might look better to use names like "Adj Feature - " [Jon Aquino]
    addLayer(StandardCategoryNames.QA, "adjFeature-" + refLyr.getName(), adj0, false, context);
    addLayer(StandardCategoryNames.QA, "adjFeature-" + subLyr.getName(), adj1, false, context);

    addLayer(StandardCategoryNames.QA, "adjEdge-" + refLyr.getName(), bm.getAdjustedEdgeIndicators(0), false, context);
    addLayer(StandardCategoryNames.QA, "adjEdge-" + subLyr.getName(), bm.getAdjustedEdgeIndicators(1), false, context);

    FeatureCollection adjVertexInd = bm.getAdjustedVertexIndicators();
    Layer lyr = addLayer(StandardCategoryNames.QA, "adjSubjectVertex", adjVertexInd, false, context);
    lyr.getBasicStyle().setFillColor(Color.red);
    lyr.getBasicStyle().setLineColor(Color.red);
    lyr.getBasicStyle().setAlpha(255);
    lyr.getVertexStyle().setEnabled(true);
    lyr.fireAppearanceChanged();

    double[] minMax = new double[2];
    computeAdjustmentStats(adjVertexInd, minMax);

    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1,
        "Boundary Matching Conflation");
    context.getOutputFrame().addText(
        "Reference Layer: " + refLyr.getName() );
    context.getOutputFrame().addText(
        "Subject Layer: " + subLyr.getName() );
    context.getOutputFrame().addText(" ");

    context.getOutputFrame().addField(
        "# Reference Adjusted Features: ", "" + adj0.size());
    context.getOutputFrame().addField(
        "# Subject Adjusted Features: ", "" + adj1.size());
    context.getOutputFrame().addField(
        "# Adjusted Vertices: ", "" + adjVertexInd.size());


    context.getOutputFrame().addText(" ");
    context.getOutputFrame().addField(
        "Distance Tolerance: ", "" + bmParam.distanceTolerance);
    context.getOutputFrame().addField(
        "Min Adjustment Distance: ", "" + minMax[0]);
    context.getOutputFrame().addField(
        "Max Adjustment Distance: ", "" + minMax[1]);
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setTitle("Boundary Match");
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("BoundaryMatch.gif")));
    dialog.setSideBarDescription("Conflates polygons at the boundaries of two coverages."
    + "  It ensures that there are no overlaps and no gaps of smaller than the distance tolerance value."
    );
    //Set initial layer values to the first and second layers in the layer list.
    //In #initialize we've already checked that the number of layers >= 2. [Jon Aquino]
    dialog.addLayerComboBox(REF_LAYER, context.getLayerManager().getLayer(0), "The Reference layer is not changed (although new vertices may be introduced)",
                         context.getLayerManager());
    dialog.addLayerComboBox(SUB_LAYER, context.getLayerManager().getLayer(1), "The Subject layer is snapped to the Reference layer",context.getLayerManager());
    dialog.addDoubleField(DIST_TOL, bmParam.distanceTolerance, 4, "The Distance Tolerance determines how large gaps and overlaps can be");
    //dialog.addDoubleField(ANG_TOL, ANG_TOL, bmParam.angleTolerance, 4, "The Angle Tolerance (in degrees) controls how parallel matched segments must be");
    dialog.addCheckBox(REF_INSERT_VERT, true, "Insert new vertices in the Reference if they are present in the Subject. "
       + "  Choosing to NOT insert new Reference vertices leaves the dataset unchanged, "
       + "but may result in the boundaries not matching exactly."
                       );
  }

  private void getDialogValues(MultiInputDialog dialog) {
    refLyr = dialog.getLayer(REF_LAYER);
    subLyr = dialog.getLayer(SUB_LAYER);
    bmParam.distanceTolerance = dialog.getDouble(DIST_TOL);
    //bmParam.angleTolerance = dialog.getDouble(ANG_TOL);
    bmParam.insertRefVertices = dialog.getBoolean(REF_INSERT_VERT);
    //bmParam.deleteSubVertices = dialog.getBoolean(SUB_DELETE_VERT);
  }
//
  private Layer addLayer(String categoryName, String layerName, FeatureCollection fc, boolean isVisible, PlugInContext context)
  {
    Layer lyr = context.addLayer(categoryName, layerName, fc);
    lyr.setVisible(isVisible);
    return lyr;
  }
}
