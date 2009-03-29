package com.vividsolutions.jcs.plugin;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import com.vividsolutions.jcs.jump.FUTURE_CursorToolToPlugInAdapter;
import com.vividsolutions.jcs.jump.FUTURE_FeatureInstaller;
import com.vividsolutions.jcs.jump.FUTURE_InstallStandardFeatureTextWritersPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.ApplicationOptionsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.AutoConflatePlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.AutoConnectEndpointTool;
import com.vividsolutions.jcs.plugin.conflate.roads.ClearPathsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.CommitTool;
import com.vividsolutions.jcs.plugin.conflate.roads.ConflationOptionsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateIntersectionSplitNodeTool;
import com.vividsolutions.jcs.plugin.conflate.roads.CreateThemingLayerPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.EditSegmentCommentPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.ExportResultPackagePlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.FindClosestRoadSegmentPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.FindSegmentsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.GenerateAdjustmentVectorsLayerPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.GenerateResultLayerPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.ImportSourcePackagePlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.LegendToolboxPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.MarkAsReviewedPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.MatchPathsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.MatchSelectedSegmentsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.NewSessionPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.OpenRoadMatcherSessionPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.PanToOtherEndTool;
import com.vividsolutions.jcs.plugin.conflate.roads.PostponeInconsistencyPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.QueryToolboxPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.RestorePostponedInconsistenciesPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.RestorePostponedInconsistencyPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.ResultOptionsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.RevertAllInFencePlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.RevertSegmentTool;
import com.vividsolutions.jcs.plugin.conflate.roads.RightClickSegmentPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.SegmentInfoToolboxPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.RoadMatcherToolboxPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.SaveProfileAsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.SaveSessionAsPlugIn;
import com.vividsolutions.jcs.plugin.conflate.roads.SpecifyRoadFeaturesTool;
import com.vividsolutions.jcs.plugin.conflate.roads.StatisticsPlugIn;
import com.vividsolutions.jcs.plugin.issuelog.CreateOrEditIssuePlugIn;
import com.vividsolutions.jcs.plugin.issuelog.IssueLogPlugIn;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class RoadMatcherExtension extends Extension {
	public static final String VERSION = "1.4";

	public String getVersion() {
		return VERSION;
	}

	public void configure(PlugInContext context) throws Exception {
		FUTURE_InstallStandardFeatureTextWritersPlugIn.fix(context
				.getWorkbenchContext());
		new NewSessionPlugIn(context.getWorkbenchContext()).initialize(context);
		new ImportSourcePackagePlugIn().initialize(context);
		new OpenRoadMatcherSessionPlugIn().initialize(context);
		new SaveSessionAsPlugIn().initialize(context);
		context.getFeatureInstaller().addMenuSeparator(
				RoadMatcherToolboxPlugIn.MENU_NAME);
		new ConflationOptionsPlugIn().initialize(context);
		new ApplicationOptionsPlugIn().initialize(context);
		new SaveProfileAsPlugIn().initialize(context);
		context.getFeatureInstaller().addMenuSeparator(
				RoadMatcherToolboxPlugIn.MENU_NAME);
		new AutoConflatePlugIn().initialize(context);
		new ResultOptionsPlugIn().initialize(context);
		new GenerateResultLayerPlugIn().initialize(context);
		new ExportResultPackagePlugIn().initialize(context);
		context.getFeatureInstaller().addMenuSeparator(
				RoadMatcherToolboxPlugIn.MENU_NAME);
		new RoadMatcherToolboxPlugIn().initialize(context);
		new LegendToolboxPlugIn().initialize(context);
		new QueryToolboxPlugIn().initialize(context);
		new SegmentInfoToolboxPlugIn().initialize(context);
		new IssueLogPlugIn().initialize(context);
		new RestorePostponedInconsistenciesPlugIn().initialize(context);
		new RevertAllInFencePlugIn().initialize(context);
		new GenerateAdjustmentVectorsLayerPlugIn().initialize(context);
		new CreateThemingLayerPlugIn().initialize(context);
		new FindSegmentsPlugIn().initialize(context);
		new StatisticsPlugIn().initialize(context);
		context.getFeatureInstaller().addMenuSeparator(
				RoadMatcherToolboxPlugIn.MENU_NAME);
		new MatchSelectedSegmentsPlugIn().initialize(context);
		new MatchPathsPlugIn().initialize(context);
		new ClearPathsPlugIn().initialize(context);
		installPopupMenuItems(context);
	}

	private void installPopupMenuItems(final PlugInContext context)
			throws Exception {
		LayerViewPanel.popupMenu().addSeparator();
		new FeatureInstaller(context.getWorkbenchContext()).addPopupMenuItem(
				LayerViewPanel.popupMenu(), new CreateOrEditIssuePlugIn(),
				new CreateOrEditIssuePlugIn().getName() + "...", false, null,
				new EnableCheckFactory(context.getWorkbenchContext())
						.createWindowWithLayerViewPanelMustBeActiveCheck());
		installPopupMenuItem(new String[] {}, new EditSegmentCommentPlugIn(),
				new EditSegmentCommentPlugIn().getName() + "...", null,
				context, RightClickSegmentPlugIn.createEnableCheck(context));
		installPopupMenuItem(new String[] {},
				new MarkAsReviewedPlugIn.Reviewed(), null, context,
				RightClickSegmentPlugIn.createEnableCheck(context));
		installPopupMenuItem(new String[] {},
				new MarkAsReviewedPlugIn.Unreviewed(), null, context,
				RightClickSegmentPlugIn.createEnableCheck(context));
		LayerViewPanel.popupMenu().addSeparator();
		installPopupMenuItem(new String[] {}, new SelectFeaturesTool(), context);
		installPopupMenuItem(new String[] {}, new PanToOtherEndTool(context
				.getWorkbenchContext()), context);
		new PostponeInconsistencyPlugIn().initialize(context);
		new RestorePostponedInconsistencyPlugIn().initialize(context);
		LayerViewPanel.popupMenu().addSeparator();
		installPopupMenuItem(new String[] {}, new MatchPathsPlugIn(), null,
				context, MatchPathsPlugIn.createEnableCheck(context));
		installPopupMenuItem(new String[] {}, new ClearPathsPlugIn(), null,
				context, ClearPathsPlugIn.createEnableCheck(context));
		installPopupMenuItem(new String[] {},
				new CreateIntersectionSplitNodeTool(context
						.getWorkbenchContext()), context);
		//The gesture for the commit tools is a click, so use CommitTool
		//rather than CommitOrPreciseMatchTool [Jon Aquino 2004-02-11]
		installPopupMenuItem(new String[] { "Road Tools" }, new CommitTool(
				CommitTool.BOTH_LAYERS, null, "commit-tool-button.png",
				Color.black, context.getWorkbenchContext()) {
			public String getName() {
				return "Match/Commit Section";
			}
		}, context);
		installPopupMenuItem(new String[] { "Road Tools" }, new CommitTool(
				CommitTool.SOURCE_LAYER_0, null, "commit-tool-0-button.png",
				Color.red, context.getWorkbenchContext()) {
			public String getName() {
				return "Match/Commit "
						+ RoadMatcherToolboxPlugIn.datasetName(0, context
								.getWorkbenchContext()) + " Section";
			}
		}, context);
		installPopupMenuItem(new String[] { "Road Tools" }, new CommitTool(
				CommitTool.SOURCE_LAYER_1, null, "commit-tool-1-button.png",
				Color.blue, context.getWorkbenchContext()) {
			public String getName() {
				return "Match/Commit "
						+ RoadMatcherToolboxPlugIn.datasetName(1, context
								.getWorkbenchContext()) + " Section";
			}
		}, context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn.createRetireTool(context
						.getWorkbenchContext()), context);
		installPopupMenuItem(new String[] { "Road Tools" },
				new RevertSegmentTool(context.getWorkbenchContext()), context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn.createCreateSplitNodeTool(context
						.getWorkbenchContext()), context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn.createCreateSplitNodeTool0(context
						.getWorkbenchContext()), context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn.createCreateSplitNodeTool1(context
						.getWorkbenchContext()), context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn.createDeleteSplitNodeTool(context
						.getWorkbenchContext()), context);
		installPopupMenuItem(new String[] { "Road Tools" },
				new AutoConnectEndpointTool(context.getWorkbenchContext()),
				context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn
						.instance(context.getWorkbenchContext())
						.createInsertVertexTool(context.getWorkbenchContext()),
				context);
		installPopupMenuItem(new String[] { "Road Tools" },
				RoadMatcherToolboxPlugIn
						.instance(context.getWorkbenchContext())
						.createDeleteVertexTool(context.getWorkbenchContext()),
				context);
	}

	private void installFindClosestRoadSectionPlugIn(
			PlugIn findClosestRoadSectionPlugIn, String icon,
			PlugInContext context) {
		context.getFeatureInstaller().addMainMenuItem(
				findClosestRoadSectionPlugIn,
				new String[] { RoadMatcherToolboxPlugIn.MENU_NAME, "Zoom" },
				findClosestRoadSectionPlugIn.getName(),
				false,
				GUIUtil.toSmallIcon(IconLoader.icon(icon)),
				FindClosestRoadSegmentPlugIn.createEnableCheck(context
						.getWorkbenchContext()));
	}

	private void installPopupMenuItem(String[] menuPath, CursorTool tool,
			PlugInContext context) {
		installPopupMenuItem(menuPath, new FUTURE_CursorToolToPlugInAdapter(
				tool), tool.getIcon() != null ? GUIUtil
				.toSmallIcon((ImageIcon) tool.getIcon()) : null, context);
	}

	private void installPopupMenuItem(String[] menuPath, final PlugIn plugIn,
			Icon icon, PlugInContext context) {
		installPopupMenuItem(menuPath, plugIn, icon, context,
				SpecifyRoadFeaturesTool
						.createConflationSessionMustBeStartedCheck(context
								.getWorkbenchContext()));
	}

	private void installPopupMenuItem(String[] menuPath, final PlugIn plugIn,
			Icon icon, PlugInContext context, EnableCheck enableCheck) {
		installPopupMenuItem(menuPath, plugIn, plugIn.getName(), icon, context,
				enableCheck);
	}

	private void installPopupMenuItem(String[] menuPath, final PlugIn plugIn,
			final String text, Icon icon, PlugInContext context,
			EnableCheck enableCheck) {
		new FUTURE_FeatureInstaller(context.getWorkbenchContext())
				.addPopupMenuItem(LayerViewPanel.popupMenu(), plugIn, menuPath,
						text, false, icon, new MultiEnableCheck().add(
								new EnableCheck() {
									public String check(JComponent component) {
										//PlugIn name may change -- I am
										// thinking here especially
										//of the commit tools, which change
										// their names to reflect
										//the current layer names [Jon Aquino
										// 2004-02-11]
										((JMenuItem) component).setText(text);
										return null;
									}
								}).add(enableCheck));
	}

	/**
	 * Workaround for Java Bug 4809393: "Menus disappear prematurely after
	 * displaying modal dialog" Evidently fixed in Java 1.5. The workaround is
	 * to wrap #actionPerformed with SwingUtilities#invokeLater.
	 */
	public static void addMainMenuItemWithJava14Fix(PlugInContext context,
			PlugIn executable, String[] menuPath, String menuItemName,
			boolean checkBox, Icon icon, EnableCheck enableCheck) {
		FUTURE_FeatureInstaller installer = new FUTURE_FeatureInstaller(context
				.getWorkbenchContext());
		installer.addMainMenuItem(executable, menuPath, menuItemName, checkBox,
				icon, enableCheck);
		JMenuItem menuItem = FeatureInstaller
				.childMenuItem(menuItemName, ((JMenu) installer
						.createMenusIfNecessary(installer
								.menuBarMenu(menuPath[0]), installer
								._behead(menuPath))));
		final ActionListener listener = abstractPlugInActionListener(menuItem
				.getActionListeners());
		menuItem.removeActionListener(listener);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						listener.actionPerformed(e);
					}
				});
			}
		});
	}

	private static ActionListener abstractPlugInActionListener(
			ActionListener[] actionListeners) {
		for (int i = 0; i < actionListeners.length; i++) {
			if (actionListeners[i].getClass().getName().indexOf(
					AbstractPlugIn.class.getName()) > -1) {
				return actionListeners[i];
			}
		}
		Assert.shouldNeverReachHere();
		return null;
	}
}