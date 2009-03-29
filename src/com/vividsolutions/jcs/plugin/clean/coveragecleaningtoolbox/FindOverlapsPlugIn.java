package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import java.awt.Color;

import com.vividsolutions.jcs.qa.InternalOverlapFinder;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.ColorUtil;
import com.vividsolutions.jump.util.feature.FeatureStatistics;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class FindOverlapsPlugIn extends AbstractFindPlugIn {
    public FindOverlapsPlugIn(ToolboxPanel toolboxPanel) {
        super("Find Overlaps", toolboxPanel);
    }
    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
        monitor.allowCancellationRequests();
        initLogPanel();
        findOverlaps(monitor);
    }
    private void findOverlaps(TaskMonitor monitor) {
        if (monitor.isCancelRequested()) {
            return;
        }

        monitor.report("Finding Overlaps...");

        InternalOverlapFinder finder = new InternalOverlapFinder(inputLayer()
                                                                     .getFeatureCollectionWrapper());
        finder.computeOverlaps();

        if (monitor.isCancelRequested()) {
            return;
        }

        generateLayer("Overlapping Features", StandardCategoryNames.QA,
            ColorUtil.GOLD, getToolboxPanel().getContext(), finder.getOverlappingFeatures(),
            "Overlapping features for " + inputLayer() + " " +
            parameterDescription());

        Layer overlapSegmentsLayer = generateLineLayer("Overlap Segments",
                StandardCategoryNames.QA, Color.magenta, getToolboxPanel().getContext(),
                finder.getOverlapIndicators(),
                "Overlap segments for " + inputLayer() + " " +
                parameterDescription());
        overlapSegmentsLayer.setVisible(true);
        getToolboxPanel().getOverlapsTab().setLayer(overlapSegmentsLayer, getToolboxPanel().getContext());
        getToolboxPanel().getLayerDependencyManager().markAsUpToDate(getToolboxPanel().getOverlapsTab(),
            inputLayer());
        generateLineLayer("Overlap Sizes", StandardCategoryNames.QA,
            Color.red, getToolboxPanel().getContext(), finder.getOverlapSizeIndicators(),
            "Overlap-size indicators for " + inputLayer() + " " +
            parameterDescription());

        if (monitor.isCancelRequested()) {
            return;
        }

        log(finder);
    }    
    public void log(InternalOverlapFinder finder) {
            getToolboxPanel().getLogPanel().addHeader(1, "Coverage Overlaps");
            getToolboxPanel().getLogPanel().addField("Overlapping Features: ",
                "" + finder.getOverlappingFeatures().size());

            double[] minMax = FeatureStatistics.minMaxValue(finder.getOverlapSizeIndicators(),
                    "LENGTH");
            getToolboxPanel().getLogPanel().addField("Min Overlap Size: ", "" +
                minMax[0]);
            getToolboxPanel().getLogPanel().addField("Max Overlap Size: ", "" +
                minMax[1]);
        }    
}
