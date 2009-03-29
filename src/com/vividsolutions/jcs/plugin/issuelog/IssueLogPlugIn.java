package com.vividsolutions.jcs.plugin.issuelog;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.vividsolutions.jcs.plugin.RoadMatcherExtension;
import com.vividsolutions.jcs.plugin.conflate.roads.RoadMatcherToolboxPlugIn;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.AttributeTablePanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OneLayerAttributeTab;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.plugin.ViewAttributesPlugIn;

public class IssueLogPlugIn extends AbstractPlugIn {
	public void initialize(PlugInContext context) throws Exception {
		modifyAttributeViewers(context);
		ensureDeleteSelectedItemsEnabled(context);
		RoadMatcherExtension
				.addMainMenuItemWithJava14Fix(
						context,
						this,
						new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
								RoadMatcherToolboxPlugIn.VIEW_MENU_NAME },
						getName(),
						false,
						null,
						new EnableCheckFactory(context.getWorkbenchContext())
								.createWindowWithAssociatedTaskFrameMustBeActiveCheck());
	}

	private void ensureDeleteSelectedItemsEnabled(final PlugInContext context) {
		AttributeTab.popupMenu(context.getWorkbenchContext())
				.addPopupMenuListener(new PopupMenuListener() {
					public void popupMenuCanceled(PopupMenuEvent e) {
					}

					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					}

					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						final JMenuItem menuItem = FeatureInstaller
								.childMenuItem(new DeleteSelectedItemsPlugIn()
										.getName(), AttributeTab
										.popupMenu(context
												.getWorkbenchContext()));
						if (menuItem == null) {
							//Not critical. Do nothing. [Jon Aquino 2004-03-10]
							return;
						}
						//Ensure my call to #setEnabled(true) happens after
						//DeleteSelectedItemsPlugIn's EnableCheck runs
						//[Jon Aquino 2004-03-10]
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								menuItem.setEnabled(true);
							}
						});
					}
				});
	}

	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		final ViewAttributesPlugIn.ViewAttributesFrame frame = new ViewAttributesPlugIn.ViewAttributesFrame(
				IssueLog.instance(context.getLayerManager()).getLayer(),
				context);
		context.getWorkbenchFrame().addInternalFrame(frame);
		return true;
	}

	private Collection checkedInternalFrames = new ArrayList();

	private void modifyAttributeViewers(final PlugInContext context) {
		GUIUtil.addInternalFrameListener(context.getWorkbenchFrame()
				.getDesktopPane(), new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				if (checkedInternalFrames.contains(e.getInternalFrame())) {
					return;
				}
				OneLayerAttributeTab tab = issueLogOneLayerAttributeTab(e
						.getInternalFrame(), context.getWorkbenchContext()
						.getLayerManager());
				if (tab == null) {
					return;
				}
				if (tab.getLayer().getFeatureCollectionWrapper().isEmpty()) {
					tab.add(new JLabel(
							"To add an issue, right-click a selected feature or fence and choose "
									+ new CreateOrEditIssuePlugIn().getName()),
							BorderLayout.NORTH);
				}
				AttributeTablePanel tablePanel = (AttributeTablePanel) GUIUtil
						.getDescendantOfClass(AttributeTablePanel.class, tab);
				tablePanel
						.setFeatureEditor(new AttributeTablePanel.FeatureEditor() {
							public void edit(PlugInContext context,
									Feature feature, Layer layer)
									throws Exception {
								new EditIssuePlugIn().setIssue(feature)
										.execute(context);
							}
						});
				checkedInternalFrames.add(e.getInternalFrame());
			}
		});
	}

	private OneLayerAttributeTab issueLogOneLayerAttributeTab(
			JInternalFrame frame, LayerManager layerManager) {
		OneLayerAttributeTab tab = (OneLayerAttributeTab) GUIUtil
				.getDescendantOfClass(OneLayerAttributeTab.class, frame);
		if (tab == null) {
			return null;
		}
		if (tab.getLayer() != IssueLog.instance(layerManager).getLayer()) {
			return null;
		}
		return tab;
	}
}