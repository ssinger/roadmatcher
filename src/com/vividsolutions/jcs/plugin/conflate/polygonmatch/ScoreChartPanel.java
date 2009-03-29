package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;

import org.jfree.data.AbstractSeriesDataset;
import org.jfree.data.XYDataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The chart component in the toolbox.
 */
public class ScoreChartPanel extends JPanel {
    private boolean zoomingEnabled = true;
    private WorkbenchContext context;
    private List sortedMatchFeatures = new ArrayList();
    private ChartPanel chartPanel;
    private BorderLayout borderLayout = new BorderLayout();
    private ZoomToSelectedItemsPlugIn zoomToSelectedItemsPlugIn =
        new ZoomToSelectedItemsPlugIn();
    private JPanel northPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private WorkbenchToolBar toolBar;
    private MyDataset dataset = new MyDataset();
    private JLabel label = new JLabel();
    private ToolboxPanel panel;
    private ColorSliderPanel colorSliderPanel;

    public ColorSliderPanel getColorSliderPanel() {
        return colorSliderPanel;
    }

    /**
     * Parameterless constructor for JBuilder's GUI designer.
     */
    public ScoreChartPanel() {}
    
    private ThresholdAnnotation thresholdAnnotation = new ThresholdAnnotation();

    public ScoreChartPanel(ToolboxPanel panel, WorkbenchContext context) {
        this.context = context;
        this.panel = panel;
        toolBar = createToolBar(context);
        colorSliderPanel = new ColorSliderPanel(context);

        try {
            jbInit();
            initChart();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        colorSliderPanel.setPreferredSize(
            new Dimension((int) colorSliderPanel.getPreferredSize().getWidth(), 5));
        colorSliderPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                thresholdAnnotation.setThreshold(colorSliderPanel.getThreshold());
            }
        });
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public void setSelectedMatchIndex(int selectedMatchIndex) {
        getPlot().getDomainAxis().setAnchorValue(selectedMatchIndex);
        getPlot().setDomainCrosshairValue(selectedMatchIndex);
        toolBar.updateEnabledState();
    }

    public int getSelectedMatchIndex() {
        return (int) getPlot().getDomainAxis().getAnchorValue();
    }

    private WorkbenchToolBar createToolBar(final WorkbenchContext context) {
        final WorkbenchToolBar toolBar = new WorkbenchToolBar(context);
        toolBar
            .addPlugIn(
                GUIUtil.toSmallIcon(IconLoader.icon("Left.gif")),
                new AbstractPlugIn() {
            public String getName() {
                return "Zoom To Previous Match";
            }
            public boolean execute(PlugInContext context) throws Exception {
                reportNothingToUndoYet(context);
                setSelectedMatchIndex(getSelectedMatchIndex() - 1);
                return true;
            }
        }, new EnableCheck() {
            public String check(JComponent component) {
                if (context.getLayerViewPanel() == null) {
                    return "X";
                }
                return getSelectedMatchIndex() == 0 ? "X" : null;
            }
        }, context);
        toolBar
            .addPlugIn(
                GUIUtil.toSmallIcon(IconLoader.icon("Right.gif")),
                new AbstractPlugIn() {
            public boolean execute(PlugInContext context) throws Exception {
                reportNothingToUndoYet(context);
                setSelectedMatchIndex(getSelectedMatchIndex() + 1);
                return true;
            }
            public String getName() {
                return "Zoom To Next Match";
            }
        }, new EnableCheck() {
            public String check(JComponent component) {
                if (context.getLayerViewPanel() == null) {
                    return "X";
                }
                return (getSelectedMatchIndex() >= sortedMatchFeatures.size() - 1)
                    ? "X"
                    : null;
            }
        }, context);
        toolBar
            .addPlugIn(
                GUIUtil.toSmallIcon(IconLoader.icon("Flashlight.gif")),
                new AbstractPlugIn() {
            public String getName() {
                return "Flash Current Match";
            }
            public boolean execute(PlugInContext context) throws Exception {
                reportNothingToUndoYet(context);
                context.getLayerViewPanel().flash(
                    (GeometryCollection) ((Feature) sortedMatchFeatures
                        .get(getSelectedMatchIndex()))
                        .getGeometry());
                return true;
            }
        }, new EnableCheck() {
            public String check(JComponent component) {
                if (context.getLayerViewPanel() == null) {
                    return "X";
                }
                return sortedMatchFeatures.isEmpty() ? "X" : null;
            }
        }, context);
        return toolBar;
    }

    private XYPlot getPlot() {
        return chartPanel.getChart().getXYPlot();
    }

    void jbInit() throws Exception {
        this.setLayout(borderLayout);
        northPanel.setLayout(gridBagLayout1);
        label.setToolTipText("");
        label.setText("label");
        this.add(northPanel, BorderLayout.NORTH);
        northPanel.add(
            toolBar,
            new GridBagConstraints(
                2,
                0,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0,
                0));
        northPanel.add(
            label,
            new GridBagConstraints(
                3,
                0,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0,
                0));
        this.add(colorSliderPanel, BorderLayout.EAST);
    }

    public void setData(Layer matchPairLayer) {
        try {
            if (matchPairLayer == null) {
                label.setText(" ");
                sortedMatchFeatures = new ArrayList();
                dataset.setScores(new ArrayList());
                return;
            }
            label.setText(
                "Matched: A "
                    + getTargetMatchRate()
                    + "%, B "
                    + getCandidateMatchRate()
                    + "%");
            sortedMatchFeatures = sort(matchPairLayer.getFeatureCollectionWrapper());
            dataset.setScores(toScores(sortedMatchFeatures));
        } finally {
            toolBar.updateEnabledState();
        }
    }

    private int getTargetMatchRate() {
        return (int) Math.round(
            100
                * panel.getEngine().getMatchedTargetsFeatureCollection().size()
                / (double) panel.getEngine().getTargetFeatureCollection().size());
    }

    private int getCandidateMatchRate() {
        return (int) Math.round(
            100
                * panel.getEngine().getMatchedCandidatesFeatureCollection().size()
                / (double) panel.getEngine().getCandidateFeatureCollection().size());
    }

    private class ThresholdAnnotation implements XYAnnotation {
        public void setThreshold(double threshold) {
            this.threshold = threshold;
            chartPanel.repaint();
        }

        public void draw(
            Graphics2D g2,
            XYPlot plot,
            Rectangle2D dataArea,
            ValueAxis domainAxis,
            ValueAxis rangeAxis) {
            g2.setColor(Color.green);
            int y = (int) (((1 - threshold) * dataArea.getHeight()) + dataArea.getMinY());
            g2.drawLine((int) dataArea.getMinX(), y, (int) dataArea.getMaxX(), y);
        }
        private double threshold = 0;
    }

    private void initChart() {
        JFreeChart chart =
            ChartFactory.createScatterPlot(
                null,
                null,
                null,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        renderer.setPlotLines(false);
        renderer.setPlotShapes(true);
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().setDomainGridlinesVisible(false);
        chart.getXYPlot().setRangeGridlinesVisible(true);
        chart.getXYPlot().setDomainCrosshairLockedOnData(true);
        chart.getXYPlot().setRangeCrosshairLockedOnData(true);
        chart.getXYPlot().setDomainCrosshairVisible(true);
        chart.getXYPlot().setRangeCrosshairVisible(true);
        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setAutoRange(false);
        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setMinimumAxisValue(0);
        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setMaximumAxisValue(1);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(150, 170));
        add(chartPanel, BorderLayout.CENTER);
        chartPanel.setVerticalAxisTrace(false);
        chartPanel.setHorizontalAxisTrace(false);
        chartPanel.setVerticalZoom(true);
        chartPanel.setHorizontalZoom(true);
        installPlotChangeListener(chart.getXYPlot());
        chart.getXYPlot().addAnnotation(thresholdAnnotation);
    }

    private void installPlotChangeListener(final XYPlot plot) {
        plot.addChangeListener(new PlotChangeListener() {
            int lastSelectedMatchIndex = -1;

            public void plotChanged(PlotChangeEvent event) {
                toolBar.updateEnabledState();
                try {
                    if (sortedMatchFeatures.isEmpty()) {
                        return;
                    }
                    if (lastSelectedMatchIndex == getSelectedMatchIndex()) {
                        return;
                    }
                    if (!zoomingEnabled) {
                        return;
                    }
                    if (getSelectedMatchIndex() >= sortedMatchFeatures.size()) {
                        //User's clicked to the right of the last point. [Jon Aquino]
                        return;
                    }
                    if (null == context.getLayerViewPanel()) {
                        return;
                    }
                    lastSelectedMatchIndex = getSelectedMatchIndex();

                    Feature matchFeature =
                        (Feature) sortedMatchFeatures.get(getSelectedMatchIndex());
                    zoomToSelectedItemsPlugIn.zoom(
                        Collections.singleton(matchFeature.getGeometry()),
                        context.getLayerViewPanel());
                } catch (Throwable t) {
                    context.getWorkbench().getFrame().warnUser(t.toString());
                    t.printStackTrace(System.err);
                }
            }
        });
    }

    private List toScores(List matchFeatures) {
        ArrayList scores = new ArrayList();

        for (Iterator i = matchFeatures.iterator(); i.hasNext();) {
            Feature matchFeature = (Feature) i.next();
            scores.add(matchFeature.getAttribute(MatchEngine.SCORE_ATTRIBUTE));
        }

        return scores;
    }

    private List sort(FeatureCollection matchFeatures) {
        ArrayList sortedList = new ArrayList(matchFeatures.getFeatures());
        Collections.sort(sortedList, new Comparator() {
            public int compare(Object o1, Object o2) {
                return toComparable(o2).compareTo(toComparable(o1));
            }

            private Comparable toComparable(Object o) {
                return (Comparable) ((Feature) o).getAttribute(
                    MatchEngine.SCORE_ATTRIBUTE);
            }
        });

        return sortedList;
    }

    private static class MyDataset extends AbstractSeriesDataset implements XYDataset {
        private List scores = new ArrayList();
        private List xValues = new ArrayList();

        public void setScores(List scores) {
            this.scores = scores;
            xValues = xValues(scores.size());
            fireDatasetChanged();
        }

        private List xValues(int size) {
            ArrayList xValues = new ArrayList();

            for (int i = 0; i < size; i++) {
                xValues.add(new Integer(i));
            }

            return xValues;
        }

        public int getSeriesCount() {
            return 1;
        }

        public String getSeriesName(int series) {
            return "Scores";
        }

        public int getItemCount(int series) {
            return scores.size();
        }

        public Number getXValue(int series, int item) {
            return (Integer) xValues.get(item);
        }

        public Number getYValue(int series, int item) {
            return (Double) scores.get(item);
        }
    }
    protected void setZoomingEnabled(boolean zoomingEnabled) {
        this.zoomingEnabled = zoomingEnabled;
    }
}
