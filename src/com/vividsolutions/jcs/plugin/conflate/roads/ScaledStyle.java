package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface ScaledStyle extends Style {
	public double getMinScale();

	public double getMaxScale();

	public ScaledStyle setMinScale(double minScale);

	public ScaledStyle setMaxScale(double maxScale);
}