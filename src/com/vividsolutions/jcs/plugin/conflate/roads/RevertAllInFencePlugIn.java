package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class RevertAllInFencePlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		return new RevertAllOp().execute(
				context.getLayerViewPanel().getFence(), context
						.getWorkbenchContext());
	}

	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.TOOLS_MENU_NAME },
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
														.getWorkbenchContext()))
								.add(
										new EnableCheckFactory(context
												.getWorkbenchContext())
												.createFenceMustBeDrawnCheck()));
	}

}