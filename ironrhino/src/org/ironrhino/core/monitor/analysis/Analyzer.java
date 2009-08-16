package org.ironrhino.core.monitor.analysis;

import java.util.Iterator;

import org.ironrhino.core.monitor.KeyValuePair;

public interface Analyzer<T> {

	public void analyze();

	public Iterator<KeyValuePair> iterate();

	public T getResult();

}