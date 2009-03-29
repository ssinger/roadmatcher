package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import com.vividsolutions.jcs.jump.FUTURE_ToolboxPlugIn;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.HTMLPanel;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;

public class SegmentInfoToolboxPlugIn extends FUTURE_ToolboxPlugIn {

	public void initialize(PlugInContext context) throws Exception {
		createMainMenuItem(new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
				RoadMatcherToolboxPlugIn.VIEW_MENU_NAME }, null, context
				.getWorkbenchContext());
	}

	protected void initializeToolbox(final ToolboxDialog toolbox) {
		SegmentInfoPanel segmentInfoPanel = new SegmentInfoPanelFactory()
				.createPanel(toolbox.getContext(), new Block() {
					public Object yield() {
						return Boolean.valueOf(toolbox.isVisible());
					}
				});
		segmentInfoPanel.setPreferredSize(new Dimension(450, 200));
		toolbox.getCenterPanel().add(segmentInfoPanel, BorderLayout.CENTER);
		toolbox.setInitialLocation(new GUIUtil.Location(20, false, 20, true));
	}

	public String getName() {
		return "Segment Info";
	}

}