package com.vividsolutions.jcs.jump;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class FUTURE_AbstractAddRemoveListModel implements
		FUTURE_AddRemoveListModel {

	private List listeners = new ArrayList();		

	public void addListener(ListDataListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ListDataListener listener) {
		listeners.remove(listener);
	}		
	
	protected void fireContentsChanged(int index0, int index1) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			ListDataListener listener = (ListDataListener) i.next();
			// Funny that you specify the event type twice: by the event-type,
			// parameter, and the choice of ListDataListener method. Nothing
			// prevents you from mismatching the two (this was painful 
			// for me to debug). [Jon Aquino 2004-09-28]
			listener.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, index0, index1));
		}
	}

	protected void fireIntervalAdded(int index0, int index1) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			ListDataListener listener = (ListDataListener) i.next();
			listener.intervalAdded(new ListDataEvent(this,
					ListDataEvent.INTERVAL_ADDED, index0, index1));
		}
	}

	protected void fireIntervalRemoved(int index0, int index1) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			ListDataListener listener = (ListDataListener) i.next();
			listener.intervalRemoved(new ListDataEvent(this,
					ListDataEvent.INTERVAL_REMOVED, index0, index1));
		}
	}

	protected void fireStructureChanged(int oldSize, int newSize) {
		if (newSize < oldSize) {
			fireIntervalRemoved(newSize, oldSize - 1);
		} else if (oldSize < newSize) {
			fireIntervalAdded(oldSize, newSize - 1);
		}
		fireContentsChanged(0, Math.min(oldSize, newSize) - 1);
	}
}
