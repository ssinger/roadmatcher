package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jcs.conflate.roads.model.ConsistencyRule;
import com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency.AdjustedMatchConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency.AdjustedMatchWithStandaloneEliminationConsistencyConfiguration;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface ConsistencyConfiguration {
	//Store class name rather than class: if we store the class, XML2Java won't
	//be able to re-create the class because XMLBinder's CustomConverter for
	//Class uses the *default* class loader, which doesn't know about the
	// classes in the JCS extension. [Jon Aquino 2004-02-04]
	public static final String CURRENT_CLASS_KEY = ConsistencyConfiguration.class + " - CURRENT CLASS";

	public static final Class DEFAULT_CLASS = AdjustedMatchWithStandaloneEliminationConsistencyConfiguration.class;

	public Style getStyle();

	public ConsistencyRule getRule();

	public void setPostponedForInconsistenciesAt(Point point,
			boolean postponed, PlugInContext context)
			throws NoninvertibleTransformException;

	public AutoAdjuster getAutoAdjuster();
}