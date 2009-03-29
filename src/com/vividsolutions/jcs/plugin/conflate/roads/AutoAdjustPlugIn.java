package com.vividsolutions.jcs.plugin.conflate.roads;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.RoadNetwork;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;

public class AutoAdjustPlugIn extends ThreadedBasePlugIn {
	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME },
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

	public String getName() {
		//Ensure no space [Jon Aquino 2004-03-03]
		return "AutoAdjust";
	}

	public boolean execute(PlugInContext context) throws Exception {
		//Note: similar code in AutoMatchPlugIn [Jon Aquino 2004-05-10]
		return JOptionPane.OK_OPTION == JOptionPane
				.showOptionDialog(
						context.getWorkbenchFrame(),
						"AutoAdjustment will be attempted for the following number of Inconsistent segments:\n\n"
								+ StringUtil.repeat(' ', 16)
								+ networkToAutoAdjust(context).getName()
								+ ": "
								+ session(context).getStatistics().get(
										networkToAutoAdjust(context).getID())
										.getResultStatistics()
										.getInconsistentCount()
								+ " segments\n\n", "AutoAdjustment",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, new Object[] {
								"Continue", "Cancel" }, "Continue");
	}

	private RoadNetwork networkToAutoAdjust(PlugInContext context) {
		return session(context).getSourceNetwork(
				AutoAdjustOptions.get(session(context)).getDatasetName());
	}

	public void run(final TaskMonitor monitor, final PlugInContext context)
			throws Exception {
		// Run outside #invokeLater, otherwise get assertion failure
		// because the UndoableEditReceiver will not be receiving.
		// [Jon Aquino 2004-08-10]
		final String report = runProper(monitor, context);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				JOptionPane
						.showMessageDialog(
								context.getWorkbenchFrame(),
								report,
								"AutoAdjust Statistics",
								JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	public String runProper(TaskMonitor monitor, final PlugInContext context) {
		reportNothingToUndoYet(context);
		final int scannedRoadSegments = session(context).getStatistics().get(
				networkToAutoAdjust(context).getID()).getResultStatistics()
				.getInconsistentCount();
		final int adjustedRoadSegments[] = { 0 };
		if (new AutoAdjustOp().checkAutoAdjustNotConstrained(context
				.getWorkbenchContext())) {
			adjustedRoadSegments[0] = new AutoAdjustOp().autoAdjust(getName(),
					session(context).getRoadSegments(), monitor, context
							.getWorkbenchContext(), ToolboxModel.instance(
							context).getConsistencyConfiguration()
							.getAutoAdjuster());
		}
		final String report = "AutoAdjust processed the following "
			+ networkToAutoAdjust(context)
					.getName()
			+ " segments:\n\n"
			+ StringUtil.repeat(' ', 16)
			+ "Scanned: "
			+ scannedRoadSegments
			+ " segments\n"
			+ StringUtil.repeat(' ', 16)
			+ "Skipped: "
			+ (scannedRoadSegments - adjustedRoadSegments[0])
			+ " segments\n"
			+ StringUtil.repeat(' ', 16)
			+ "Adjusted: "
			+ adjustedRoadSegments[0]
			+ " segments\n\n";
		return report;
	}

	private ConflationSession session(PlugInContext context) {
		return ToolboxModel.instance(context).getSession();
	}
}