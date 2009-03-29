package com.vividsolutions.jcs.jump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FUTURE_DefaultAddRemoveListModel extends
		FUTURE_AbstractAddRemoveListModel {

	public FUTURE_DefaultAddRemoveListModel() {
		this(new ArrayList());
	}

	public FUTURE_DefaultAddRemoveListModel(Collection items) {
		setItems(items);
	}

	private boolean sorted = false;

	private List items = new ArrayList();

	public void add(Object item) {
		items.add(item);
		if (sorted) {
			sort();
		}
		int i = items.indexOf(item);
		fireIntervalAdded(i, i);
	}

	public void setItems(Collection items) {
		int oldSize = this.items.size();
		int newSize = items.size();
		this.items = new ArrayList(items);
		if (sorted) {
			sort();
		}
		fireStructureChanged(oldSize, newSize);
	}

	private void sort() {
		Collections.sort(items);
	}

	public List getItems() {
		// Don't use Collections#unmodifiableList, which leads to
		// ConcurrentModificationExceptions because it backs the list.
		// [Jon Aquino 2004-09-28]
		return new ArrayList(items);
	}

	public FUTURE_DefaultAddRemoveListModel setSorted(boolean sorted) {
		this.sorted = sorted;
		if (sorted) {
			sort();
		}
		fireContentsChanged(0, items.size() - 1);
		return this;
	}

	public void remove(Object item) {
		int i = items.indexOf(item);
		items.remove(item);
		fireIntervalRemoved(i, i);
	}

}