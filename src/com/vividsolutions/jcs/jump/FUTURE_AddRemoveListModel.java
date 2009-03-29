package com.vividsolutions.jcs.jump;

import javax.swing.event.ListDataListener;

import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;

public interface FUTURE_AddRemoveListModel extends AddRemoveListModel {

	public void addListener(ListDataListener listener);
	public void removeListener(ListDataListener listener);
	
}
