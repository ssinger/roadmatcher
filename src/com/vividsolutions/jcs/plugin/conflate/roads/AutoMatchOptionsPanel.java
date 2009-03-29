package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import com.vividsolutions.jcs.conflate.roads.match.RoadMatchOptions;
import com.vividsolutions.jcs.jump.FUTURE_CardLayoutWrapper;
import com.vividsolutions.jcs.jump.FUTURE_ValidatingTextField;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
public class AutoMatchOptionsPanel extends JPanel implements OptionsPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JCheckBox standaloneCheckBox = new JCheckBox();
    private JLabel standaloneMinDistanceLabel = new JLabel();
    private RoadMatchOptions defaultRoadMatchOptions = new RoadMatchOptions();
    private Block optionsGetter = new Block() {
        public Object yield() {
            return toolboxModel().getSession().getMatchOptions();
        }
    };
    public RoadMatchOptions options() {
        return (RoadMatchOptions) optionsGetter.yield();
    }
    private ToolboxModel toolboxModel() {
        return ToolboxModel.instance(context.getLayerManager(), context);
    }
    private JTextField standaloneMinDistanceTextField = new ValidatingTextField(
            "",
            4,
            SwingConstants.RIGHT,
            new ValidatingTextField.CompositeValidator(
                    new ValidatingTextField.Validator[]{
                            ValidatingTextField.DOUBLE_VALIDATOR,
                            new ValidatingTextField.GreaterThanOrEqualValidator(
                                    0)}),
            new ValidatingTextField.CompositeCleaner(
                    new ValidatingTextField.Cleaner[]{new FUTURE_ValidatingTextField.BlankCleaner(
                            "") {
                        protected String getReplacement() {
                            return ""
                                    + defaultRoadMatchOptions
                                            .getStandaloneOptions()
                                            .getDistanceTolerance();
                        }
                    }}));
    private JCheckBox matchCheckBox = new JCheckBox();
    private JLabel matchMaxDistanceLabel = new JLabel();
    private JTextField matchMaxDistanceTextField = new ValidatingTextField(
            "",
            4,
            SwingConstants.RIGHT,
            new ValidatingTextField.CompositeValidator(
                    new ValidatingTextField.Validator[]{
                            ValidatingTextField.DOUBLE_VALIDATOR,
                            new ValidatingTextField.GreaterThanOrEqualValidator(
                                    0)}),
            new ValidatingTextField.CompositeCleaner(
                    new ValidatingTextField.Cleaner[]{new FUTURE_ValidatingTextField.BlankCleaner(
                            "") {
                        protected String getReplacement() {
                            return ""
                                    + defaultRoadMatchOptions
                                            .getEdgeMatchOptions()
                                            .getDistanceTolerance();
                        }
                    }}));
    private JLabel matchMinLineSegmentLengthLabel = new JLabel();
    private JLabel nearnessToleranceLabel = new JLabel();
    private JTextField matchMinLineSegmentLengthTextField = new ValidatingTextField(
            "",
            4,
            SwingConstants.RIGHT,
            new ValidatingTextField.CompositeValidator(
                    new ValidatingTextField.Validator[]{
                            ValidatingTextField.DOUBLE_VALIDATOR,
                            new ValidatingTextField.GreaterThanOrEqualValidator(
                                    0)}),
            new ValidatingTextField.CompositeCleaner(
                    new ValidatingTextField.Cleaner[]{new FUTURE_ValidatingTextField.BlankCleaner(
                            "") {
                        protected String getReplacement() {
                            return ""
                                    + defaultRoadMatchOptions
                                            .getEdgeMatchOptions()
                                            .getLineSegmentLengthTolerance();
                        }
                    }}));
    private JTextField nearnessToleranceTextField = new ValidatingTextField(
            "",
            4,
            SwingConstants.RIGHT,
            new ValidatingTextField.CompositeValidator(
                    new ValidatingTextField.Validator[]{
                            ValidatingTextField.DOUBLE_VALIDATOR,
                            new ValidatingTextField.GreaterThanOrEqualValidator(
                                    0)}),
            new ValidatingTextField.CompositeCleaner(
                    new ValidatingTextField.Cleaner[]{new FUTURE_ValidatingTextField.BlankCleaner(
                            "") {
                        protected String getReplacement() {
                            return ""
                                    + defaultRoadMatchOptions
                                            .getEdgeMatchOptions()
                                            .getNearnessTolerance();
                        }
                    }}));    
    private WorkbenchContext context;
    private JPanel fillerPanel = new JPanel();
    private JPanel fillerPanel2 = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    public AutoMatchOptionsPanel(WorkbenchContext context) {
        this.context = context;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    void jbInit() throws Exception {
        standaloneCheckBox
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        findStandaloneRoadsCheckBox_actionPerformed(e);
                    }
                });
        this.setLayout(gridBagLayout1);
        standaloneMinDistanceLabel.setText("Minimum distance");
        matchCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                matchRoadsCheckBox_actionPerformed(e);
            }
        });
        matchMaxDistanceLabel.setText("Maximum distance");
        matchMinLineSegmentLengthLabel.setText("Minimum Line Segment length ");
        nearnessToleranceLabel.setText("Nearness tolerance ");
        matchMinLineSegmentLengthTextField.setText("nnnnn");
        nearnessToleranceTextField.setText("nnnnn");
        matchMaxDistanceTextField.setText("nnnnn");
        standaloneMinDistanceTextField.setText("nnnnn");
        standaloneCheckBox.setText("Find Standalone Roads");
        matchCheckBox.setText("Find Matched Roads");
        fillerPanel2.setPreferredSize(new Dimension(12, 12));
        fillerPanel2.setLayout(gridBagLayout2);
        fillerPanel.setLayout(gridBagLayout3);
        this.add(standaloneCheckBox, new GridBagConstraints(1, 3, 2, 1, 0.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 0, 0, 0), 0, 0));
        this.add(matchCheckBox, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6,
                        0, 0, 0), 0, 0));
        this.add(standaloneMinDistanceLabel, new GridBagConstraints(1, 4, 1, 1,
                0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 30, 0, 0), 0, 0));
        this.add(standaloneMinDistanceTextField, new GridBagConstraints(2, 4,
                1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(matchMaxDistanceLabel, new GridBagConstraints(1, 6, 1, 1, 0.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 30, 0, 0), 0, 0));
        this.add(matchMinLineSegmentLengthTextField, new GridBagConstraints(2,
                1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(matchMinLineSegmentLengthLabel, new GridBagConstraints(1, 1,
                1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));        
        this.add(nearnessToleranceTextField, new GridBagConstraints(2,
                2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        this.add(nearnessToleranceLabel, new GridBagConstraints(1, 2,
                1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));        
        this.add(matchMaxDistanceTextField, new GridBagConstraints(2, 6, 1, 1,
                0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(fillerPanel, new GridBagConstraints(49, 50, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        0, 0, 0, 0), 0, 0));
        this.add(fillerPanel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
                        0, 0, 0, 0), 0, 0));
    }
    void findStandaloneRoadsCheckBox_actionPerformed(ActionEvent e) {
        updateComponents();
    }
    void matchRoadsCheckBox_actionPerformed(ActionEvent e) {
        updateComponents();
    }    
    private void updateComponents() {
        standaloneMinDistanceLabel.setEnabled(standaloneCheckBox.isSelected());
        standaloneMinDistanceTextField.setEnabled(standaloneCheckBox
                .isSelected());
        matchMaxDistanceLabel.setEnabled(matchCheckBox.isSelected());
        matchMaxDistanceTextField.setEnabled(matchCheckBox.isSelected());
    }
    public String validateInput() {
        return null;
    }
    public void okPressed() {
        options().setStandaloneEnabled(standaloneCheckBox.isSelected());
        options().getStandaloneOptions().setDistanceTolerance(
                Double.parseDouble(standaloneMinDistanceTextField.getText()));
        options().setEdgeMatchEnabled(matchCheckBox.isSelected());
        options().getEdgeMatchOptions().setDistanceTolerance(
                Double.parseDouble(matchMaxDistanceTextField.getText()));
        options().getEdgeMatchOptions().setLineSegmentLengthTolerance(
                Double
                        .parseDouble(matchMinLineSegmentLengthTextField
                                .getText()));
        options().getEdgeMatchOptions().setNearnessTolerance(
                Double
                        .parseDouble(nearnessToleranceTextField
                                .getText()));        
    }
    public void init() {
        standaloneCheckBox.setSelected(options().isStandaloneEnabled());
        matchCheckBox.setSelected(options().isEdgeMatchEnabled());
        standaloneMinDistanceTextField.setText(""
                + options().getStandaloneOptions().getDistanceTolerance());
        matchMaxDistanceTextField.setText(""
                + options().getEdgeMatchOptions().getDistanceTolerance());
        matchMinLineSegmentLengthTextField.setText(""
                + options().getEdgeMatchOptions()
                        .getLineSegmentLengthTolerance());
        nearnessToleranceTextField.setText(""
                + options().getEdgeMatchOptions()
                        .getNearnessTolerance());
        updateComponents();
    }
    public static class BorderLayoutOptionsPanelWrapper extends
            OptionsPanelWrapper {
        private OptionsPanel optionsPanel;
        public BorderLayoutOptionsPanelWrapper(OptionsPanel optionsPanel) {
            setLayout(new BorderLayout());
            add((Component) optionsPanel, BorderLayout.CENTER);
            this.optionsPanel = optionsPanel;
        }
        protected OptionsPanel optionsPanel() {
            return optionsPanel;
        }
    }
    public void setOptionsGetter(Block optionsGetter) {
        this.optionsGetter = optionsGetter;
    }
    private static abstract class OptionsPanelWrapper extends JPanel implements
            OptionsPanel {
        public String validateInput() {
            return optionsPanel().validateInput();
        }
        protected abstract OptionsPanel optionsPanel();
        public void okPressed() {
            optionsPanel().okPressed();
        }
        public void init() {
            optionsPanel().init();
        }
    }
    public static class SessionCheckingWrapper extends OptionsPanelWrapper {
        private boolean checkingSessionExists = true;
        private static class DummyOptionsPanel extends JPanel implements
                OptionsPanel {
            public DummyOptionsPanel() {
                setLayout(new GridBagLayout());
                add(new JLabel("No session is currently active"));
            }
            public String validateInput() {
                return null;
            }
            public void okPressed() {
            }
            public void init() {
            }
        }
        private WorkbenchContext context;
        private DummyOptionsPanel dummyOptionsPanel = new DummyOptionsPanel();
        private FUTURE_CardLayoutWrapper cardLayoutWrapper;
        private OptionsPanel optionsPanel;
        public SessionCheckingWrapper(OptionsPanel optionsPanel,
                WorkbenchContext context) {
            this.context = context;
            cardLayoutWrapper = new FUTURE_CardLayoutWrapper(this);
            this.optionsPanel = optionsPanel;
            cardLayoutWrapper.add((Component) optionsPanel);
            cardLayoutWrapper.add(dummyOptionsPanel);
        }
        protected OptionsPanel optionsPanel() {
            return (OptionsPanel) cardLayoutWrapper.getTopComponent();
        }
        public void init() {
            cardLayoutWrapper.setTopComponent(!checkingSessionExists
                    || (context.getLayerManager() != null && ToolboxModel
                            .instance(context.getLayerManager(), context)
                            .isInitialized()) ? (Component) optionsPanel
                    : dummyOptionsPanel);
            super.init();
        }
        public void setCheckingSessionExists(boolean checkingSessionExists) {
            this.checkingSessionExists = checkingSessionExists;
        }
    }
}