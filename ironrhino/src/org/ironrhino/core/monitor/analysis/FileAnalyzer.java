package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

	public FileAnalyzer(Iterator<KeyValuePair>... iterators) {
		super(iterators);
	}

	public FileAnalyzer() throws FileNotFoundException {
		this(new Date());
	}

	public FileAnalyzer(Date date) throws FileNotFoundException {
		this.files = getLogFile(date);
		newIterator();
	}

	public FileAnalyzer(Date[] dates) throws FileNotFoundException {
		List<File> list = new ArrayList<File>();
		for (int i = 0; i < dates.length; i++) {
			File[] array = getLogFile(dates[i]);
			if (array != null)
				for (File f : array)
					list.add(f);
		}
		this.files = list.toArray(new File[0]);
		newIterator();
	}

	public FileAnalyzer(Date start, Date end, boolean excludeEnd)
			throws FileNotFoundException {
		List<File> list = new ArrayList<File>();
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
			File[] array = getLogFile(cal.getTime());
			if (array != null)
				for (File f : array)
					list.add(f);
		}
		this.files = list.toArray(new File[0]);
		newIterator();
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

	private void newIterator() throws FileNotFoundException {
		for (File f : files)
			if (!f.exists())
				throw new FileNotFoundException(f.getAbsolutePath());
		this.iterator = new TextFileIterator<KeyValuePair>(
				MonitorSettings.ENCODING, files) {
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

	public static File[] getLogFile(Date date) {
		boolean today = DateUtils.isToday(date);
		StringBuilder sb = new StringBuilder();
		sb.append('_');
		sb.append(MonitorSettings.STAT_LOG_FILE_NAME);
		if (!today)
			sb.append(new SimpleDateFormat(MonitorSettings.DATE_STYLE)
					.format(date));
		final String suffix = sb.toString();
		File dir = MonitorSettings.getLogFileDirectory();
		return dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().endsWith(suffix))
					return true;
				return false;
			}
		});
	}

	public static boolean hasLogFile(Date date) {
		File[] files = getLogFile(date);
		return files != null && files.length > 0;
	}

}