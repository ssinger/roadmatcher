package com.vividsolutions.jcs.jump;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class FUTURE_CompositeStyle implements Style {
    private List styles = new ArrayList();
    public FUTURE_CompositeStyle add(Style style) {
        styles.add(style);
        return this;
    }
    public void paint(final Feature f, final Graphics2D g, final Viewport viewport) throws Exception {
        for (Iterator i = styles.iterator(); i.hasNext(); ) {
            Style style = (Style) i.next();
            style.paint(f, g, viewport);
        }
    }
    public void initialize(Layer layer) {
        for (Iterator i = styles.iterator(); i.hasNext(); ) {
            Style style = (Style) i.next();
            style.initialize(layer);
        }
    }
    public Object clone() {
        throw new UnsupportedOperationException();
    }
    public void setEnabled(boolean enabled) {
        for (Iterator i = styles.iterator(); i.hasNext(); ) {
            Style style = (Style) i.next();
            style.setEnabled(enabled);
        }
    }
    public boolean isEnabled() {
        return styles.isEmpty() ? false : ((Style)styles.iterator().next()).isEnabled();
    }
}
