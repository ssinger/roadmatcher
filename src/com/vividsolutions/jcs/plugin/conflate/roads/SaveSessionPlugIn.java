package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class SaveSessionPlugIn extends AbstractSaveSessionPlugIn {

    public SaveSessionPlugIn(SaveSessionAsPlugIn saveSessionAsPlugIn) {
        this.saveSessionAsPlugIn = saveSessionAsPlugIn;
    }

    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        return session(context).getFile() == null ? saveSessionAsPlugIn
                .execute(context) : true;
    }

    protected String getMenuText() {
        return getName();
    }

    private SaveSessionAsPlugIn saveSessionAsPlugIn;
}
