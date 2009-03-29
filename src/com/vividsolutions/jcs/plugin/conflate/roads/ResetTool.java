package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

public class ResetTool extends SetUnmatchedStateTool {

    public ResetTool(boolean layer0, boolean layer1, String cursorImage, String buttonImage, Color color, WorkbenchContext context) {
        super(SourceState.UNKNOWN, layer0, layer1, cursorImage, buttonImage, color, context, GestureMode.LINE);
    }
    
    protected Map layerToSpecifiedFeaturesMap()
            throws NoninvertibleTransformException {
        Map layerToSpecifiedFeaturesMap = super.layerToSpecifiedFeaturesMap();
        for (Iterator i = layerToSpecifiedFeaturesMap.keySet().iterator(); i.hasNext(); ) {
            Layer layer = (Layer) i.next();
            Collection features = (Collection) layerToSpecifiedFeaturesMap.get(layer);
            for (Iterator j = features.iterator(); j.hasNext(); ) {
                SourceFeature feature = (SourceFeature) j.next();
                if (feature.getRoadSegment().getState() == SourceState.UNKNOWN) {
                    j.remove();
                    noRoadSegmentsWarning = "Already reset";
                }
            }
            if (features.isEmpty()) { i.remove(); }
        }
        return layerToSpecifiedFeaturesMap;        
    }

}
