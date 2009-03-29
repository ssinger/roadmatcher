package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Layer;

public abstract class AbstractScaledStyle implements ScaledStyle {
	private double minScale = 0;

	private double maxScale = Double.MAX_VALUE;

	public double getMaxScale() {
		return maxScale;
	}

	public ScaledStyle setMaxScale(double maxScale) {
		Assert.isTrue(minScale < maxScale);
		this.maxScale = maxScale;
		return this;
	}

	public double getMinScale() {
		return minScale;
	}

	public ScaledStyle setMinScale(double minScale) {
		Assert.isTrue(minScale < maxScale);
		this.minScale = minScale;
		return this;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.shouldNeverReachHere();
			return null;
		}
	}
	
	public void initialize(Layer layer) {
	}

	private boolean enabled = true;	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}