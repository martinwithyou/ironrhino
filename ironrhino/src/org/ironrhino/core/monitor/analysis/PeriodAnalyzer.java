package org.ironrhino.core.monitor.analysis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.Value;

public class PeriodAnalyzer extends AbstractAnalyzer<List<Value>> {

	private List<Value> result = new ArrayList<Value>(24);

	private Map<String, List<Value>> perHostResult = new HashMap<String, List<Value>>();

	private Key key;

	private boolean cumulative;

	private boolean perHostEnabled;

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

	public boolean isPerHostEnabled() {
		return perHostEnabled;
	}

	public void setPerHostEnabled(boolean perHostEnabled) {
		this.perHostEnabled = perHostEnabled;
	}

	public boolean isCumulative() {
		return cumulative;
	}

	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
	}

	public List<Value> getResult() {
		return result;
	}

	public Map<String, List<Value>> getPerHostResult() {
		return perHostResult;
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
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		result.get(hour).cumulate(pair.getValue());
		if (cumulative)
			for (int i = 0; i < hour - 1; i++)
				result.get(i).cumulate(pair.getValue());
		if (perHostEnabled) {
			List<Value> list = perHostResult.get(pair.getHost());
			if (list == null) {
				list = new ArrayList<Value>(24);
				for (int i = 0; i < 24; i++)
					list.add(new Value());
				perHostResult.put(pair.getHost(), list);
			}
			list.get(hour).cumulate(pair.getValue());
			if (cumulative)
				for (int i = 0; i < hour - 1; i++)
					list.get(i).cumulate(pair.getValue());
		}
	}

	@Override
	protected void postAnalyze() {
		if (!perHostEnabled)
			return;
		// sort map;
		Map<String, List<Value>> map = new LinkedHashMap<String, List<Value>>();
		List<String> hosts = new ArrayList<String>(perHostResult.size());
		hosts.addAll(perHostResult.keySet());
		Collections.sort(hosts);
		for (String host : hosts)
			map.put(host, perHostResult.get(host));
		perHostResult = map;
	}
}