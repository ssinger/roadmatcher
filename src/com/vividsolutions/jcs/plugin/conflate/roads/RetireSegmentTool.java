package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jcs.conflate.roads.model.SourceState;
import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jcs.jump.FUTURE_PreventableConfirmationDialog;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

public class RetireSegmentTool extends SetUnmatchedStateTool {

    public RetireSegmentTool(boolean layer0, boolean layer1,
            String cursorImage, String buttonImage, Color color,
            WorkbenchContext context) {
        super(SourceState.RETIRED, layer0, layer1, cursorImage, buttonImage,
                color, context, GestureMode.LINE);
    }

    protected Map layerToSpecifiedFeaturesMap()
            throws NoninvertibleTransformException {
        Boolean retiringCommittedFeatures = null;
        Map layerToSpecifiedFeaturesMap = super.layerToSpecifiedFeaturesMap();
        for (Iterator i = layerToSpecifiedFeaturesMap.keySet().iterator(); i
                .hasNext();) {
            Layer layer = (Layer) i.next();
            Collection features = (Collection) layerToSpecifiedFeaturesMap
                    .get(layer);
            for (Iterator j = features.iterator(); j.hasNext();) {
                SourceFeature feature = (SourceFeature) j.next();
                if (feature.getRoadSegment().getState().indicates(
                        SourceState.COMMITTED)) {
                    if (retiringCommittedFeatures == null) {
                        retiringCommittedFeatures = Boolean
                                .valueOf(FUTURE_PreventableConfirmationDialog
                                        .show(
                                                getContext().getWorkbench()
                                                        .getFrame(),
                                                "RoadMatcher",
                                                ErrorMessages.retireRoadSegmentTool_committed_statusLineWarning,
                                                ErrorMessages.retireRoadSegmentTool_committed_dialogText,
                                                "Retire them",
                                                "Skip them",
                                                getClass().getName()
                                                        + " - DO NOT SHOW AGAIN"));
                    }
                    if (!retiringCommittedFeatures.booleanValue()) {
                        j.remove();
                    }
                } else if (feature.getRoadSegment().getState() == SourceState.RETIRED) {
                    j.remove();
                    noRoadSegmentsWarning = ErrorMessages.retireRoadSegmentTool_alreadyRetired;
                }
            }
            if (features.isEmpty()) {
                i.remove();
            }
        }
        return layerToSpecifiedFeaturesMap;
    }

}
