package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import java.awt.Color;

public class ColorSliderPanel extends JPanel {
    private BorderLayout borderLayout1 = new BorderLayout();
    private JSlider slider = new JSlider();
    private WorkbenchContext context;

    public JSlider getSlider() {
        return slider;
    }
    private LayerManager dummyLayerManager = new LayerManager();
    private JLabel label = new JLabel();
    public ColorSliderPanel(WorkbenchContext context) {
        this.context = context;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        label.setToolTipText(slider.getToolTipText());
        ChangeListener changeListener = new ChangeListener() {
            private DecimalFormat formatter = new DecimalFormat("0.00");
            public void stateChanged(ChangeEvent e) {
                label.setText(formatter.format(
                    Math.round(getThreshold() * 100) / 100.0));
                if (slider.getValueIsAdjusting()) {
                    return;
                }
                Layer layer = layerManager().getLayer(MatchPlugIn.MATCH_PAIR_LAYER_NAME);
                if (layer == null) {
                    return;
                }
                ((MatchPairStyle) layer.getStyle(MatchPairStyle.class)).setThreshold(getThreshold());
                layer.fireAppearanceChanged();
            }
        };
        slider.addChangeListener(changeListener);
        changeListener.stateChanged(null);
    }

    private LayerManager layerManager() {
        return (context.getLayerManager() == null)
            ? dummyLayerManager
            : context.getLayerManager();
    }
    void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        slider.setOrientation(SwingConstants.VERTICAL);
        slider.setToolTipText("Matches below this score will be drawn with a paler color");
        label.setForeground(Color.blue);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText("0.0");
        this.add(slider, BorderLayout.CENTER);
        this.add(label,  BorderLayout.SOUTH);
        slider.setValue(0);
    }
    public double getThreshold() {
        return slider.getValue() / 100.0;
    }
    public void setThreshold(double threshold) {
        slider.setValue((int) (threshold * 100));
    }
    public void addChangeListener(ChangeListener l) {
        slider.addChangeListener(l);
    }

}