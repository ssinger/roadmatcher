package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public interface AutoAdjuster {

    /**
     * @return Boolean.TRUE, Boolean.FALSE, or a String reason for why it is
     * not adjustable
     */
    public Object autoAdjustable(SourceRoadSegment roadSegment, Coordinate apparentEndpoint, ConflationSession session);

    public Adjustment adjustment(SourceRoadSegment roadSegment,
            Coordinate apparentEndpoint, ConflationSession session,
            SimpleAdjustmentMethod adjustmentMethod, LayerViewPanel layerViewPanel) throws ZeroLengthException;

}
