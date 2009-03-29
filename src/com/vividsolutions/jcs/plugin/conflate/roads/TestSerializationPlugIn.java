package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.jump.FUTURE_SerializabilityChecker;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class TestSerializationPlugIn extends AbstractPlugIn {
    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);
        context.getOutputFrame().createNewDocument();
        context.getOutputFrame()
                .append(
                        new FUTURE_SerializabilityChecker().reportHTML(
                                ToolboxModel.instance(
                                        context)
                                        .getSession(), -1, 50));
        context.getOutputFrame().surface();
        return true;
    }
    public void initialize(PlugInContext context) throws Exception {
        context
        .getFeatureInstaller()
        .addMainMenuItem(
                this,
                new String[]{RoadMatcherToolboxPlugIn.MENU_NAME, "Test"},
                getName(),
                false,
                null,
                new MultiEnableCheck()
                .add(
                        context
                        .getCheckFactory()
                        .createWindowWithLayerViewPanelMustBeActiveCheck())
                .add(
                        SpecifyRoadFeaturesTool
                        .createConflationSessionMustBeStartedCheck(context
                                .getWorkbenchContext())));
    }    
}
