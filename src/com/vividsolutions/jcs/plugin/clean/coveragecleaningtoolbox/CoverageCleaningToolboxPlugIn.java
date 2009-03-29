package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.vividsolutions.jcs.jump.FUTURE_ToolboxPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxStateManager;

public class CoverageCleaningToolboxPlugIn extends FUTURE_ToolboxPlugIn {
    protected void initializeToolbox(ToolboxDialog toolbox) {
        ToolboxPanel toolboxPanel = new ToolboxPanel(toolbox.getContext());
        toolbox.add(new UpdatingSnapVerticesTool(toolbox.getContext(),
                toolboxPanel));
        moveToolbarSouth(toolbox);
        toolbox.getCenterPanel().add(toolboxPanel, BorderLayout.CENTER);
        toolbox.setInitialLocation(new GUIUtil.Location(20, true, 20, false));
        new ToolboxStateManager(toolbox, Collections.singletonMap(Tab.class,
                Tab.STRATEGY));
    }

    private void moveToolbarSouth(ToolboxDialog toolbox) {
        JToolBar toolbar = toolbox.getToolBar();
        toolbar.getParent().remove(toolbar);

        JPanel southPanel = new JPanel(new GridBagLayout());
        southPanel.add(toolbar, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2,
                        2, 2, 2), 0, 0));
        southPanel.add(new JLabel(
                "Snap Vertices tool, for fixing gaps manually"),
                new GridBagConstraints(1, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        southPanel.add(new JPanel(), new GridBagConstraints(2, 0, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        toolbox.getCenterPanel().add(southPanel, BorderLayout.SOUTH);
    }

    public void initialize(PlugInContext context) throws Exception {
        createMainMenuItem(new String[]{"Clean"}, null, context
                .getWorkbenchContext());
    }
}
