

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

package com.vividsolutions.jcs.plugin.tools;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

import java.util.*;
import com.vividsolutions.jcs.qa.FeatureSegmentCounter;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.ui.*;

public class UniqueSegmentsPlugIn
    extends ThreadedBasePlugIn
{

  private static List toLineStrings(List segmentList)
  {
    GeometryFactory fact = new GeometryFactory();
    List lineStringList = new ArrayList();
    for (int i = 0; i < segmentList.size(); i++) {
      LineSegment seg = (LineSegment) segmentList.get(i);
      LineString ls = LineSegmentUtil.asGeometry(fact, seg);
      lineStringList.add(ls);
    }
    return lineStringList;
  }

  private final static String LAYER = "Layer";

  private MultiInputDialog dialog;
  private String layerName;
  private int inputEdgeCount = 0;
  private int uniqueSegmentCount = 0;

  public UniqueSegmentsPlugIn() { }

  /**
   * Returns a very brief description of this task.
   * @return the name of this task
   */
  public String getName() { return "Unique Segments"; }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this,
        new String[] {"Tools", "Analysis"}, "Unique Segments...", false, null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerViewPanelMustBeActiveCheck())
        .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(1))
        );
  }

  public boolean execute(PlugInContext context) throws Exception {
    dialog = new MultiInputDialog(
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

    monitor.report("Extracting Unique Segments...");

    Layer layer = dialog.getLayer(LAYER);
    FeatureCollection lineFC = layer.getFeatureCollectionWrapper();
    inputEdgeCount = lineFC.size();

    FeatureSegmentCounter fsc = new FeatureSegmentCounter(false, monitor);
    fsc.add(lineFC);
    List uniqueFSList = fsc.getUniqueSegments();
    uniqueSegmentCount = uniqueFSList.size();
    List linestringList = toLineStrings(uniqueFSList);

    if (monitor.isCancelRequested()) return;
    createLayers(context, linestringList);
  }

  private void createLayers(PlugInContext context, List linestringList)
         throws Exception
  {

    FeatureCollection lineStringFC = FeatureDatasetFactory.createFromGeometry(linestringList);
    context.addLayer(
        StandardCategoryNames.RESULT_SUBJECT,
        layerName + " Unique Segs",
        lineStringFC);

    createOutput(context);

  }

  private void createOutput(PlugInContext context)
  {
    context.getOutputFrame().createNewDocument();
    context.getOutputFrame().addHeader(1,
        "Unique Segment Extraction");
    context.getOutputFrame().addField("Layer: ", layerName);


    context.getOutputFrame().addText(" ");
    context.getOutputFrame().addField(
                                      "# Unique Segments Extracted: ", "" + uniqueSegmentCount);
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("UniqueSegments.png")));
    dialog.setSideBarDescription("Extracts all unique line segments from a dataset. "
    );
    String fieldName = LAYER;
    JComboBox addLayerComboBox = dialog.addLayerComboBox(fieldName, context.getCandidateLayer(0), null, context.getLayerManager());
  }

  private void getDialogValues(MultiInputDialog dialog) {
    Layer layer = dialog.getLayer(LAYER);
    layerName = layer.getName();
  }
}
