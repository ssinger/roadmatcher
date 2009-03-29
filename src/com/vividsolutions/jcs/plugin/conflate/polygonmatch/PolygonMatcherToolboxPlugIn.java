package com.vividsolutions.jcs.plugin.conflate.polygonmatch;

import com.vividsolutions.jcs.jump.FUTURE_ToolboxPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;

import java.awt.BorderLayout;


public class PolygonMatcherToolboxPlugIn extends FUTURE_ToolboxPlugIn {
    protected void initializeToolbox(ToolboxDialog toolbox) {
        toolbox.getCenterPanel().add(new ToolboxPanel(toolbox.getContext()),
            BorderLayout.CENTER);
        toolbox.setInitialLocation(new GUIUtil.Location(20, true, 20, false));
    }

    public void initialize(PlugInContext context) throws Exception {
        createMainMenuItem(new String[] { "Conflate" }, null,
            context.getWorkbenchContext());
    }
}
