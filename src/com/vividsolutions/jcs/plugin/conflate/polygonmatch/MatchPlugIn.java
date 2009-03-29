package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.vividsolutions.jcs.conflate.polygonmatch.AngleHistogramMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.CentroidAligner;
import com.vividsolutions.jcs.conflate.polygonmatch.CentroidDistanceMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.CompactnessMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.FeatureMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.HausdorffDistanceMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.SymDiffMatcher;
import com.vividsolutions.jcs.conflate.polygonmatch.WeightedMatcher;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;

/**
 * Called when the user hits the Match button. A plug-in, because the
 * task-monitor dialog works with plug-ins. Works at the Layer level, whereas
 * MatchEngine works at the FeatureCollection level.
 * @see MatchEngine
 */
public class MatchPlugIn extends ThreadedBasePlugIn {
    public static final String MATCH_PAIR_LAYER_NAME = "Matches";
    private ToolboxPanel panel;
    public MatchPlugIn(ToolboxPanel panel) {
        this.panel = panel;
    }

    public String getName() {
        //Displayed in the task monitor. [Jon Aquino] 
        return "Polygon Matching";
    }

    public boolean execute(PlugInContext context) throws Exception {
        if (validateInput() != null) {
            reportValidationError(validateInput());
        }
        return validateInput() == null;
    }

    private void reportValidationError(String errorMessage) {
        JOptionPane.showMessageDialog(
            SwingUtilities.windowForComponent(panel),
            errorMessage,
            "JUMP",
            JOptionPane.ERROR_MESSAGE);
    }

    public void run(TaskMonitor monitor, PlugInContext context) {
        try {
            //context.getLayerViewPanel().setToolTipText("Score: {score}");            
            Layer targetLayer = panel.getTargetLayerComboBox().getSelectedLayer();
            Layer candidateLayer = panel.getCandidateLayerComboBox().getSelectedLayer();
            panel.getEngine().match(
                targetLayer.getFeatureCollectionWrapper(),
                candidateLayer.getFeatureCollectionWrapper(),
                createFeatureMatcher(),
                panel.getFilterByWindowCheckBox().isSelected(),
                panel.getFilterByWindowField().getDouble(),
                panel.getFilterByAreaCheckBox().isSelected(),
                panel.getFilterByAreaMinField().getDouble(),
                panel.getFilterByAreaMaxField().getDouble(),
                panel.getUnionCheckBox().isSelected(),
                Integer.parseInt(panel.getUnionTextField().getText()),
                monitor);
            monitor.report("Creating layer: " + MATCH_PAIR_LAYER_NAME);
            Layer matchPairLayer =
                addMatchPairLayer(targetLayer, candidateLayer, context);
            monitor.report("Updating chart: " + MATCH_PAIR_LAYER_NAME);
            panel.getChartPanel().setData(matchPairLayer);
            monitor.report("Updating statistics: " + MATCH_PAIR_LAYER_NAME);
            panel.getStatisticsPanel().update(panel.getEngine());
            monitor.report("Creating layer: " + unmatchedFeaturesLayerName(targetLayer));
            addUnmatchedFeaturesLayer(
                panel.getEngine().getUnmatchedTargetsFeatureCollection(),
                targetLayer,
                StandardCategoryNames.RESULT_REFERENCE,
                context);
            monitor.report(
                "Creating layer: " + unmatchedFeaturesLayerName(candidateLayer));
            addUnmatchedFeaturesLayer(
                panel.getEngine().getUnmatchedCandidatesFeatureCollection(),
                candidateLayer,
                StandardCategoryNames.RESULT_SUBJECT,
                context);
            targetLayer.setVisible(false);
            candidateLayer.setVisible(false);
        } catch (Throwable t) {
            context.getErrorHandler().handleThrowable(t);
        }
    }

    private void removeAndDisposeLayer(String name, PlugInContext context) {
        Layer layer = context.getLayerManager().getLayer(name);
        if (layer == null) {
            return;
        }
        context.getLayerManager().remove(layer);
        layer.dispose();
    }

    private Layer addMatchPairLayer(
        Layer targetLayer,
        Layer candidateLayer,
        PlugInContext context) {
        removeAndDisposeLayer(MATCH_PAIR_LAYER_NAME, context);
        Layer matchPairLayer =
            context.getLayerManager().addLayer(
                StandardCategoryNames.RESULT_SUBJECT,
                MATCH_PAIR_LAYER_NAME,
                panel.getEngine().getMatchPairFeatureCollection());
        matchPairLayer.getBasicStyle().setEnabled(false);
        matchPairLayer.addStyle(
            new MatchPairStyle(
                targetLayer.getBasicStyle(),
                candidateLayer.getBasicStyle()));
        return matchPairLayer;
    }

    private void addUnmatchedFeaturesLayer(
        FeatureCollection fc,
        Layer originalLayer,
        String category,
        PlugInContext context) {
        String name = unmatchedFeaturesLayerName(originalLayer);
        removeAndDisposeLayer(name, context);
        Layer layer = context.getLayerManager().addLayer(category, name, fc);
        layer.getBasicStyle().setRenderingFill(false);
        layer.getBasicStyle().setAlpha(MatchPairStyle.LOW_SCORE_LINE_ALPHA);
        layer.getBasicStyle().setLineColor(originalLayer.getBasicStyle().getFillColor());
        layer.fireAppearanceChanged();
    }

    private String unmatchedFeaturesLayerName(Layer originalLayer) {
        return "Unmatched " + originalLayer.getName();
    }

    public FeatureMatcher createFeatureMatcher() {
        return new WeightedMatcher(
            new Object[] {
                new Double(
                    zeroIfNotSelected(
                        panel.getCentroidDistanceWeightField(),
                        panel.getCentroidCheckBox())),
                new CentroidDistanceMatcher(),
                new Double(
                    zeroIfNotSelected(
                        panel.getHausdorffDistanceWeightField(),
                        panel.getHausdorffCheckBox())),
                new CentroidAligner(new HausdorffDistanceMatcher()),
                new Double(
                    zeroIfNotSelected(
                        panel.getSymDiffWeightField(),
                        panel.getSymDiffCheckBox())),
                new SymDiffMatcher(),
                new Double(
                    zeroIfNotSelected(
                        panel.getSymDiffCentroidsAlignedWeightField(),
                        panel.getSymDiffCentroidsAlignedCheckBox())),
                new CentroidAligner(new SymDiffMatcher()),
                new Double(
                    zeroIfNotSelected(
                        panel.getCompactnessWeightField(),
                        panel.getCompactnessCheckBox())),
                new CompactnessMatcher(),
                new Double(
                    zeroIfNotSelected(
                        panel.getAngleWeightField(),
                        panel.getAngleCheckBox())),
                new AngleHistogramMatcher(
                    Integer.parseInt(panel.getAngleBinField().getText()))});
    }
    private double zeroIfNotSelected(MyValidatingTextField field, JCheckBox checkBox) {
        return checkBox.isSelected() ? field.getDouble() : 0;
    }

    public String validateInput() {
        if (panel.getTargetLayerComboBox().getSelectedLayer() == null) {
            return "Layer A is not specified";
        }
        if (panel.getCandidateLayerComboBox().getSelectedLayer() == null) {
            return "Layer B is not specified";
        }
        if (panel.getTargetLayerComboBox().getSelectedLayer()
            == panel.getCandidateLayerComboBox().getSelectedLayer()) {
            return "Layer A and Layer B are the same";
        }
        return null;
    }

}
