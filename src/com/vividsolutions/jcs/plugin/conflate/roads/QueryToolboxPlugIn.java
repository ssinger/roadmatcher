package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComboBox;

import com.vividsolutions.jcs.jump.FUTURE_ToolboxPlugIn;
import com.vividsolutions.jcs.jump.FUTURE_ToolboxStateManager;
import com.vividsolutions.jcs.plugin.clean.coveragecleaningtoolbox.Tab;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxStateManager;

public class QueryToolboxPlugIn extends FUTURE_ToolboxPlugIn {
	private static final String INSTANCE_KEY = QueryToolboxPlugIn.class
			.getName()
			+ " - INSTANCE";

	public static QueryToolboxPlugIn instance(WorkbenchContext context) {
		return (QueryToolboxPlugIn) context.getBlackboard().get(INSTANCE_KEY);
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getWorkbenchContext().getBlackboard().put(INSTANCE_KEY, this);
		createMainMenuItem(new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
				RoadMatcherToolboxPlugIn.VIEW_MENU_NAME }, null, context
				.getWorkbenchContext());
	}

	public String getName() {
		return "Query";
	}

	protected void initializeToolbox(final ToolboxDialog toolbox) {
		final QueryToolboxPanel panel = new QueryToolboxPanel(toolbox
				.getContext());
		panel.setPreferredSize(new Dimension(611, 168));
		toolbox.getCenterPanel().add(panel, BorderLayout.CENTER);
		toolbox.setInitialLocation(new GUIUtil.Location(20, false, 20, true));
		toolbox.addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				panel.updateComponents();
			}
		});
		GUIUtil.addInternalFrameListener(toolbox.getContext().getWorkbench()
				.getFrame().getDesktopPane(), GUIUtil
				.toInternalFrameListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						panel.updateComponents();
					}
				}));
		//Use a ToolboxStateManager to automatically change the AttributeViewer
		//when the TaskFrame changes; otherwise, buttons will disable when
		//first TaskFrame is closed (because an AttributeViewer is associated
		//with one TaskFrame) [Jon Aquino 2004-05-14]
		//Don't manage the comboboxes. Otherwise we start getting status-line
		//enable-check warnings). [Jon Aquino 2004-05-14]
		new ToolboxStateManager(toolbox, CollectionUtil.createMap(new Object[] {
				Tab.class, Tab.STRATEGY, JComboBox.class,
				FUTURE_ToolboxStateManager.DUMMY_STRATEGY }));
	}
}