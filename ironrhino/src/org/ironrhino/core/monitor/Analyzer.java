package org.ironrhino.core.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Analyzer {

	private File[] files;

	private Map<Key, Value> cumulativeData = new HashMap<Key, Value>(50);

	public Analyzer() {
		this.files = new File[] { new File(Monitor.getPath()) };
		parse();
	}

	public Analyzer(Date date) {
		this.files = new File[] { new File(Monitor.getPath()
				+ new SimpleDateFormat(Monitor.DATE_STYLE).format(date)) };
		parse();
	}

	public Analyzer(Date[] dates) {
		this.files = new File[dates.length];
		for (int i = 0; i < dates.length; i++)
			this.files[i] = new File(Monitor.getPath()
					+ new SimpleDateFormat(Monitor.DATE_STYLE).format(dates[i]));
		parse();
	}

	public Analyzer(Date start, Date end) {
		this(start, end, false);
	}

	public Analyzer(Date start, Date end, boolean excludeEnd) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(end);
		int endDay = cal.get(Calendar.DAY_OF_YEAR);
		if (excludeEnd)
			endDay--;
		cal.setTime(start);
		int startDay = cal.get(Calendar.DAY_OF_YEAR);
		this.files = new File[endDay - startDay + 1];
		for (int i = 0; i < files.length; i++) {
			cal.add(Calendar.DAY_OF_YEAR, i);
			this.files[i] = new File(Monitor.getPath()
					+ new SimpleDateFormat(Monitor.DATE_STYLE).format(cal
							.getTime()));
		}
		parse();
	}

	public Analyzer(File file) {
		this.files = new File[] { file };
		parse();
	}

	public Analyzer(File... files) {
		this.files = files;
		parse();
	}

	private void parse() {
		String line = null;
		try {
			for (File file : files) {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis, Monitor.ENCODING));
				while ((line = br.readLine()) != null)
					parseLine(line);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public Map<Key, Value> getCumulativeData() {
		return cumulativeData;
	}

	private void parseLine(String line) {
		String[] array = line.split("\\|");
		Key key = Key.fromString(array[0]);
		Value value = Value.fromString(array[1]);
		if (key.isCumulative()) {
			Value v = cumulativeData.get(key);
			if (v == null)
				cumulativeData.put(key, value);
			else
				v.cumulate(value);
		}
	}

}
