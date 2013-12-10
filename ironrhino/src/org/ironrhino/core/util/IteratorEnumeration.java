package org.ironrhino.core.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<T> implements Enumeration<T> {

	private final Iterator<T> iterator;

	public IteratorEnumeration(Iterator<T> iterator) {
		if (iterator == null) {
			throw new IllegalArgumentException();
		}
		this.iterator = iterator;
	}

	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	@Override
	public T nextElement() {
		return iterator.next();
	}

	@Override
	public String toString() {
		return "IteratorEnumeration";
	}
}