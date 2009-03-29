package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency.AdjustedMatchConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.adjustedmatchconsistency.AdjustedMatchWithStandaloneEliminationConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.resultconsistency.ResultConsistencyConfiguration;
import com.vividsolutions.jcs.plugin.conflate.roads.sourcematchconsistency.SourceMatchConsistencyConfiguration;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
public class ConsistencyRuleOptionsPanel extends JPanel implements OptionsPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel label = new JLabel();
    private JRadioButton sourceMatchConsistencyRadioButton = new JRadioButton();
    private JRadioButton adjustedMatchConsistencyRadioButton = new JRadioButton();
    private JRadioButton adjustedMatchWithStandaloneEliminationConsistencyRadioButton = new JRadioButton();
    private JRadioButton resultConsistencyRadioButton = new JRadioButton();
    private WorkbenchContext context;
    private ButtonGroup buttonGroup = new ButtonGroup();
    public ConsistencyRuleOptionsPanel(WorkbenchContext context) {
        this.context = context;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        buttonGroup.add(sourceMatchConsistencyRadioButton);
        buttonGroup.add(adjustedMatchConsistencyRadioButton);
        buttonGroup.add(adjustedMatchWithStandaloneEliminationConsistencyRadioButton);
        buttonGroup.add(resultConsistencyRadioButton);
        consistencyConfigurationClassToButtonMap = CollectionUtil
                .createMap(new Object[]{
                        SourceMatchConsistencyConfiguration.class,
                        sourceMatchConsistencyRadioButton,
                        AdjustedMatchConsistencyConfiguration.class,
                        adjustedMatchConsistencyRadioButton,
                        AdjustedMatchWithStandaloneEliminationConsistencyConfiguration.class,
                        adjustedMatchWithStandaloneEliminationConsistencyRadioButton,
                        ResultConsistencyConfiguration.class,
                        resultConsistencyRadioButton});
    }
    void jbInit() throws Exception {
        label.setText("Rule used to check topological consistency:");
        this.setLayout(gridBagLayout1);
        sourceMatchConsistencyRadioButton.setText("Source Match Consistency");
        adjustedMatchConsistencyRadioButton
                .setText("Adjusted Match Consistency");
        adjustedMatchWithStandaloneEliminationConsistencyRadioButton
        .setText("Adjusted Match Consistency With Standalone Elimination");
        resultConsistencyRadioButton.setText("Result Consistency");
        fillerPanel2.setLayout(gridBagLayout2);
        warningLabel
                .setText("<HTML>Warning: Changing the rule will cause a recomputation<BR>"
                        + "of the result states, which may take a few minutes</HTML>");
        warningLabel.setIcon(GUIUtil.toSmallIcon(SpecifyRoadFeaturesTool
                .createIcon("Caution.gif")));
        panelLimitingWarningLabelWidth.setPreferredSize(new Dimension(300, 1));
        this.add(label, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0, 0, 0), 0, 0));
        this.add(sourceMatchConsistencyRadioButton, new GridBagConstraints(1,
                2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(adjustedMatchConsistencyRadioButton, new GridBagConstraints(1,
                3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(adjustedMatchWithStandaloneEliminationConsistencyRadioButton, new GridBagConstraints(1,
                4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(resultConsistencyRadioButton, new GridBagConstraints(1, 5, 1,
                1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(fillerPanel, new GridBagConstraints(2, 8, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        0, 0, 0, 0), 0, 0));
        this.add(fillerPanel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
                        12, 12, 0, 0), 0, 0));
        this.add(warningLabel, new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(50, 0, 0, 0), 0, 0));
        this.add(panelLimitingWarningLabelWidth, new GridBagConstraints(1, 6,
                2, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    public String validateInput() {
        return null;
    }
    public void okPressed() {
        if (FUTURE_GUIUtil.selectedButton(buttonGroup) == consistencyConfigurationClassToButtonMap
                .get((Class) ApplicationOptionsPlugIn.options(context).get(
                        ConsistencyConfiguration.CURRENT_CLASS_KEY,
                        ConsistencyConfiguration.DEFAULT_CLASS))) {
            return;
        }
        createActionListener(
                (Class) CollectionUtil.inverse(
                        consistencyConfigurationClassToButtonMap).get(
                        FUTURE_GUIUtil.selectedButton(buttonGroup)))
                .actionPerformed(null);
    }
    private Map consistencyConfigurationClassToButtonMap;
    private JPanel fillerPanel = new JPanel();
    private JPanel fillerPanel2 = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JLabel warningLabel = new JLabel();
    private JPanel panelLimitingWarningLabelWidth = new JPanel();
    public void init() {
        ((JRadioButton) consistencyConfigurationClassToButtonMap
                .get((Class) ApplicationOptionsPlugIn.options(context).get(
                        ConsistencyConfiguration.CURRENT_CLASS_KEY,
                        ConsistencyConfiguration.DEFAULT_CLASS)))
                .setSelected(true);
    }
    private ActionListener createActionListener(
            final Class newConsistencyConfigurationClass) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Class oldConsistencyConfigurationClass = (Class) ApplicationOptionsPlugIn
                        .options(context).get(
                                ConsistencyConfiguration.CURRENT_CLASS_KEY,
                                ConsistencyConfiguration.DEFAULT_CLASS);
                AbstractPlugIn.toActionListener(new AbstractPlugIn() {
                    public boolean execute(final PlugInContext context)
                            throws Exception {
                        execute(new UndoableCommand("Change Consistency Rule") {
                            public void execute() {
                                change(newConsistencyConfigurationClass,
                                        context);
                            }
                            public void unexecute() {
                                change(oldConsistencyConfigurationClass,
                                        context);
                            }
                        }, context);
                        return true;
                    }
                }, context, null).actionPerformed(null);
            }
            private void change(final Class consistencyConfigurationClass,
                    PlugInContext plugInContext) {
                new TaskMonitorManager().execute(new ThreadedBasePlugIn() {
                    public String getName() {
                        return "Change Consistency Rule";
                    }
                    public void run(TaskMonitor monitor, PlugInContext context)
                            throws Exception {
                        change(consistencyConfigurationClass, monitor);
                    }
                }, plugInContext);
            }
            private void change(Class consistencyConfigurationClass,
                    TaskMonitor monitor) {
                ApplicationOptionsPlugIn.options(context).put(
                        ConsistencyConfiguration.CURRENT_CLASS_KEY,
                        consistencyConfigurationClass);
                //Push consistency rule into each conflation session, rather
                //than have conflation session pull rule from higher up, so
                //that we need not serialize any higher than the conflation
                //session. [Jon Aquino 1/5/2004]
                JInternalFrame[] internalFrames = context.getWorkbench()
                        .getFrame().getInternalFrames();
                for (int i = 0; i < internalFrames.length; i++) {
                    if (!(internalFrames[i] instanceof LayerManagerProxy)) {
                        continue;
                    }
                    LayerManager layerManager = ((LayerManagerProxy) internalFrames[i])
                            .getLayerManager();
                    if (ToolboxModel.instance(layerManager, context)
                            .isInitialized()) {
                        try {
                            ToolboxModel
                                    .instance(layerManager, context)
                                    .getSession()
                                    .setConsistencyRule(
                                            ((ConsistencyConfiguration) consistencyConfigurationClass
                                                    .newInstance()).getRule());
                        } catch (InstantiationException e) {
                            Assert.shouldNeverReachHere();
                        } catch (IllegalAccessException e) {
                            Assert.shouldNeverReachHere();
                        }
                        ToolboxModel.instance(layerManager, context)
                                .updateResultStates(monitor);
                    }
                }
            }
        };
    }
}