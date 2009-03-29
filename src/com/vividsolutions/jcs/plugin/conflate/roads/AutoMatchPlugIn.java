package com.vividsolutions.jcs.plugin.conflate.roads;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
public class AutoMatchPlugIn extends ThreadedBasePlugIn {
	public AutoMatchPlugIn() {
		//      taskWindowMustBeActiveCheck = new
		// EnableCheckFactory(toolboxPanel.getContext().getWorkbench()
		//                                                                  .getContext()).createTaskWindowMustBeActiveCheck();
		this.taskMonitorTitle = "AutoMatch";
	}
	public boolean execute(PlugInContext context) throws Exception {
		//Note: similar code in AutoAdjustPlugIn [Jon Aquino 2004-05-10]
		return JOptionPane.OK_OPTION == JOptionPane
				.showOptionDialog(
						context.getWorkbenchFrame(),
						summary("will be", context),
						"AutoMatching", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, new Object[]{
								"Continue", "Cancel"}, "Continue");
	}
	public static String summary(String tense, PlugInContext context) {
		return "AutoMatching "+tense+" performed for the following number of Unknown segments:\n\n"
				+ StringUtil.repeat(' ', 16)
				+ ToolboxModel.instance(context).getSession()
						.getSourceNetwork(0).getName()
				+ ": "
				+ ToolboxModel.instance(context).getSession()
						.getStatistics().get(0)
						.getUnknownCount()
				+ " segments\n"
				+ StringUtil.repeat(' ', 16)
				+ ToolboxModel.instance(context).getSession()
						.getSourceNetwork(1).getName()
				+ ": "
				+ ToolboxModel.instance(context).getSession()
						.getStatistics().get(1)
						.getUnknownCount() + " segments\n\n";
	}
	public String getName() {
		//Ensure no space [Jon Aquino 2004-03-03]
		return "AutoMatch";
	}
	public void initialize(final PlugInContext context) throws Exception {
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[]{RoadMatcherToolboxPlugIn.MENU_NAME},
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
	private QueryToolboxPanel queryToolboxPanel(WorkbenchContext context) {
		return (QueryToolboxPanel) FUTURE_GUIUtil.getDescendantOfClass(
				QueryToolboxPanel.class, SwingUtilities.getAncestorOfClass(
						WorkbenchFrame.class, RoadMatcherToolboxPlugIn
								.instance(context).getToolboxPanel()));
	}
	public void run(final TaskMonitor monitor, final PlugInContext context)
			throws Exception {
		monitor.allowCancellationRequests();
		ToolboxModel.instance(context).getSession().getRoadsEventFirer()
				.deferFiringEvents(new Block() {
					public Object yield() {
						time("Auto-Matching", context, new Block() {
							public Object yield() {
								ToolboxModel.instance(context).getSession()
										.autoMatch(monitor);
								return null;
							}
						});
						time("Updating result states", context, new Block() {
							public Object yield() {
								ToolboxModel.instance(context)
										.updateResultStates(monitor);
								return null;
							}
						});
						return null;
					}
				});
		//The problem: We made a decision to put the responsibility of
		//firing feature events on tools/plug-ins, rather than automatically
		//driven by changes to the network. The AutoMatchPlugIn wasn't firing
		//feature-added, feature-modified, feature-removed events.
		//
		//How I fixed it: Unfortunately it is hard to determine which features
		//have been added/modified/removed after AutoMatching. So I
		//just tell the three tables to do a full refresh. Note that my
		//workaround is a manual update of three listeners -- other listeners
		//(if any) do not do a full refresh, of course. So if you've got the
		//AttributeViewer window open on a layer and do an AutoMatch, I
		//suspect it will get out of sync. Might not be a big deal tho ... ?
		//
		//The right way to fix this would be to fire feature events
		//automatically in response to changes in the network. I'm a bit
		//afraid to do this because it involves touching lots of code
		//i.e. removing the calls to #fireFeatureEvent from *all* the tools
		//and plug-ins for roads. Plus I'd want to think about aggregating
		//feature events a bit (to avoid refreshing the screen/tables a
		//hundred times a second, for example).
		//
		//[Jon Aquino 2004-02-06]
		
		//Note that we have recently implemented events, so we can now
		//do it the right way if we wanted [Jon Aquino 2004-08-04]
		if (queryToolboxPanel(context.getWorkbenchContext()) != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					queryToolboxPanel(context.getWorkbenchContext())
							.getTableTab().refresh();
				}
			});
		}
	}
	private void time(String description, PlugInContext context, Block block) {
		long start = System.currentTimeMillis();
		block.yield();
		context.getWorkbenchFrame().log(
				description
						+ ": "
						+ StringUtil.toTimeString(System.currentTimeMillis()
								- start));
	}
	private String taskMonitorTitle;
}