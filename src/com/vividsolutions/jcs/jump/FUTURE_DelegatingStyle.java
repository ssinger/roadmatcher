package com.vividsolutions.jcs.jump;

import java.awt.Graphics2D;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.DummyStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class FUTURE_DelegatingStyle implements Style {
    private Style style = DummyStyle.instance();

    public FUTURE_DelegatingStyle setStyle(Style style) {
        this.style = style;
        return this;
    }

    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
        style.paint(f, g, viewport);
    }

    public void initialize(Layer layer) {
        style.initialize(layer);
    }

    public Object clone() {
        return new FUTURE_DelegatingStyle().setStyle((Style)style.clone());
    }

    public void setEnabled(boolean enabled) {
        style.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return style.isEnabled();
    }
	public Style getStyle() {
		return style;
	}
}
