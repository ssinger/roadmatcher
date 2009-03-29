package com.vividsolutions.jcs.plugin.conflate.roads;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
public class AutoMatchToolboxTab extends JPanel implements OptionsPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JPanel centerPanel = new JPanel();
    private GridLayout gridLayout1 = new GridLayout();
    private JPanel optionsButtonPanel = new JPanel();
    private JPanel autoMatchButtonPanel = new JPanel();
    private JPanel autoAdjustButtonPanel = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private GridBagLayout gridBagLayout4 = new GridBagLayout();
    private JButton optionsButton = new JButton();
    private JButton autoMatchButton = new JButton();
    private JButton autoAdjustButton = new JButton();
    private WorkbenchContext context;
    public static class InternalFrameListeningWrapper extends
            AutoMatchOptionsPanel.BorderLayoutOptionsPanelWrapper {
        public InternalFrameListeningWrapper(OptionsPanel optionsPanel,
                final WorkbenchContext context) {
            super(optionsPanel);
            GUIUtil.addInternalFrameListener(context.getWorkbench().getFrame()
                    .getDesktopPane(), GUIUtil
                    .toInternalFrameListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            InternalFrameListeningWrapper.this.init();
                        }
                    }));
        }
    }
    public static JPanel create(WorkbenchContext context) {
        //The only reason we're implementing OptionsPanel is so that we can
        //use SessionCheckingWrapper [Jon Aquino 2004-03-11]
        return new InternalFrameListeningWrapper(
                new AutoMatchOptionsPanel.SessionCheckingWrapper(
                        new AutoMatchToolboxTab(context), context), context);
    }
    private AutoMatchToolboxTab(WorkbenchContext context) {
        this.context = context;
        autoMatchButton.setIcon(IconLoader.icon("GoalFlag.gif"));
        autoAdjustButton.setIcon(IconLoader.icon("RedFlag.gif"));
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        centerPanel.setLayout(gridLayout1);
        optionsButtonPanel.setLayout(gridBagLayout2);
        autoMatchButtonPanel.setLayout(gridBagLayout3);
        autoAdjustButtonPanel.setLayout(gridBagLayout4);
        gridLayout1.setColumns(1);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(10);
        optionsButton.setText("Options...");
        optionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                optionsButton_actionPerformed(e);
            }
        });
        autoMatchButton.setText("AutoMatch");
        autoAdjustButton.setText("AutoAdjust");
        autoMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autoMatchButton_actionPerformed(e);
            }
        });
        autoAdjustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autoAdjustButton_actionPerformed(e);
            }
        });
        this.add(centerPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 0, 10), 0, 0));
        centerPanel.add(optionsButtonPanel, null);
        centerPanel.add(autoMatchButtonPanel, null);
        centerPanel.add(autoAdjustButtonPanel, null);
        optionsButtonPanel.add(optionsButton, new GridBagConstraints(0, 0, 1,
                1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        autoMatchButtonPanel.add(autoMatchButton, new GridBagConstraints(0, 0,
                1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        autoAdjustButtonPanel.add(autoAdjustButton, new GridBagConstraints(0,
                0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }
    void optionsButton_actionPerformed(ActionEvent e) {
        promptForAutoMatchOptions(false, null, context);
    }
    private void promptForAutoMatchOptions(boolean checkingSessionExists,
            Block optionsGetter, WorkbenchContext context) {
        ConflationOptionsPlugIn optionsPlugIn = new ConflationOptionsPlugIn();
        ((JTabbedPane) GUIUtil.getDescendantOfClass(JTabbedPane.class,
                optionsPlugIn.getDialog(context)))
                .setSelectedComponent(sessionCheckingWrapper(optionsPlugIn,
                        context));
        sessionCheckingWrapper(optionsPlugIn, context)
                .setCheckingSessionExists(checkingSessionExists);
        if (optionsGetter != null) {
            ((AutoMatchOptionsPanel) GUIUtil.getDescendantOfClass(
                    AutoMatchOptionsPanel.class, optionsPlugIn
                            .getDialog(context)))
                    .setOptionsGetter(optionsGetter);
        }
        AbstractPlugIn.toActionListener(optionsPlugIn, context, null)
                .actionPerformed(null);
    }
    void autoMatchButton_actionPerformed(ActionEvent e) {
        AbstractPlugIn.toActionListener(new AutoMatchPlugIn(), context,
                new TaskMonitorManager()).actionPerformed(e);
    }
    void autoAdjustButton_actionPerformed(ActionEvent e) {
        AbstractPlugIn.toActionListener(new AutoAdjustPlugIn(), context,
                new TaskMonitorManager()).actionPerformed(e);
    }
    public String validateInput() {
        throw new UnsupportedOperationException();
    }
    public void okPressed() {
        throw new UnsupportedOperationException();
    }
    public void init() {
    }
    private AutoMatchOptionsPanel.SessionCheckingWrapper sessionCheckingWrapper(
            ConflationOptionsPlugIn optionsPlugIn, WorkbenchContext context) {
        return ((AutoMatchOptionsPanel.SessionCheckingWrapper) GUIUtil
                .getDescendantOfClass(
                        AutoMatchOptionsPanel.SessionCheckingWrapper.class,
                        optionsPlugIn.getDialog(context)));
    }
}