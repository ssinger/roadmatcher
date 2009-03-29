package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class FindSegmentsPlugIn extends AbstractPlugIn {
	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.TOOLS_MENU_NAME },
						getName() + "...",
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

	public boolean execute(PlugInContext context) throws Exception {
		// Ideally we only call #reportNothingToUndoYet for undoable operations
		// or operations that do not affect the model. But this operation
		// affects the model in a way that has little impact on the undo
		// history: adding a layer. [Jon Aquino 2004-09-24]
		reportNothingToUndoYet(context);
		MultiInputDialog dialog = new MultiInputDialog(context
				.getWorkbenchFrame(), getName(), true);
		final String FIND_OVERSHOOT_SEGMENTS = "Find Overshoot Segments";
		dialog.addCheckBox(FIND_OVERSHOOT_SEGMENTS, true);
		dialog.setVisible(true);
		if (!dialog.wasOKPressed()) {
			return false;
		}
		if (dialog.getBoolean(FIND_OVERSHOOT_SEGMENTS)) {
			addLayers("Overshoots", context, new Block() {
				public Object yield(Object network) {
					return new FindOvershootSegmentsOp()
							.findOvershoots((RoadNetwork) network);
				}
			});
		}
		return true;
	}

	private void addLayers(String prefix, PlugInContext context, Block block) {
		ToolboxModel.instance(context).createQALayers(
				new FeatureCollection[] {
						(FeatureCollection) block.yield(ToolboxModel.instance(
								context).getSession().getSourceNetwork(0)),
						(FeatureCollection) block.yield(ToolboxModel.instance(
								context).getSession().getSourceNetwork(1)) },
				prefix + "-", prefix + " for ");
	}
}