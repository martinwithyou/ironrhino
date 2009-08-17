package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.util.CompositeIterator;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.common.util.TextFileIterator;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.MonitorSettings;
import org.ironrhino.core.monitor.Value;

public abstract class AbstractAnalyzer<T> implements Analyzer<T> {

	protected Log log = LogFactory.getLog(getClass());

	protected Iterator<KeyValuePair> iterator;

	public AbstractAnalyzer() throws FileNotFoundException {
		this(new Date());
	}

	public AbstractAnalyzer(Date date) throws FileNotFoundException {
		this.iterator = newIterator(getLogFile(date).values().toArray(
				new File[0]));
	}

	public AbstractAnalyzer(File file) throws FileNotFoundException {
		this.iterator = newIterator(new File[] { file });
	}

	public AbstractAnalyzer(Date[] dates) throws FileNotFoundException {
		List<File> list = new ArrayList<File>();
		for (int i = 0; i < dates.length; i++)
			list.addAll(getLogFile(dates[i]).values());
		this.iterator = newIterator(list.toArray(new File[0]));
	}

	public AbstractAnalyzer(Date start, Date end) throws FileNotFoundException {
		List<File> list = new ArrayList<File>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(end);
		int endDay = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(start);
		int startDay = cal.get(Calendar.DAY_OF_YEAR);
		for (int i = 0; i < (endDay - startDay + 1); i++) {
			cal.add(Calendar.DAY_OF_YEAR, i);
			list.addAll(getLogFile(cal.getTime()).values());
		}
		this.iterator = newIterator(list.toArray(new File[0]));
	}

	public AbstractAnalyzer(Iterator<KeyValuePair>... iterators) {
		if (iterators.length == 1)
			this.iterator = iterators[0];
		else
			this.iterator = new CompositeIterator<KeyValuePair>(iterators);
	}

	private Iterator<KeyValuePair> newIterator(File[] files)
			throws FileNotFoundException {
		for (File f : files)
			if (!f.exists())
				throw new FileNotFoundException(f.getAbsolutePath());
		return new TextFileIterator<KeyValuePair>(MonitorSettings.ENCODING,
				files) {
			@Override
			protected KeyValuePair transform(String line) {
				String[] array = line.split("\\|");
				Key key = Key.fromString(array[0]);
				Value value = Value.fromString(array[1]);
				Date date = null;
				if (StringUtils.isNumeric(array[2])) {
					date = new Date(Long.valueOf(array[2]));
				} else {
					try {
						date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.parse(array[2]);
					} catch (Exception e) {

					}
				}
				return new KeyValuePair(key, value, date);
			}
		};
	}

	@Override
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

	@Override
	public Iterator<KeyValuePair> iterate() {
		return this.iterator;
	}

	protected void preAnalyze() {

	}

	protected void postAnalyze() {

	}

	protected abstract void process(KeyValuePair pair);

	// host,file pair
	public static Map<String, File> getLogFile(Date date) {
		final Map<String, File> map = new LinkedHashMap<String, File>();
		boolean today = DateUtils.isToday(date);
		StringBuilder sb = new StringBuilder();
		sb.append('_');
		sb.append(MonitorSettings.STAT_LOG_FILE_NAME);
		if (!today)
			sb.append(new SimpleDateFormat(MonitorSettings.DATE_STYLE)
					.format(date));
		final String suffix = sb.toString();
		File dir = MonitorSettings.getLogFileDirectory();
		dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				String name = f.getName();
				if (name.endsWith(suffix)) {
					map.put(name.substring(0, name.lastIndexOf(suffix)), f);
					return true;
				}
				return false;
			}
		});
		return map;
	}

	public static boolean hasLogFile(Date date) {
		return getLogFile(date).size() > 0;
	}
}