package com.vividsolutions.jcs.plugin.conflate.roads.resultconsistency;

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.resultconsistency.ResultConsistencyRule;
import com.vividsolutions.jcs.jump.FUTURE_CompositeStyle;
import com.vividsolutions.jcs.plugin.conflate.roads.ConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.AutoAdjuster;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class ResultConsistencyConfiguration implements ConsistencyConfiguration {

    private ResultConsistencyRule rule = new ResultConsistencyRule();

    private Style style = new FUTURE_CompositeStyle().add(
            new AdjustedRoadSegmentInconsistentEndpointStyle())
            .add(new UnadjustedRoadSegmentInconsistentEndpointStyle());

    public Style getStyle() {
        return style;
    }

    public ConsistencyRule getRule() {
        return rule;
    }

    public AutoAdjuster getAutoAdjuster() {
        throw new UnsupportedOperationException();
    }

	public void setPostponedForInconsistenciesAt(Point point, boolean postponed, PlugInContext context) throws NoninvertibleTransformException {		
		throw new UnsupportedOperationException();
	}

}
