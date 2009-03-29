package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Color;

import com.vividsolutions.jcs.conflate.roads.model.SourceFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

public class PanToOtherEndTool extends SpecifyClosestRoadFeatureTool {

    public PanToOtherEndTool(WorkbenchContext context) {
        super(true, true, null, null, Color.black, context, GestureMode.POINT);
    }

    protected void gestureFinished(SourceFeature feature, Layer layer)
            throws Exception {
        FindClosestRoadSegmentPlugIn.panTo(feature.getRoadSegment()
                .apparentEndpointFurthestFrom(
                        getPanel().getViewport().toModelCoordinate(
                                getPanel().getLastClickedPoint())), getPanel());
    }

    private Coordinate viewportCentre() {
        return EnvelopeUtil.centre(getPanel().getViewport()
                .getEnvelopeInModelCoordinates());
    }

}
