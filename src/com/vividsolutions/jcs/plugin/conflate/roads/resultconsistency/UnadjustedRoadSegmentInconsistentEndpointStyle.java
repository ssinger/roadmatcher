package com.vividsolutions.jcs.plugin.conflate.roads.resultconsistency;

import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Set;

import com.vividsolutions.jcs.conflate.roads.model.resultconsistency.UnadjustedRoadSegmentNodeConsistencyRule;
import com.vividsolutions.jcs.plugin.conflate.roads.AbstractInconsistentEndpointStyle;
import com.vividsolutions.jcs.plugin.conflate.roads.RoadStyleUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.ui.Viewport;

public class UnadjustedRoadSegmentInconsistentEndpointStyle
		extends
			AbstractInconsistentEndpointStyle {
	public UnadjustedRoadSegmentInconsistentEndpointStyle() {
		super(UnadjustedRoadSegmentNodeConsistencyRule.START_NEIGHBOURHOOD_KEY,
				UnadjustedRoadSegmentNodeConsistencyRule.END_NEIGHBOURHOOD_KEY);
	}

	protected void paint(Coordinate endpoint, Object neighbourhood,
			final Graphics2D g, final Viewport viewport)
			throws NoninvertibleTransformException {
		//Repaint the endpoint even if it has already been painted as part of
		//another neighbourhood, because neighbourhoods are drawn
		//assymetrically (bulbous at one endpoint) [Jon Aquino 2004-06-05]
		RoadStyleUtil.instance().paintInconsistentEndpoint(endpoint,
				getInconsistentColour(viewport), g, viewport);
		RoadStyleUtil.instance().paintNeighbourhood(endpoint,
				(Set) neighbourhood, getInconsistentColour(viewport), g,
				viewport);
	}

}