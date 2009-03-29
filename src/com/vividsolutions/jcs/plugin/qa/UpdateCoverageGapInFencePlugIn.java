

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
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.util.feature.*;
import com.vividsolutions.jcs.qa.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.plugin.*;
import com.vividsolutions.jump.workbench.ui.renderer.style.*;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Updates the layers computed by the CoverageGapPlugin
 * with the gaps found in the current Fence.
 * Only segments which intersect the fence will be processed.
 * The size of the fence is used to determine the distance tolerance
 * (This will produce bad results for very large fences - the user must
 * be aware of this).
 */
public class UpdateCoverageGapInFencePlugIn
    extends AbstractPlugIn
{
  private static void removeFromLayer(PlugInContext context, String layerName, Envelope fence)
  {
    Layer lyr = context.getLayerManager().getLayer(layerName);
    if (lyr == null) return;
    lyr.getFeatureCollectionWrapper().remove(fence);
  }

  private static void addToLayer(PlugInContext context, String layerName, FeatureCollection fc)
  {
    Layer lyr = context.getLayerManager().getLayer(layerName);
    if (lyr == null) return;
    lyr.getFeatureCollectionWrapper().addAll(fc.getFeatures());
  }

  private InternalMatchedSegmentFinder.Parameters param
      = new InternalMatchedSegmentFinder.Parameters();

  public UpdateCoverageGapInFencePlugIn() { }

  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(
          this, "Clean", "Update Gap in Fence", null, new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(1))
          .add(context.getCheckFactory().createFenceMustBeDrawnCheck()));
  }

  public boolean execute(PlugInContext context)
       throws Exception
  {
    computeMatchedSegments(context);
    return true;
  }

  private void computeMatchedSegments(PlugInContext context)
  {
    if (context.getLayerViewPanel().getFence() == null)
      return;
    // we have already checked that there is a fence set
    //Might want to use an Assert in that case [Jon Aquino]
    Envelope fence = context.getLayerViewPanel().getFence().getEnvelopeInternal();
    // use the size of the fence as the distance tolerance
    // this will produce bad results for very big fences, but the user should be aware of this
    double distTol = fence.getHeight();
    if (fence.getWidth() > distTol) distTol = fence.getWidth();
    param.distanceTolerance = distTol;

    Layer layer = context.getCandidateLayer(0);
    InternalMatchedSegmentFinder msf = new InternalMatchedSegmentFinder(
        layer.getFeatureCollectionWrapper(), param, new DummyTaskMonitor());
    msf.setFence(fence);

    FeatureCollection segs = msf.getMatchedSegments();
    FeatureCollection sizeInd = msf.getSizeIndicators();

    removeFromLayer(context, CoverageGapPlugIn.GAP_SEGMENT_LAYER_NAME, fence);
    removeFromLayer(context, CoverageGapPlugIn.GAP_SIZE_LAYER_NAME, fence);

    addToLayer(context, CoverageGapPlugIn.GAP_SEGMENT_LAYER_NAME, segs);
    addToLayer(context, CoverageGapPlugIn.GAP_SIZE_LAYER_NAME, sizeInd);

  }


}
