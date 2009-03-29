package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

public abstract class SpecifyClosestRoadFeatureTool extends
        SpecifyNClosestRoadFeaturesTool {

    public SpecifyClosestRoadFeatureTool(boolean forLayer0, boolean forLayer1,
            String cursorImage, String buttonImage, Color color,
            WorkbenchContext context, GestureMode gestureMode) {
        super(1, forLayer0, forLayer1, cursorImage, buttonImage, color,
                context, gestureMode);
    }

    protected void gestureFinished(Map layerToSpecifiedFeaturesMap)
            throws Exception {
        gestureFinished(
                (SourceFeature) ((Collection) layerToSpecifiedFeaturesMap
                        .values().iterator().next()).iterator().next(),
                (Layer) layerToSpecifiedFeaturesMap.keySet().iterator().next());
    }

    protected abstract void gestureFinished(SourceFeature feature, Layer layer)
            throws Exception;

}
