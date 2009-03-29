

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

package com.vividsolutions.jcs.plugin.clean;

import java.awt.Color;

import javax.swing.JComboBox;

import com.vividsolutions.jcs.simplify.*;
import com.vividsolutions.jump.util.feature.FeatureStatistics;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.ui.*;

public class ShortSegmentRemoverPlugIn
    extends ThreadedBasePlugIn
{

  private final static String LAYER = "Layer";
  private final static String MIN_LENGTH = "Minimum Length";
  private final static String DISPLACEMENT_TOL = "Displacement Tolerance";

  private Layer layer;
  private double minLength = 1.0;
  private double displacementTolerance = .01;

  public ShortSegmentRemoverPlugIn() { }

  /**
   * Returns a very brief description of this task.
   * @return the name of this task
   */
  public String getName() { return "Short Segment Remover"; }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addLayerNameViewMenuItem(
          this, "Clean", "Remove Short Segments...");
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), "Remove Short Segments", true);
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

    monitor.report("Removing Short Segments...");
    FeatureShortSegmentRemover remover = new FeatureShortSegmentRemover(layer.getFeatureCollectionWrapper(), minLength, displacementTolerance);
    FeatureCollection newFC = remover.process(monitor);

    if (monitor.isCancelRequested()) return;
    createLayers(context, newFC);
    createOutput(context, newFC.size(), remover.getSegmentsRemovedCount());
  }


  private void createLayers(PlugInContext context, FeatureCollection newFC)
         throws Exception
  {
    context.addLayer(
        StandardCategoryNames.RESULT_SUBJECT,
        layer.getName(),
        newFC);


  }

  private void createOutput(PlugInContext context, int newFCSize, int segmentsRemovedCount)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1,
        "Short Segment Removal");
    context.getOutputFrame().addField("Layer: ", layer.getName() );
    context.getOutputFrame().addField("Minimum Length: ", "" + minLength);
    context.getOutputFrame().addField("Displacement Tolerance: ", "" + displacementTolerance);
    context.getOutputFrame().addText(" ");

    context.getOutputFrame().addField("# Features Adjusted: ", "" + newFCSize);
    context.getOutputFrame().addField("# Segments Removed: ", "" + segmentsRemovedCount);
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarDescription("Removes all segments from geometries "
        + "which are shorter than a minimum length "
        + "and whose removal does not displace neighbouring segments too much. "
        + "[Currently only removes isolated segments.]"
    );
    String fieldName = LAYER;
    JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getCandidateLayer(0), null, context.getLayerManager());
    dialog.addDoubleField(MIN_LENGTH, minLength, 8, "The Minimum Length determines the size of segments to remove");
    dialog.addDoubleField(DISPLACEMENT_TOL, displacementTolerance, 8, "The Displacement Tolerance controls how much merged segments can be displaced by");
  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer = dialog.getLayer(LAYER);
    minLength = dialog.getDouble(MIN_LENGTH);
    displacementTolerance = dialog.getDouble(DISPLACEMENT_TOL);
  }
}
