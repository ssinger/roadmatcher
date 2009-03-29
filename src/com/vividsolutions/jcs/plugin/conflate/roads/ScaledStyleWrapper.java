package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Graphics2D;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class ScaledStyleWrapper extends AbstractScaledStyle {

	private Style style;

	public ScaledStyleWrapper(Style style) {
		this.style = style;
	}

	public void initialize(Layer layer) {
		style.initialize(layer);
	}

	public void paint(Feature f, Graphics2D g, Viewport viewport)
			throws Exception {
		if (getMinScale() < 1d / viewport.getScale()
				&& 1d / viewport.getScale() < getMaxScale()) {
			style.paint(f, g, viewport);
		}
	}

}