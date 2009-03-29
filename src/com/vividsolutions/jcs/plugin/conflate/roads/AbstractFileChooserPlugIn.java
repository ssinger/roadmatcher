package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public abstract class AbstractFileChooserPlugIn extends AbstractPlugIn {

    protected String getLastFilenameKey() {
        return getClass().getName() + " - LAST FILENAME";
    }

    public void setLastFile(File file, WorkbenchContext context) {
        ApplicationOptionsPlugIn.options(context).put(getLastFilenameKey(),
                file != null ? file.getPath() : null);
    }

    public File getLastFile(WorkbenchContext context) {
        String filename = (String) ApplicationOptionsPlugIn.options(context).get(
                getLastFilenameKey());
        return filename != null ? new File(filename) : null;
    }

    protected File getInitialFile(File currentDirectory, PlugInContext context) {
        return getLastFile(context.getWorkbenchContext());
    }

    public abstract String getExtension();

    protected abstract String getFileFilterDescription();

    protected String[] getMenuPath() {
        return new String[]{RoadMatcherToolboxPlugIn.MENU_NAME};
    }

    protected String getMenuItemName() {
        return getName() + "...";
    }

    protected MultiEnableCheck createEnableCheck(PlugInContext context) {
        return new MultiEnableCheck();
    }

    protected ConflationSession session(final PlugInContext context) {
        return ToolboxModel.instance(context).getSession();
    }
}
