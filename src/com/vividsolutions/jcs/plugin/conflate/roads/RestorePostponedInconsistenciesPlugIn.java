package com.vividsolutions.jcs.plugin.conflate.roads;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;

import com.vividsolutions.jcs.conflate.roads.model.NeighbourhoodList;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class RestorePostponedInconsistenciesPlugIn extends AbstractPlugIn {
	public boolean execute(final PlugInContext context) throws Exception {
		final Collection originalPostponedInconsistentNeighbourhoods = new ArrayList(
				postponedInconsistentNeighbourhoods(
						context.getWorkbenchContext()).toCollection());
		execute(new UndoableCommand(getName()) {
			public void execute() {
				postponedInconsistentNeighbourhoods(
						context.getWorkbenchContext()).clear();
				ToolboxModel.instance(context).getSourceLayer(0)
						.fireAppearanceChanged();
				ToolboxModel.instance(context).getSourceLayer(1)
						.fireAppearanceChanged();
			}
			public void unexecute() {
				postponedInconsistentNeighbourhoods(
						context.getWorkbenchContext()).addAll(
						originalPostponedInconsistentNeighbourhoods);
				ToolboxModel.instance(context).getSourceLayer(0)
						.fireAppearanceChanged();
				ToolboxModel.instance(context).getSourceLayer(1)
						.fireAppearanceChanged();
			}
		}, context);
		return true;
	}

	private NeighbourhoodList postponedInconsistentNeighbourhoods(
			WorkbenchContext context) {
		return NeighbourhoodList
				.postponedInconsistentNeighbourhoods(ToolboxModel.instance(
						context).getSession());
	}
	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.TOOLS_MENU_NAME},
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
								.add(new EnableCheck() {
									public String check(JComponent component) {
										return postponedInconsistentNeighbourhoods(
												context.getWorkbenchContext())
												.toCollection().isEmpty()
												? ErrorMessages.restorePostponedInconsistenciesPlugIn_noInconsistencies
												: null;
									}
								}));
	}
}