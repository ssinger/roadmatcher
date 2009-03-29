package com.vividsolutions.jcs.plugin.conflate.roads;

import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public class SegmentInfoPanelFactory {

	public static final String SEGMENT_INFO_ATTRIBUTES_0 = "Segment Info Attributes 0";

	public static final String SEGMENT_INFO_ATTRIBUTES_1 = "Segment Info Attributes 1";

	public SegmentInfoPanel createPanel(WorkbenchContext context,
			Block enableLogic) {
		SegmentInfoPanel panel = new SegmentInfoPanel();
		new SegmentInfoPanelMouseMotionListenerInstaller().install(context,
				panel, enableLogic);
		new SegmentInfoPanelQueryToolboxPanelListenerInstaller().install(
				context, panel);
		return panel;
	}

}