package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.common.util.TextFileIterator;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.MonitorSettings;
import org.ironrhino.core.monitor.Value;

public abstract class FileAnalyzer extends AbstractAnalyzer {

	protected Log log = LogFactory.getLog(getClass());

	protected File[] files;

	public FileAnalyzer() throws FileNotFoundException {
		this.files = new File[] { new File(MonitorSettings
				.getLogFile(MonitorSettings.STAT_LOG_FILE)) };
		checkFiles();
	}

	public FileAnalyzer(Date date) throws FileNotFoundException {
		boolean isToday = DateUtils.isToday(date);
		this.files = new File[] { new File(MonitorSettings
				.getLogFile(MonitorSettings.STAT_LOG_FILE)
				+ (isToday ? "" : new SimpleDateFormat(
						MonitorSettings.DATE_STYLE).format(date))) };
		checkFiles();
	}

	public FileAnalyzer(Date[] dates) throws FileNotFoundException {
		this.files = new File[dates.length];
		for (int i = 0; i < dates.length; i++) {
			boolean isToday = DateUtils.isToday(dates[i]);
			this.files[i] = new File(MonitorSettings
					.getLogFile(MonitorSettings.STAT_LOG_FILE)
					+ (isToday ? "" : new SimpleDateFormat(
							MonitorSettings.DATE_STYLE).format(dates[i])));
		}
		checkFiles();
	}

	public FileAnalyzer(Date start, Date end, boolean excludeEnd)
			throws FileNotFoundException {
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
			{
				Date date = cal.getTime();
				boolean isToday = DateUtils.isToday(date);
				this.files[i] = new File(MonitorSettings
						.getLogFile(MonitorSettings.STAT_LOG_FILE)
						+ (isToday ? "" : new SimpleDateFormat(
								MonitorSettings.DATE_STYLE).format(date)));
			}
		}
		checkFiles();
	}

	public FileAnalyzer(Date start, Date end) throws FileNotFoundException {
		this(start, end, false);
	}

	public FileAnalyzer(File file) {
		this.files = new File[] { file };
	}

	public FileAnalyzer(File... files) {
		this.files = files;
	}

	private void checkFiles() throws FileNotFoundException {
		for (File f : files)
			if (!f.exists())
				throw new FileNotFoundException(f.getAbsolutePath());
	}

	public Iterator<KeyValuePair> iterate() {
		return new TextFileIterator<KeyValuePair>(MonitorSettings.ENCODING,
				files) {
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

}