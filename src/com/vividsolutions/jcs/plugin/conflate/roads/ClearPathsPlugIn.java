package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
public class ClearPathsPlugIn extends AbstractPlugIn {
	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		PathUtilities.changePathUndoablyTo(new List[]{new ArrayList(),
				new ArrayList()}, getName(), context.getLayerViewPanel());
		return true;
	}
	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension.addMainMenuItemWithJava14Fix(context, this,
				new String[]{RoadMatcherToolboxPlugIn.MENU_NAME}, getName(),
				false, null, createEnableCheck(context));
	}
	public static MultiEnableCheck createEnableCheck(final PlugInContext context) {
		return new MultiEnableCheck()
				.add(
						context
								.getCheckFactory()
								.createWindowWithLayerViewPanelMustBeActiveCheck())
				.add(
						SpecifyRoadFeaturesTool
								.createConflationSessionMustBeStartedCheck(context
										.getWorkbenchContext()));
	}
}