package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.BorderFactory;
import com.vividsolutions.jcs.jump.FUTURE_ToolboxPlugIn;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil.Location;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;

public class LegendToolboxPlugIn extends FUTURE_ToolboxPlugIn {
	private static final String INSTANCE_KEY = LegendToolboxPlugIn.class
			.getName()
			+ " - INSTANCE";

	public static LegendToolboxPlugIn instance(WorkbenchContext context) {
		return (LegendToolboxPlugIn) context.getBlackboard().get(INSTANCE_KEY);
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getWorkbenchContext().getBlackboard().put(INSTANCE_KEY, this);
		createMainMenuItem(new String[] { RoadMatcherToolboxPlugIn.MENU_NAME,
				RoadMatcherToolboxPlugIn.VIEW_MENU_NAME }, null, context
				.getWorkbenchContext());
	}

	public String getName() {
		return "Legend";
	}

	protected void initializeToolbox(final ToolboxDialog toolbox) {
		toolbox.setResizable(false);
		final LegendToolboxPanel panel = new LegendToolboxPanel(toolbox
				.getContext().getWorkbench().getFrame(), toolbox.getContext());
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLoweredBevelBorder(), BorderFactory.createMatteBorder(5,
				5, 5, 5, Color.white)));
		toolbox.getCenterPanel().add(panel, BorderLayout.CENTER);
		toolbox.setInitialLocation(new Location(20, false, 20, false));
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
		blockMouseInput(toolbox);
	}

	private void blockMouseInput(final ToolboxDialog toolbox) {
		toolbox.getGlassPane().setVisible(true);
		toolbox.getGlassPane().addMouseListener(new MouseAdapter() {
		});
	}
}