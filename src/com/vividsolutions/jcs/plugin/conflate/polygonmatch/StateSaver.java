package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.XYPlot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


/**
 * Sets the fields whenever the active internal frame changes.
 */
public class StateSaver {
    private WorkbenchContext context;
    private ToolboxPanel panel;
    private LayerManager dummyLayerManager = new LayerManager();

    public StateSaver(final ToolboxPanel panel, WorkbenchContext context) {
        this.panel = panel;
        this.context = context;
        monitorChartPanel();

        String TARGET_LAYER_KEY = StateSaver.class + " - TARGET LAYER";
        String CANDIDATE_LAYER_KEY = StateSaver.class + " - CANDIDATE LAYER";
        monitorLayerComboBox(panel.getTargetLayerComboBox(), TARGET_LAYER_KEY,
            CANDIDATE_LAYER_KEY);
        monitorLayerComboBox(panel.getCandidateLayerComboBox(),
            CANDIDATE_LAYER_KEY, TARGET_LAYER_KEY);
        monitorColorSliderPanel();
    }

    private void monitorColorSliderPanel() {
        final String THRESHOLD_KEY = getClass().getName() + " - THRESHOLD";
        final ColorSliderPanel sliderPanel = panel.getChartPanel()
                                                  .getColorSliderPanel();
        ChangeListener changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    blackboard().put(THRESHOLD_KEY, sliderPanel.getThreshold());
                }
            };

        sliderPanel.getSlider().addChangeListener(changeListener);

        InternalFrameAdapter internalFrameAdapter = new InternalFrameAdapter() {
                public void internalFrameActivated(InternalFrameEvent e) {
                    sliderPanel.setThreshold(blackboard().get(THRESHOLD_KEY, 0.0));
                }
            };

        GUIUtil.addInternalFrameListener(context.getWorkbench().getFrame()
                                                .getDesktopPane(),
            internalFrameAdapter);
        changeListener.stateChanged(null);
        internalFrameAdapter.internalFrameActivated(null);
    }

    private void monitorLayerComboBox(final LayerComboBox layerComboBox,
        final String layerKey, final String otherLayerKey) {
        final boolean[] updating = new boolean[] { false };
        ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (updating[0]) {
                        return;
                    }

                    blackboard().put(layerKey, layerComboBox.getSelectedLayer());
                }
            };

        layerComboBox.addActionListener(actionListener);

        InternalFrameAdapter internalFrameListener = new InternalFrameAdapter() {
                public void internalFrameActivated(InternalFrameEvent e) {
                    updating[0] = true;

                    try {
                        layerComboBox.setLayerManager(layerManager());
                        layerComboBox.setSelectedLayer((Layer) blackboard().get(layerKey,
                                pickLayerPreferablyNot(otherLayerKey)));
                    } finally {
                        updating[0] = false;
                    }
                }
            };

        GUIUtil.addInternalFrameListener(context.getWorkbench().getFrame()
                                                .getDesktopPane(),
            internalFrameListener);
        actionListener.actionPerformed(null);
        internalFrameListener.internalFrameActivated(null);
    }

    private void monitorChartPanel() {
        final boolean[] updating = new boolean[] { false };
        final String SELECTED_MATCH_INDEX_KEY = StateSaver.class +
            " - SELECTED MATCH INDEX";
        final String AUTO_RANGING_DOMAIN_KEY = StateSaver.class +
            " - AUTO RANGING DOMAIN";
        final String AUTO_RANGING_RANGE_KEY = StateSaver.class +
            " - AUTO RANGING RANGE";
        final String DOMAIN_MIN_KEY = StateSaver.class + " - DOMAIN MIN";
        final String DOMAIN_MAX_KEY = StateSaver.class + " - DOMAIN MAX";
        final String RANGE_MIN_KEY = StateSaver.class + " - RANGE MIN";
        final String RANGE_MAX_KEY = StateSaver.class + " - RANGE MAX";
        PlotChangeListener listener = new PlotChangeListener() {
                public void plotChanged(PlotChangeEvent event) {
                    if (updating[0]) {
                        return;
                    }

                    blackboard().put(SELECTED_MATCH_INDEX_KEY,
                        new Integer((int) xyPlot().getDomainAxis()
                                              .getAnchorValue()));
                    blackboard().put(AUTO_RANGING_DOMAIN_KEY,
                        new Boolean(xyPlot().getDomainAxis().isAutoRange()));
                    blackboard().put(AUTO_RANGING_RANGE_KEY,
                        new Boolean(xyPlot().getRangeAxis().isAutoRange()));
                    blackboard().put(DOMAIN_MIN_KEY,
                        new Double(xyPlot().getDomainAxis().getMinimumAxisValue()));
                    blackboard().put(DOMAIN_MAX_KEY,
                        new Double(xyPlot().getDomainAxis().getMaximumAxisValue()));
                    blackboard().put(RANGE_MIN_KEY,
                        new Double(xyPlot().getRangeAxis().getMinimumAxisValue()));
                    blackboard().put(RANGE_MAX_KEY,
                        new Double(xyPlot().getRangeAxis().getMaximumAxisValue()));
                }
            };

        xyPlot().addChangeListener(listener);

        InternalFrameAdapter internalFrameListener = new InternalFrameAdapter() {
                public void internalFrameActivated(InternalFrameEvent e) {
                    //Don't want to change what the user was viewing [Jon Aquino]
                    panel.getChartPanel().setZoomingEnabled(false);

                    try {
                        updating[0] = true;
                        try {
                            Layer matchPairLayer = (null != layerManager()
                                                                .getLayer(MatchPlugIn.MATCH_PAIR_LAYER_NAME))
                                ? layerManager().getLayer(MatchPlugIn.MATCH_PAIR_LAYER_NAME)
                                : null;
                            panel.getChartPanel().setData(matchPairLayer);
                            panel.getChartPanel().setSelectedMatchIndex(blackboard()
                                                                            .get(SELECTED_MATCH_INDEX_KEY,
                                    0));
                            xyPlot().getDomainAxis().setAutoRange(blackboard()
                                                                      .get(AUTO_RANGING_DOMAIN_KEY,
                                    true));

                            //Want the range axis to be from 0 to 1; therefore don't auto-range it. [Jon Aquino]                
                            xyPlot().getRangeAxis().setAutoRange(blackboard()
                                                                     .get(AUTO_RANGING_RANGE_KEY,
                                    false));

                            if (!blackboard().getBoolean(AUTO_RANGING_DOMAIN_KEY)) {
                                xyPlot().getDomainAxis().setMinimumAxisValue(blackboard()
                                                                                 .getDouble(DOMAIN_MIN_KEY));
                                xyPlot().getDomainAxis().setMaximumAxisValue(blackboard()
                                                                                 .getDouble(DOMAIN_MAX_KEY));
                            }

                            if (!blackboard().getBoolean(AUTO_RANGING_RANGE_KEY)) {
                                xyPlot().getRangeAxis().setMinimumAxisValue(blackboard()
                                                                                .get(RANGE_MIN_KEY,
                                        0.0));
                                xyPlot().getRangeAxis().setMaximumAxisValue(blackboard()
                                                                                .get(RANGE_MAX_KEY,
                                        1.0));
                            }
                        } finally {
                            updating[0] = false;
                        }
                    } finally {
                        panel.getChartPanel().setZoomingEnabled(true);
                    }
                }
            };

        GUIUtil.addInternalFrameListener(context.getWorkbench().getFrame()
                                                .getDesktopPane(),
            internalFrameListener);
        listener.plotChanged(null);
        internalFrameListener.internalFrameActivated(null);
    }

    private XYPlot xyPlot() {
        return panel.getChartPanel().getChartPanel().getChart().getXYPlot();
    }

    private LayerManager layerManager() {
        return (context.getLayerManager() == null) ? dummyLayerManager
                                                   : context.getLayerManager();
    }

    private Blackboard blackboard() {
        return layerManager().getBlackboard();
    }

    private Layer pickLayerPreferablyNot(String layerToAvoidKey) {
        Layer layerToAvoid = (Layer) layerManager().getBlackboard().get(layerToAvoidKey);
        ArrayList candidates = new ArrayList(layerManager().getLayers());
        candidates.remove(layerToAvoid);

        return (!candidates.isEmpty()) ? (Layer) candidates.iterator().next()
                                       : layerToAvoid;
    }
}
