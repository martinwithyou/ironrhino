package org.ironrhino.core.monitor.analysis;

import java.util.Iterator;

import org.ironrhino.core.monitor.KeyValuePair;

public abstract class AbstractAnalyzer implements Analyzer {

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

	protected abstract Iterator<KeyValuePair> iterate();

	protected abstract void process(KeyValuePair pair);

	protected void preAnalyze() {

	}

	protected void postAnalyze() {

	}

}