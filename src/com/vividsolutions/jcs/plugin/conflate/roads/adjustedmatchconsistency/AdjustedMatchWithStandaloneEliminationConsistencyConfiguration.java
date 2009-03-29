package com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency;

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.conflate.roads.model.adjustedmatchconsistency.AdjustedMatchWithStandaloneEliminationConsistencyRule;
import com.vividsolutions.jcs.plugin.conflate.roads.AutoAdjuster;
import com.vividsolutions.jcs.plugin.conflate.roads.ConsistencyConfiguration;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class AdjustedMatchWithStandaloneEliminationConsistencyConfiguration
		implements
			ConsistencyConfiguration {

	private ConsistencyConfiguration configuration = new AdjustedMatchConsistencyConfiguration();
	private ConsistencyRule rule = new AdjustedMatchWithStandaloneEliminationConsistencyRule();

	public Style getStyle() {
		return configuration.getStyle();
	}

	public ConsistencyRule getRule() {
		return rule;
	}

	public void setPostponedForInconsistenciesAt(Point point,
			boolean postponed, PlugInContext context)
			throws NoninvertibleTransformException {
		configuration.setPostponedForInconsistenciesAt(point, postponed,
				context);
	}

	public AutoAdjuster getAutoAdjuster() {
		return configuration.getAutoAdjuster();
	}

}