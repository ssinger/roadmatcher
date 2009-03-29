package com.vividsolutions.jcs.plugin.conflate.polygonmatch;



import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.*;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


/**
 * GUI components.
 */
public class ToolboxPanel extends JPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JTabbedPane upperTabbedPane = new JTabbedPane();
    private JButton matchButton = new JButton("Match",
            IconLoader.icon("GreenFlag.gif"));
    private JPanel layerPanel = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private LayerComboBox candidateLayerComboBox = new LayerComboBox();
    private LayerComboBox targetLayerComboBox = new LayerComboBox();
    private JLabel candidateLabel = new JLabel();
    private JPanel filteringTab = new JPanel();
    private JPanel unioningTab = new JPanel();
    private JPanel matchingTab = new JPanel();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private GridBagLayout gridBagLayout4 = new GridBagLayout();
    private MatchEngine engine = new MatchEngine();
    private GridBagLayout gridBagLayout5 = new GridBagLayout();
    private JPanel filterByAreaPanel = new JPanel();
    private GridBagLayout gridBagLayout6 = new GridBagLayout();
    private JCheckBox filterByAreaCheckBox = new JCheckBox("", true);
    private MyValidatingTextField filterByAreaMinField = new MyValidatingTextField("0",
            4, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private MyValidatingTextField filterByAreaMaxField = new MyValidatingTextField("9E6",
            4, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JLabel filterByAreaLabel2 = new JLabel();
    private JLabel filterByAreaLabel1 = new JLabel();
    private JPanel filterByWindowPanel = new JPanel();
    private GridBagLayout gridBagLayout7 = new GridBagLayout();
    private JCheckBox filterByWindowCheckBox = new JCheckBox("", true);
    private JLabel filterByWindowLabel = new JLabel();
    private MyValidatingTextField filterByWindowField = new MyValidatingTextField("50",
            4, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JCheckBox unionCheckBox = new JCheckBox("", false);
    private JLabel unionLabel1 = new JLabel();
    private MyValidatingTextField unionTextField = new MyValidatingTextField("2",
            1,
            new MyValidatingTextField.CompositeValidator(new MyValidatingTextField.Validator[] {
                    MyValidatingTextField.NON_NEGATIVE_INTEGER_VALIDATOR,
                    new MyValidatingTextField.GreaterThanValidator(1.5)
                }), "2");
    private JLabel unionLabel2 = new JLabel();
    private MyValidatingTextField angleBinField = new MyValidatingTextField("18",
            3,
            new MyValidatingTextField.CompositeValidator(new MyValidatingTextField.Validator[] {
                    MyValidatingTextField.NON_NEGATIVE_INTEGER_VALIDATOR,
                    new MyValidatingTextField.GreaterThanValidator(1.5)
                }), "2");
    private JLabel weightLabel = new JLabel();
    private JCheckBox centroidCheckBox = new JCheckBox("", true);
    private MyValidatingTextField centroidDistanceWeightField = new MyValidatingTextField("10",
            3, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JLabel centroidLabel = new JLabel();
    private JCheckBox hausdorffCheckBox = new JCheckBox("", true);
    private MyValidatingTextField hausdorffDistanceWeightField = new MyValidatingTextField("10",
            3, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JLabel hausdorffLabel = new JLabel();
    private JCheckBox symDiffCheckBox = new JCheckBox("", true);
    private MyValidatingTextField symDiffWeightField = new MyValidatingTextField("10",
            3, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JLabel symDiffLabel = new JLabel();
    private JCheckBox symDiffCentroidsAlignedCheckBox = new JCheckBox("", true);
    private MyValidatingTextField symDiffCentroidsAlignedWeightField = new MyValidatingTextField("10",
            3, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JLabel symDiffCentroidsAlignedLabel = new JLabel();
    private JCheckBox compactnessCheckBox = new JCheckBox("", true);
    private MyValidatingTextField compactnessWeightField = new MyValidatingTextField("10",
            3, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JLabel compactnessLabel = new JLabel();
    private JCheckBox angleCheckBox = new JCheckBox("", true);
    private MyValidatingTextField angleWeightField = new MyValidatingTextField("10",
            3, MyValidatingTextField.NON_NEGATIVE_DOUBLE_VALIDATOR, "0");
    private JPanel anglePanel = new JPanel();
    private GridBagLayout gridBagLayout8 = new GridBagLayout();
    private JLabel angleLabel = new JLabel();
    private JPanel matchingFillerPanel = new JPanel();
    private JTabbedPane lowerTabbedPane = new JTabbedPane();
    private ScoreChartPanel chartPanel = new ScoreChartPanel();
    private JLabel targetLabel = new JLabel();
    private JTextArea filterByAreaTextArea = new JTextArea();
    private JTextArea unioningTextArea = new JTextArea();
    private StatisticsPanel statisticsPanel = new StatisticsPanel();

    public ToolboxPanel(WorkbenchContext context) {
        chartPanel = new ScoreChartPanel(this, context);
        new StateSaver(this, context);
        matchButton.addActionListener(AbstractPlugIn.toActionListener(
                new MatchPlugIn(this), context, new TaskMonitorManager()));
        filterByAreaTextArea.setFont(angleLabel.getFont().deriveFont(Font.ITALIC));
        unioningTextArea.setFont(angleLabel.getFont().deriveFont(Font.ITALIC));
        handleLabelClicks();

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MatchEngine getEngine() {
        return engine;
    }

    public MyValidatingTextField getAngleBinField() {
        return angleBinField;
    }

    public JCheckBox getAngleCheckBox() {
        return angleCheckBox;
    }

    public MyValidatingTextField getAngleWeightField() {
        return angleWeightField;
    }

    public JCheckBox getCentroidCheckBox() {
        return centroidCheckBox;
    }

    public MyValidatingTextField getCentroidDistanceWeightField() {
        return centroidDistanceWeightField;
    }

    public JCheckBox getCompactnessCheckBox() {
        return compactnessCheckBox;
    }

    public MyValidatingTextField getCompactnessWeightField() {
        return compactnessWeightField;
    }

    public JCheckBox getHausdorffCheckBox() {
        return hausdorffCheckBox;
    }

    public MyValidatingTextField getHausdorffDistanceWeightField() {
        return hausdorffDistanceWeightField;
    }

    public JCheckBox getSymDiffCheckBox() {
        return symDiffCheckBox;
    }

    public MyValidatingTextField getSymDiffWeightField() {
        return symDiffWeightField;
    }

    public JCheckBox getSymDiffCentroidsAlignedCheckBox() {
        return symDiffCentroidsAlignedCheckBox;
    }

    public MyValidatingTextField getSymDiffCentroidsAlignedWeightField() {
        return symDiffCentroidsAlignedWeightField;
    }

    public void setFilterByAreaMaxField(
        MyValidatingTextField filterByAreaMaxField) {
        this.filterByAreaMaxField = filterByAreaMaxField;
    }

    public void setFilterByAreaMinField(
        MyValidatingTextField filterByAreaMinField) {
        this.filterByAreaMinField = filterByAreaMinField;
    }

    public void setFilterByWindowCheckBox(JCheckBox filterByWindowCheckBox) {
        this.filterByWindowCheckBox = filterByWindowCheckBox;
    }

    public void setUnionTextField(MyValidatingTextField unionTextField) {
        this.unionTextField = unionTextField;
    }

    public JCheckBox getFilterByAreaCheckBox() {
        return filterByAreaCheckBox;
    }

    public MyValidatingTextField getFilterByAreaMaxField() {
        return filterByAreaMaxField;
    }

    public MyValidatingTextField getFilterByAreaMinField() {
        return filterByAreaMinField;
    }

    public JCheckBox getFilterByWindowCheckBox() {
        return filterByWindowCheckBox;
    }

    public MyValidatingTextField getFilterByWindowField() {
        return filterByWindowField;
    }

    public JCheckBox getUnionCheckBox() {
        return unionCheckBox;
    }

    public MyValidatingTextField getUnionTextField() {
        return unionTextField;
    }

    private void handleLabelClicks() {
        handleClicks(filterByWindowLabel, filterByWindowCheckBox);
        handleClicks(filterByAreaLabel1, filterByAreaCheckBox);
        handleClicks(unionLabel1, unionCheckBox);
    }

    private void handleClicks(JLabel label, final JCheckBox checkBox) {
        label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    checkBox.doClick();
                }
            });
    }

    public LayerComboBox getCandidateLayerComboBox() {
        return candidateLayerComboBox;
    }

    public LayerComboBox getTargetLayerComboBox() {
        return targetLayerComboBox;
    }

    public ScoreChartPanel getChartPanel() {
        return chartPanel;
    }

    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        layerPanel.setLayout(gridBagLayout2);
        candidateLabel.setText("Layer B: ");
        filteringTab.setLayout(gridBagLayout3);
        unioningTab.setLayout(gridBagLayout4);
        matchingTab.setLayout(gridBagLayout5);
        filterByAreaPanel.setLayout(gridBagLayout6);
        filterByAreaLabel2.setText(" Max: ");
        filterByAreaLabel1.setText("Filter by area. Min: ");
        filterByWindowPanel.setLayout(gridBagLayout7);
        filterByWindowLabel.setText("Filter by window. Buffer: ");
        unionLabel1.setText("Union up to ");
        unionLabel2.setText(" adjacent Layer-A features");
        weightLabel.setText("Weight");
        centroidLabel.setText("Centroid Distance");
        hausdorffLabel.setText("Hausdorff Distance (Centroids Aligned)");
        symDiffLabel.setText("Symmetric Difference");
        symDiffCentroidsAlignedLabel.setText(
            "Symmetric Difference (Centroids Aligned)");
        compactnessLabel.setText("Compactness");
        anglePanel.setLayout(gridBagLayout8);
        angleLabel.setText("Angle Histogram. Bins: ");
        targetLabel.setText("Layer A: ");
        filterByAreaTextArea.setEnabled(false);
        filterByAreaTextArea.setBorder(null);
        filterByAreaTextArea.setOpaque(false);
        filterByAreaTextArea.setDisabledTextColor(Color.black);
        filterByAreaTextArea.setEditable(false);
        filterByAreaTextArea.setText(
            "Filtering will speed up the matching process. Filter By Window weeds " +
            "out matches between features whose envelopes do not overlap. Filter " +
            "By Area is used to weed out very small and very large features.");
        filterByAreaTextArea.setLineWrap(true);
        filterByAreaTextArea.setWrapStyleWord(true);
        unioningTextArea.setWrapStyleWord(true);
        unioningTextArea.setLineWrap(true);
        unioningTextArea.setText(
            "Better matches may be found by creating temporary unions of features " +
            "sharing a common edge.");
        unioningTextArea.setEditable(false);
        unioningTextArea.setDisabledTextColor(Color.black);
        unioningTextArea.setOpaque(false);
        unioningTextArea.setBorder(null);
        unioningTextArea.setEnabled(false);
        this.add(upperTabbedPane,
            new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(matchButton,
            new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(4, 4, 4, 4), 0, 0));
        this.add(layerPanel,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        layerPanel.add(candidateLayerComboBox,
            new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        layerPanel.add(targetLayerComboBox,
            new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        layerPanel.add(candidateLabel,
            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        layerPanel.add(targetLabel,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filteringTab.add(filterByAreaPanel,
            new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        upperTabbedPane.add(matchingTab, "Matching");
        upperTabbedPane.add(filteringTab, "Filtering");
        upperTabbedPane.add(unioningTab, "Unioning");
        matchingTab.add(weightLabel,
            new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByAreaPanel.add(filterByAreaMinField,
            new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByAreaPanel.add(filterByAreaMaxField,
            new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByAreaPanel.add(filterByAreaLabel2,
            new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByAreaPanel.add(filterByAreaLabel1,
            new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByAreaPanel.add(filterByAreaCheckBox,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filteringTab.add(filterByWindowPanel,
            new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByWindowPanel.add(filterByWindowCheckBox,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByWindowPanel.add(filterByWindowLabel,
            new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        filterByWindowPanel.add(filterByWindowField,
            new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        unioningTab.add(unionCheckBox,
            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        unioningTab.add(unionLabel1,
            new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        unioningTab.add(unionTextField,
            new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        unioningTab.add(unionLabel2,
            new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        unioningTab.add(unioningTextArea,
            new GridBagConstraints(0, 10, 10, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(10, 4, 4, 4), 0, 0));
        filteringTab.add(filterByAreaTextArea,
            new GridBagConstraints(1, 10, 2, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(10, 4, 4, 4), 0, 0));
        matchingTab.add(centroidCheckBox,
            new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(centroidDistanceWeightField,
            new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(centroidLabel,
            new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(hausdorffCheckBox,
            new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(hausdorffDistanceWeightField,
            new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(hausdorffLabel,
            new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(symDiffCheckBox,
            new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(symDiffWeightField,
            new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(symDiffLabel,
            new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(symDiffCentroidsAlignedCheckBox,
            new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(symDiffCentroidsAlignedWeightField,
            new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(symDiffCentroidsAlignedLabel,
            new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(compactnessCheckBox,
            new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(compactnessWeightField,
            new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(compactnessLabel,
            new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(angleCheckBox,
            new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(angleWeightField,
            new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(anglePanel,
            new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        anglePanel.add(angleLabel,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        anglePanel.add(angleBinField,
            new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        matchingTab.add(matchingFillerPanel,
            new GridBagConstraints(50, 50, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(lowerTabbedPane,
            new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        lowerTabbedPane.add(chartPanel, "Chart");
        lowerTabbedPane.add(statisticsPanel, "Statistics");
    }

    public StatisticsPanel getStatisticsPanel() {
        return statisticsPanel;
    }
}
