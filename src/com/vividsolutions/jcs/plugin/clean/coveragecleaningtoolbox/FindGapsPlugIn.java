package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import java.awt.Color;
import java.util.Collection;

import com.vividsolutions.jcs.conflate.coverage.CoverageGapRemover;
import com.vividsolutions.jcs.qa.InternalMatchedSegmentFinder;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.feature.FeatureStatistics;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;


public class FindGapsPlugIn extends AbstractFindPlugIn {
    public static final String GAP_SIZES_LAYER_NAME = "Gap Sizes";
    public static final String GAP_SEGMENTS_LAYER_NAME = "Gap Segments";

    public FindGapsPlugIn(ToolboxPanel toolboxPanel) {
        super("Find Gaps", toolboxPanel);
    }

    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
        monitor.allowCancellationRequests();
        initLogPanel();
        getToolboxPanel().getLogPanel().addField("Distance Tolerance: ",
            "" + getToolboxPanel().getGapToleranceTextField().getDouble());
        getToolboxPanel().getLogPanel().addField("Angle Tolerance: ",
            "" + getToolboxPanel().getAngleToleranceTextField().getDouble());
        //Ensure that the QA category appears before the Result-Subject category,
        //so that gap indicators are rendered above the data [Jon Aquino]
        inputLayer().getLayerManager().addCategory(StandardCategoryNames.QA);
        if (getToolboxPanel().getFixAutomaticallyCheckBox().isSelected()) {
            fixGaps(monitor);
        }
        findGaps(monitor);
    }

    private void fixGaps(TaskMonitor monitor) {
        if (monitor.isCancelRequested()) {
            return;
        }

        monitor.report("Removing Gaps...");

        CoverageGapRemover remover = new CoverageGapRemover(inputLayer()
                                                                .getFeatureCollectionWrapper(),
                monitor);
        remover.process(new CoverageGapRemover.Parameters(
                getToolboxPanel().getGapToleranceTextField().getDouble(),
                getToolboxPanel().getAngleToleranceTextField().getDouble()));

        if (monitor.isCancelRequested()) {
            return;
        }

        inputLayer().setVisible(false);

        Collection inputLayerStyles = inputLayer().cloneStyles();
        getToolboxPanel().getLayerDependencyManager().setIgnoringLayerChanges(getToolboxPanel().getGapsTab(), true);

        try {
            generateLayer(getToolboxPanel().getOutputLayerTextField().getText(),
                StandardCategoryNames.RESULT_SUBJECT,
                inputLayer().getBasicStyle().getFillColor(),
                getToolboxPanel().getContext(), remover.getUpdatedFeatures(),
                inputLayer().getDescription()).setStyles(inputLayerStyles);
        } finally {
            getToolboxPanel().getLayerDependencyManager().setIgnoringLayerChanges(getToolboxPanel().getGapsTab(), false);
        }

        //Editable so that user can use Snap Vertices tool on it [Jon Aquino]
        outputLayer().setEditable(true);
        outputLayer().setVisible(true);

        getToolboxPanel().getInputLayerComboBox().setSelectedLayer(outputLayer());

        Layer autoFixedFeaturesLayer = generateLayer("Auto-Fixed Features",
                StandardCategoryNames.QA, Color.green.darker(),
                getToolboxPanel().getContext(), remover.getAdjustedFeatures(),
                "Auto-fixed features for " + inputLayer() + " " +
                parameterDescription());
        autoFixedFeaturesLayer.fireAppearanceChanged();

        Layer autoFixedGapSizesLayer = generateLineLayer("Auto-Fixed Gap Sizes",
                StandardCategoryNames.QA, Color.green,
                getToolboxPanel().getContext(),
                remover.getAdjustmentIndicators(),
                "Auto-fix-size indicators for " + inputLayer() + " " +
                parameterDescription());
        autoFixedGapSizesLayer.setVisible(true);
        getToolboxPanel().getAutoFixedTab().setLayer(autoFixedGapSizesLayer,
            getToolboxPanel().getContext());
        autoFixedGapSizesLayer.fireAppearanceChanged();

        if (monitor.isCancelRequested()) {
            return;
        }

        log(remover);
    }

    private void findGaps(TaskMonitor monitor) {
        if (monitor.isCancelRequested()) {
            return;
        }

        monitor.report("Finding Gaps...");

        InternalMatchedSegmentFinder finder = new InternalMatchedSegmentFinder(inputLayer()
                                                                                   .getFeatureCollectionWrapper(),
                new InternalMatchedSegmentFinder.Parameters(getToolboxPanel()
                                                                .getGapToleranceTextField()
                                                                .getDouble(),
                    getToolboxPanel().getAngleToleranceTextField().getDouble()),
                monitor);
        finder.computeMatches();

        if (monitor.isCancelRequested()) {
            return;
        }

        generateLineLayer(GAP_SEGMENTS_LAYER_NAME, StandardCategoryNames.QA,
            Color.cyan, getToolboxPanel().getContext(),
            finder.getMatchedSegments(),
            "Gap segments for " + inputLayer() + " " + parameterDescription());

        Layer gapSizesLayer = generateLineLayer(GAP_SIZES_LAYER_NAME,
                StandardCategoryNames.QA, Color.blue,
                getToolboxPanel().getContext(), finder.getSizeIndicators(),
                "Gap-size indicators for " + inputLayer() + " " +
                parameterDescription());
        gapSizesLayer.setVisible(true);
        getToolboxPanel().getGapsTab().setLayer(gapSizesLayer,
            getToolboxPanel().getContext());
        getToolboxPanel().getLayerDependencyManager().markAsUpToDate(getToolboxPanel()
                                                                         .getGapsTab(),
            inputLayer());

        if (monitor.isCancelRequested()) {
            return;
        }

        log(finder);
    }

    private Layer outputLayer() {
        return getToolboxPanel().getContext().getLayerManager().getLayer(getToolboxPanel()
                                                                             .getOutputLayerTextField()
                                                                             .getText());
    }

    public void log(CoverageGapRemover remover) {
        getToolboxPanel().getLogPanel().addHeader(1, "Coverage Gap Removal");
        getToolboxPanel().getLogPanel().addField("Features Adjusted: ",
            "" + remover.getAdjustedFeatures().size());
        getToolboxPanel().getLogPanel().addField("Vertices Adjusted: ",
            "" + remover.getAdjustmentIndicators().size());

        double[] minMax = FeatureStatistics.minMaxValue(remover.getAdjustmentIndicators(),
                "LENGTH");
        getToolboxPanel().getLogPanel().addField("Min Adjustment Size: ",
            "" + minMax[0]);
        getToolboxPanel().getLogPanel().addField("Max Adjustment Size: ",
            "" + minMax[1]);
    }

    public void log(InternalMatchedSegmentFinder finder) {
        getToolboxPanel().getLogPanel().addHeader(1, "Coverage Gaps");
        getToolboxPanel().getLogPanel().addField("Matched Segments: ",
            "" + finder.getMatchedSegments().size());
        getToolboxPanel().getLogPanel().addField("Gaps: ",
            "" + finder.getSizeIndicators().size());

        double[] minMax = FeatureStatistics.minMaxValue(finder.getSizeIndicators(),
                "LENGTH");
        getToolboxPanel().getLogPanel().addField("Min Gap Size: ",
            "" + minMax[0]);
        getToolboxPanel().getLogPanel().addField("Max Gap Size: ",
            "" + minMax[1]);
    }
}
