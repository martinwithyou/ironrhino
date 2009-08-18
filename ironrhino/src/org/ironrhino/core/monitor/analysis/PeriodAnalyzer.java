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

public class PeriodAnalyzer extends AbstractAnalyzer<List<Value>> {

	private List<Value> result = new ArrayList<Value>(24);

	private Key key;

	Calendar calendar = Calendar.getInstance();

	public PeriodAnalyzer(Key key) throws FileNotFoundException {
		super();
		this.key = key;
	}

	public PeriodAnalyzer(Key key, Date date) throws FileNotFoundException {
		super(date);
		this.key = key;
	}

	public PeriodAnalyzer(Key key, Date[] dates) throws FileNotFoundException {
		super(dates);
		this.key = key;
	}

	public PeriodAnalyzer(Key key, Date start, Date end)
			throws FileNotFoundException {
		super(start, end);
		this.key = key;
	}

	public PeriodAnalyzer(Key key, Iterator<? extends KeyValuePair> iterator) {
		super(iterator);
		this.key = key;
	}

	public List<Value> getResult() {
		return result;
	}

	@Override
	protected void preAnalyze() {
		if (key == null)
			throw new IllegalArgumentException("key is null");
		for (int i = 0; i < 24; i++) {
			result.add(new Value());
		}
	}

	@Override
	protected void process(KeyValuePair pair) {
		if (!key.isAncestorOf(pair.getKey()))
			return;
		calendar.setTime(pair.getDate());
		Value v = result.get(calendar.get(Calendar.HOUR_OF_DAY));
		v.cumulate(pair.getValue());
	}

}