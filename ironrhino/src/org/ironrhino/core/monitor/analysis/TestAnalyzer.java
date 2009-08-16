package org.ironrhino.core.monitor.analysis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.Value;

public class TestAnalyzer extends AbstractAnalyzer<List<Value>> {

	private List<Value> result = new ArrayList<Value>();

	Calendar calendar = Calendar.getInstance();

	int currentHour = 0;

	private Key key;

	public TestAnalyzer(Key key) throws FileNotFoundException {
		super();
		this.key = key;
	}

	public TestAnalyzer(Key key, Date date) throws FileNotFoundException {
		super(date);
		this.key = key;
	}

	public TestAnalyzer(Key key, Date[] dates) throws FileNotFoundException {
		super(dates);
		this.key = key;
	}

	public TestAnalyzer(Key key, Date start, Date end)
			throws FileNotFoundException {
		super(start, end);
		this.key = key;
	}

	public TestAnalyzer(Key key, Iterator<KeyValuePair>... iterators) {
		super(iterators);
		this.key = key;
	}

	public List<Value> getResult() {
		return result;
	}

	public void dosomething() {
		// TODO;
	}

	@Override
	protected void preAnalyze() {
		if (key == null)
			throw new IllegalArgumentException("key is null");
	}

	@Override
	protected void process(KeyValuePair pair) {
		if (!key.equals(pair.getKey()))
			return;
		// TODO
	}

	@Override
	protected void postAnalyze() {
		dosomething();
	}

}