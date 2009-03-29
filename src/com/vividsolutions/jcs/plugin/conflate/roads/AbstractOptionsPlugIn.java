package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
public abstract class AbstractOptionsPlugIn extends AbstractPlugIn {
    private OptionsDialog dialog = null;
    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);
        GUIUtil.centreOnWindow(getDialog(context.getWorkbenchContext()));
        getDialog(context.getWorkbenchContext()).setVisible(true);
        //Update default-reference display [Jon Aquino 2004-04-28]
        RoadMatcherToolboxPlugIn.instance(context.getWorkbenchContext())
                .getToolboxPanel().updateComponents();
        return getDialog(context.getWorkbenchContext()).wasOKPressed();
    }
    public OptionsDialog getDialog(WorkbenchContext context) {
        if (dialog == null) {
            dialog = createDialog(context);
        }
        return dialog;
    }
    protected abstract OptionsDialog createDialog(WorkbenchContext context);
}
