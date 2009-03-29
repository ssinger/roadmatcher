package com.vividsolutions.jcs.jump;

import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;

public class FUTURE_DummyLayerViewPanelContext implements LayerViewPanelContext {

	public void setStatusMessage(String message) {
	}

	public void warnUser(String warning) {
	}

	public void handleThrowable(Throwable t) {
		throw new RuntimeException(t);
	}

}
