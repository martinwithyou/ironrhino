package org.ironrhino.core.monitor.analysis;

import java.util.Iterator;

import org.ironrhino.common.util.CompositeIterator;
import org.ironrhino.core.monitor.KeyValuePair;

public abstract class AbstractAnalyzer implements Analyzer {

	protected Iterator<KeyValuePair> iterator;

	public AbstractAnalyzer() {

	}

	public AbstractAnalyzer(Iterator<KeyValuePair>... iterators) {
		if (iterators.length == 1)
			this.iterator = iterators[0];
		else
			this.iterator = new CompositeIterator<KeyValuePair>(iterators);
	}

	public void analyze() {
		preAnalyze();
		try {
			Iterator<KeyValuePair> it = iterate();
			while (it.hasNext())
				process(it.next());
			postAnalyze();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected Iterator<KeyValuePair> iterate() {
		return this.iterator;
	}

	protected abstract void process(KeyValuePair pair);

	protected void preAnalyze() {

	}

	protected void postAnalyze() {

	}

}