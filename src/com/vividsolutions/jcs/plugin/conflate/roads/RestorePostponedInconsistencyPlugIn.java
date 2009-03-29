package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

public class RestorePostponedInconsistencyPlugIn extends AbstractPlugIn {
	public boolean execute(PlugInContext context) throws Exception {
		ToolboxModel.instance(context).getConsistencyConfiguration()
				.setPostponedForInconsistenciesAt(
						context.getLayerViewPanel().getLastClickedPoint(),
						false, context);
		return true;
	}

	public void initialize(PlugInContext context) throws Exception {
		context
				.getFeatureInstaller()
				.addPopupMenuItem(
						LayerViewPanel.popupMenu(),
						this,
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