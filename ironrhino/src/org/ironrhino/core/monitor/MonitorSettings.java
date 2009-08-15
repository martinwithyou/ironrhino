package org.ironrhino.core.monitor;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.util.DateUtils;

public class MonitorSettings {

	public static final String WRITETHREAD_NAME = "MONITOR-WRITE";

	public static final String ENCODING = "UTF-8";

	public static final String DATE_STYLE = "'.'yyyy-MM-dd";

	public static final String LAYOUT = "%m%n";

	public static final String TOKEN = "|";

	public static final String FILE_DIRECTORY = "logs";

	public static final String STAT_LOG_FILE = "stat.log";

	public static final String SYSTEM_LOG_FILE = "system.log";

	private static final Log log = LogFactory.getLog(MonitorSettings.class);

	private static long intervalUnit = 10; // senconds

	private static long systemIntervalMultiple = 10; // system.log
	// SYSTEM_INTERVAL_MULTIPLE*INTERVAL_UNIT
	// senconds

	public static final String HOST;

	static {
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
		HOST = host;
	}

	public static long getIntervalUnit() {
		return intervalUnit;
	}

	public static void setIntervalUnit(long intervalUnit) {
		if (intervalUnit > 0)
			MonitorSettings.intervalUnit = intervalUnit;
	}

	public static long getSystemIntervalMultiple() {
		return systemIntervalMultiple;
	}

	public static void setSystemIntervalMultiple(long systemIntervalMultiple) {
		if (systemIntervalMultiple > 0)
			MonitorSettings.systemIntervalMultiple = systemIntervalMultiple;
	}

	public static String getLogFile(String logfile) {
		File dir = new File(System.getProperty("user.home"), FILE_DIRECTORY);
		if (!dir.exists() && dir.mkdirs())
			log.error("mkdir error:" + dir.getAbsolutePath());
		return new File(dir, HOST + "_" + logfile).getAbsolutePath();
	}

	public static boolean hasLogFile(Date date) {
		return new File(MonitorSettings
				.getLogFile(MonitorSettings.STAT_LOG_FILE)
				+ (DateUtils.isToday(date) ? "" : new SimpleDateFormat(
						MonitorSettings.DATE_STYLE).format(date))).exists();
	}
}
