package com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.toolbox.MainButtonPlugIn;


public abstract class AbstractFindPlugIn extends MainButtonPlugIn {
    public AbstractFindPlugIn(String taskMonitorTitle, ToolboxPanel toolboxPanel) {
        super(taskMonitorTitle, toolboxPanel);
        taskWindowMustBeActiveCheck = new EnableCheckFactory(toolboxPanel.getContext().getWorkbench()
                                                                    .getContext()).createTaskWindowMustBeActiveCheck();
        this.toolboxPanel = toolboxPanel;
    }
    protected ToolboxPanel getToolboxPanel() {
        return toolboxPanel;
    }    
    private ToolboxPanel toolboxPanel;
    public String validateInput() {
        if (inputLayer() == null) {
            return "Input layer is not specified";
        }

        if (null != taskWindowMustBeActiveCheck.check(null)) {
            //AttributeTabs require a TaskFrame to be active [Jon Aquino]
            return taskWindowMustBeActiveCheck.check(null);
        }

        return null;
    }
    protected Layer inputLayer() {
        return getToolboxPanel().getInputLayerComboBox().getSelectedLayer();
    }    
    private EnableCheck taskWindowMustBeActiveCheck;
    protected void initLogPanel() {
        getToolboxPanel().getLogPanel().createNewDocument();
        getToolboxPanel().getLogPanel().addField("Date: ",
            new SimpleDateFormat("hh:mm:ss a yyyy-MM-dd").format(new Date()));
        getToolboxPanel().getLogPanel().addField("Layer: ", inputLayer().getName());
    }
    protected String parameterDescription() {
        return "(Distance Tol = " +
        getToolboxPanel().getGapToleranceTextField().getText() + ")";
    }    
}
