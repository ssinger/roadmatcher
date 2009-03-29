package com.vividsolutions.jcs.plugin.conflate.roads;
import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jcs.jump.FUTURE_EventFirer;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;
public class AdjustEndpointTool extends DelegatingTool {
	private boolean activated;
	private LayerViewPanel panelIfActivated;
	public AdjustEndpointTool(final AdjustPanel adjustPanel) {
		super(new DummyTool());
		updateDelegate(adjustPanel);
		adjustPanel.getEventFirer().add(new FUTURE_EventFirer.Listener() {
			public void update(Object o) {
				if (isActivated()) {
					getDelegate().deactivate();
				}
				updateDelegate(adjustPanel);
				if (isActivated()) {
					getDelegate().activate(panelIfActivated);
				}
			}
		});
	}
	private void updateDelegate(final AdjustPanel adjustPanel) {
		try {
			setDelegate((CursorTool) adjustPanel.currentToolClass()
					.newInstance());
		} catch (InstantiationException e) {
			Assert.shouldNeverReachHere();
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere();
		}
	}
	public String getName() {
		return AbstractCursorTool.name(this);
	}
	private boolean isActivated() {
		return panelIfActivated != null;
	}
	public void activate(LayerViewPanel layerViewPanel) {
		super.activate(layerViewPanel);
		panelIfActivated = layerViewPanel;
	}
	public void deactivate() {
		super.deactivate();
		panelIfActivated = null;
	}
	public Cursor getCursor() {
		return cursor;
	}
	private ImageIcon icon = SpecifyRoadFeaturesTool
			.createIcon("adjust-endpoint-tool-button.png");
	private Cursor cursor = GUIUtil.createCursorFromIcon(icon.getImage());
	public Icon getIcon() {
		return icon;
	}
}
