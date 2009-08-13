package org.ironrhino.core.monitor.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.MonitorSettings;
import org.ironrhino.core.monitor.Value;

public abstract class StatAnalyzer implements Analyzer {

	protected Log log = LogFactory.getLog(getClass());

	protected File[] files;

	public StatAnalyzer() {
		this.files = new File[] { new File(MonitorSettings
				.getLogFile(MonitorSettings.STAT_LOG_FILE)) };
	}

	public StatAnalyzer(Date date) {
		this.files = new File[] { new File(MonitorSettings
				.getLogFile(MonitorSettings.STAT_LOG_FILE)
				+ new SimpleDateFormat(MonitorSettings.DATE_STYLE).format(date)) };
	}

	public StatAnalyzer(Date[] dates) {
		this.files = new File[dates.length];
		for (int i = 0; i < dates.length; i++)
			this.files[i] = new File(MonitorSettings
					.getLogFile(MonitorSettings.STAT_LOG_FILE)
					+ new SimpleDateFormat(MonitorSettings.DATE_STYLE)
							.format(dates[i]));
	}

	public StatAnalyzer(Date start, Date end) {
		this(start, end, false);
	}

	public StatAnalyzer(Date start, Date end, boolean excludeEnd) {
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
			this.files[i] = new File(MonitorSettings
					.getLogFile(MonitorSettings.STAT_LOG_FILE)
					+ new SimpleDateFormat(MonitorSettings.DATE_STYLE)
							.format(cal.getTime()));
		}
	}

	public StatAnalyzer(File file) {
		this.files = new File[] { file };
	}

	public StatAnalyzer(File... files) {
		this.files = files;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ironrhino.core.monitor.analysis.Analyzer#analyze()
	 */
	public void analyze() {
		preAnalyze();
		String line = null;
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			for (File file : files) {
				fis = new FileInputStream(file);
				br = new BufferedReader(new InputStreamReader(fis,
						MonitorSettings.ENCODING));
				while (StringUtils.isNotBlank((line = br.readLine())))
					if (StringUtils.isNotBlank(line))
						processLine(line);
			}
			postAnalyze();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (br != null)
					br.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	private void processLine(String line) {
		String[] array = line.split("\\|");
		try {
			Key key = Key.fromString(array[0]);
			Value value = Value.fromString(array[1]);
			Date date;
			if (StringUtils.isNumeric(array[2])) {
				date = new Date(Long.valueOf(array[2]));
			} else {
				date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(array[2]);
			}
			process(key, value, date);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	protected abstract void process(Key key, Value value, Date date);

	protected void preAnalyze() {

	}

	protected void postAnalyze() {

	}

}