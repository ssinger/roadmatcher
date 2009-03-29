package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;

/**
 * Augments the BasicStyle of the Matches layer by drawing the outlines of the first 
 * and second geometries of each geometry collection using the colours from the 
 * original layers. When using this Style, be sure to turn off line-drawing in the layer's 
 * BasicStyle. 
 */
public class MatchPairStyle implements Style {
    public static final int LOW_SCORE_LINE_ALPHA = 150;
    private BasicStyle targetStyle;
    private BasicStyle candidateStyle;
    private double threshold;

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public MatchPairStyle(BasicStyle targetStyle, BasicStyle candidateStyle) {
        this.targetStyle = targetStyle;
        this.candidateStyle = candidateStyle;
    }

    private BasicStroke fillStroke = new BasicStroke();

    public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
        GeometryCollection gc = (GeometryCollection) f.getGeometry();
        double score =
            ((Double) f.getAttribute(MatchEngine.SCORE_ATTRIBUTE)).doubleValue();
        paint(gc.getGeometryN(0), score, targetStyle, g, viewport, gc);
        paint(gc.getGeometryN(1), score, candidateStyle, g, viewport, gc);
    }

    private void paint(
        Geometry geometry,
        double score,
        BasicStyle basicStyle,
        Graphics2D g,
        Viewport viewport,
        GeometryCollection gc)
        throws NoninvertibleTransformException {
        StyleUtil.paint(
            geometry,
            g,
            viewport,
            true,
            fillStroke,
            GUIUtil.alphaColor(basicStyle.getFillColor(), score > threshold ? LOW_SCORE_LINE_ALPHA : 50),
            true,
            basicStyle.getLineStroke(),
            GUIUtil.alphaColor(basicStyle.getLineColor(), score > threshold ? 255 : 50));
    }

    public void initialize(Layer layer) {}

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    private boolean enabled = true;
    public boolean isEnabled() {
        return enabled;
    }

}
