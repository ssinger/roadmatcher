package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.Iterator;
import javax.swing.JComponent;

import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
public class MatchPathsPlugIn extends AbstractPlugIn {
	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		if (!PathUtilities.checkPathRoadSegmentsInNetwork(context
				.getLayerViewPanel(), context.getWorkbenchContext())) {
			return false;
		}
		MatchPathsOperation.MyUndoableCommand command = new MatchPathsOperation()
				.createUndoableCommand(
						PathUtilities.pathMatch(context.getWorkbenchContext()), context
								.getLayerViewPanel().getContext(), ToolboxModel
								.instance(context.getLayerManager(), context
										.getWorkbenchContext()));
		execute(command, context);
		if (command.isExecutionSuccessful()) {
			new AutoAdjustAfterManualCommitOp().autoAdjust(command, context
					.getWorkbenchContext());
			new ClearPathsPlugIn().execute(context);
		}
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
										.getWorkbenchContext())).add(
						new EnableCheck() {
							public String check(JComponent component) {
								//Must use WorkbenchContext rather
								// than PlugInContext
								//because at this point in time,
								// PlugInContext#getLayerViewPanel
								//returns null. [Jon Aquino
								// 2004-03-02]
								return PathUtilities.path(
										0,
										context.getWorkbenchContext()
												.getLayerViewPanel()).isEmpty()
										|| PathUtilities.path(
												1,
												context.getWorkbenchContext()
														.getLayerViewPanel())
												.isEmpty()
										? FUTURE_StringUtil
												.substitute(
														ErrorMessages.matchPathsPlugIn_noPathsDefined,
														new String[]{new DefinePathsTool(
																context
																		.getWorkbenchContext())
																.getName()})
										: !new MatchPathsOperation()
												.allRoadSegmentsUnknown(PathUtilities.pathMatch(context
														.getWorkbenchContext()))
												? ErrorMessages.matchPathsPlugIn_nonUnknownRoadSegments
												: null;
							}
						});
	}
}