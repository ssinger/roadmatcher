package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jcs.jump.FUTURE_OptionsDialog;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;

public class ResultOptionsPlugIn extends AbstractPlugIn {
	public static final String RESULT_MENU_NAME = "Result";

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		GUIUtil.centreOnWindow(getDialog(context.getWorkbenchContext()));
		getDialog(context.getWorkbenchContext()).setVisible(true);
		return getDialog(context.getWorkbenchContext()).wasOKPressed();
	}

	public OptionsDialog getDialog(WorkbenchContext context) {
		if (dialog == null) {
			dialog = FUTURE_OptionsDialog.construct(context.getWorkbench()
					.getFrame(), getName(), true);
			dialog.setTitle(getName());
			dialog.addTab("Source Attributes",
					new SourceAttributesOptionsPanel(context));
			dialog.addTab("Vertex Transfer", new VertexTransferOptionsPanel(
					context));
		}
		return dialog;
	}

	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.RESULT_MENU_NAME },
						"Options...",
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

	private OptionsDialog dialog = null;
}