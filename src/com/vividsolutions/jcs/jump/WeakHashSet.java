package com.vividsolutions.jcs.jump;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakHashSet implements Set {
	private WeakHashMap map = new WeakHashMap();

	private static final Object DUMMY_VALUE = new Object();

	public int size() {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	public boolean add(Object o) {
		return map.put(o, DUMMY_VALUE) == null;
	}

	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public Iterator iterator() {
		throw new UnsupportedOperationException();
	}

	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException();
	}
}