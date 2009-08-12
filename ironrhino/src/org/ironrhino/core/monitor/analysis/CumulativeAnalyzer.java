package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;

public class CumulativeAnalyzer extends Analyzer {

	private Map<Key, Value> cumulativeData = new HashMap<Key, Value>(50);

	public CumulativeAnalyzer() {
		super();
	}

	public CumulativeAnalyzer(Date start, Date end, boolean excludeEnd) {
		super(start, end, excludeEnd);
	}

	public CumulativeAnalyzer(Date start, Date end) {
		super(start, end);
	}

	public CumulativeAnalyzer(Date date) {
		super(date);
	}

	public CumulativeAnalyzer(Date[] dates) {
		super(dates);
	}

	public CumulativeAnalyzer(File... files) {
		super(files);
	}

	public CumulativeAnalyzer(File file) {
		super(file);
	}

	public Map<Key, Value> getCumulativeData() {
		return cumulativeData;
	}

	protected void process(Key key, Value value, Date date) {
		if (key.isCumulative()) {
			Value v = cumulativeData.get(key);
			if (v == null)
				cumulativeData.put(key, value);
			else
				v.cumulate(value);
		}
	}

}
