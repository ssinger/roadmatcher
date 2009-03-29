

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

import java.awt.*;
import java.util.*;


import com.vividsolutions.jcs.conflate.polygonmatch.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;

/**
 * @deprecated
 */
public class PolygonMatchPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

  public PolygonMatchPlugIn() {
  }
  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(this, new String[] {"Conflate"},
        getName()+"...", false, null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerViewPanelMustBeActiveCheck())
        .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
  }

  public boolean execute(PlugInContext context) throws Exception {
    return prompt(context);
  }

  public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
throw new UnsupportedOperationException();
//    FeatureMatcher matcher = featureMatcher(dialog);
//    BasicFCMatchFinder fcMatcher = new BasicFCMatchFinder(matcher);
//    Map matchMap = fcMatcher.match(dialog.getLayer(SUBJECT_LAYER).getFeatureCollection(),
//                    dialog.getLayer(REFERENCE_LAYER).getFeatureCollection(), monitor);
//    generateLayers(matchMap, context);
  }

  public void generateLayers(Map matchMap, PlugInContext context) {
    generateMatchLayer(matchMap, context);
  }

  private static final String SCORE = "SCORE";
  private static final String GEOMETRY = "GEOMETRY";

  private FeatureSchema matchSchema = null;
  public FeatureSchema matchSchema() {
    if (matchSchema == null) {
      matchSchema = new FeatureSchema();
      matchSchema.addAttribute(GEOMETRY, AttributeType.GEOMETRY);
      matchSchema.addAttribute(SCORE, AttributeType.DOUBLE);
    }
    return matchSchema;
  }

  private void generateMatchLayer(Map matchMap, PlugInContext context) {
    FeatureDataset dataset = new FeatureDataset(matchSchema());
    for (Iterator i = matchMap.keySet().iterator(); i.hasNext(); ) {
      Feature target = (Feature) i.next();
      Matches matches = (Matches) matchMap.get(target);
      for (int j = 0; j < matches.size(); j++) {
        Feature match = matches.getFeature(j);
        dataset.add(toMatchFeature(target, match, matches.getScore(j), matchSchema()));
      }
    }
    Layer layer = new Layer("Polygon Matches", MATCH_LAYER_COLOR, dataset, context.getLayerManager());
    setMatchStyles(layer, MATCH_LAYER_COLOR, context);
    context.getLayerManager().addLayer(StandardCategoryNames.WORKING, layer);
  }

  private static final Color MATCH_LAYER_COLOR = Color.red;

  public void setMatchStyles(Layer layer, Color color, PlugInContext context) {
    boolean firingEvents = context.getLayerManager().isFiringEvents();
    context.getLayerManager().setFiringEvents(false);
    try {
      layer.getBasicStyle().setLineWidth(2);
      layer.getBasicStyle().setLineColor(color);
      layer.addStyle(new ArrowLineStringEndpointStyle.NarrowSolidEnd());
      layer.setDrawingLast(true);
    }
    finally {
      context.getLayerManager().setFiringEvents(firingEvents);
    }

  }

  private GeometryFactory factory = new GeometryFactory();
  private InteriorPointFinder interiorPointFinder = new InteriorPointFinder();

  public Feature toMatchFeature(Feature target, Feature match, double score, FeatureSchema schema) {
    LineString lineString = factory.createLineString(new Coordinate[] {
          interiorPointFinder.findPoint(target.getGeometry()),
          interiorPointFinder.findPoint(match.getGeometry())});
    Feature feature = new BasicFeature(schema);
    feature.setGeometry(lineString);
    feature.setAttribute(SCORE, new Double(score));
    return feature;
  }

  private FeatureMatcher featureMatcher(MultiInputDialog dialog) {
    return new ChainMatcher(new FeatureMatcher[] {
      new WindowFilter(dialog.getDouble(ENVELOPE_BUFFER)),
      new SymDiffMatcher(),
      new TopScoreFilter(),
      new ThresholdFilter(dialog.getDouble(MINIMUM_SCORE))
    });
  }

  private MultiInputDialog dialog;

  public static final String REFERENCE_LAYER = "Reference Layer";
  public static final String SUBJECT_LAYER = "Subject Layer";
  public static final String ENVELOPE_BUFFER = "Envelope Buffer";
  public static final String MINIMUM_SCORE = "Minimum Score (0.0 - 1.0)";

  private boolean prompt(PlugInContext context) {
    dialog = new MultiInputDialog(context.getWorkbenchFrame(),
          "Polygon Match", true);
    Layer initialReferenceLayer = context.getLayerManager().getLayer(0);
    Layer initialSubjectLayer = context.getLayerManager().getLayer(0);
    Layer[] selectedLayers = context.getSelectedLayers();
    if (selectedLayers.length > 0) { initialReferenceLayer = selectedLayers[0]; }
    if (selectedLayers.length > 1) { initialSubjectLayer = selectedLayers[1]; }
    dialog.addLayerComboBox(REFERENCE_LAYER, initialReferenceLayer, null, context.getLayerManager());
    dialog.addLayerComboBox(SUBJECT_LAYER, initialSubjectLayer, null, context.getLayerManager());
    dialog.addPositiveDoubleField(ENVELOPE_BUFFER, 50, 5);
    //<<TODO:ENHANCEMENT>> Add "pluggable" field validators to MultiInputDialog,
    //so that for example we can check that the min score lies between 0 and 1.
    //[Jon Aquino]
    dialog.addPositiveDoubleField(MINIMUM_SCORE, 0.5, 5);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    return dialog.wasOKPressed();
  }
}
