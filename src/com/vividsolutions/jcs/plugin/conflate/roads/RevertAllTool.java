package com.vividsolutions.jcs.plugin.conflate.roads;

import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jcs.jump.FUTURE_GUIUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

public class RevertAllTool extends DragTool {
	protected void gestureFinished() throws Exception {
		new RevertAllOp().execute(EnvelopeUtil
				.toGeometry(getBoxInModelCoordinates()), getWorkbench()
				.getContext());
	}

	private ImageIcon icon = SpecifyRoadFeaturesTool
			.createIcon("revert-all-tool-button.png");

	private Cursor cursor = FUTURE_GUIUtil
			.createCursorFromIcon("revert-all-tool-button.png");

	public Icon getIcon() {
		return icon;
	}

	public Cursor getCursor() {
		return cursor;
	}
}