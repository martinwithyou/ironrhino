package org.ironrhino.core.monitor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Monitor {

	public static final String WRITETHREAD_NAME = "MONITOR-WRITE";

	public static final String ENCODING = "UTF-8";

	public static final String DATE_STYLE = "'.'yyyy-MM-dd";

	public static final String LAYOUT = "%m%n";

	public static final String TOKEN = "|";

	public static final String FILE_DIRECTORY = "logs";

	public static final String STAT_LOG_FILE = "stat.log";

	public static final String SYSTEM_LOG_FILE = "system.log";

	private static final Logger statLogger = Logger.getLogger("Stat");

	private static final Logger systemLogger = Logger.getLogger("System");

	private static final Log log = LogFactory.getLog(Monitor.class);

	private static final Lock timerLock = new ReentrantLock();

	private static final Lock mapLock = new ReentrantLock();

	private static final Condition condition = timerLock.newCondition();

	private static final Map<Key, Value> data = new ConcurrentHashMap<Key, Value>(
			50);

	private static long intervalUnit = 10; // senconds

	private static long systemIntervalMultiple = 10; // system.log
	// SYSTEM_INTERVAL_MULTIPLE*INTERVAL_UNIT
	// senconds

	private static Thread writeThread = null;

	private static int currentSystemInterval;

	public static final String HOST;

	static {

		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
		HOST = host;
		PatternLayout layout = new PatternLayout(LAYOUT);
		FileAppender appender = null;
		try {
			appender = new DailyRollingFileAppender(layout,
					getLogFile(STAT_LOG_FILE), ENCODING, DATE_STYLE);
			appender.setAppend(true);
			// appender.setEncoding(ENCODING);//doesn't works
			statLogger.addAppender(appender);
			statLogger.setLevel(Level.INFO);
			statLogger.setAdditivity(false);

			appender = new DailyRollingFileAppender(layout,
					getLogFile(SYSTEM_LOG_FILE), ENCODING, DATE_STYLE);
			appender.setAppend(true);
			systemLogger.addAppender(appender);
			systemLogger.setLevel(Level.INFO);
			systemLogger.setAdditivity(false);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		runWriteThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				write(false);
			}
		});
	}

	public static long getIntervalUnit() {
		return intervalUnit;
	}

	public static void setIntervalUnit(long intervalUnit) {
		if (intervalUnit > 0)
			Monitor.intervalUnit = intervalUnit;
	}

	public static long getSystemIntervalMultiple() {
		return systemIntervalMultiple;
	}

	public static void setSystemIntervalMultiple(long systemIntervalMultiple) {
		if (systemIntervalMultiple > 0)
			Monitor.systemIntervalMultiple = systemIntervalMultiple;
	}

	public static String getLogFile(String logfile) {
		File dir = new File(System.getProperty("user.home"), FILE_DIRECTORY);
		if (!dir.exists() && dir.mkdirs())
			log.error("mkdir error:" + dir.getAbsolutePath());
		return new File(dir, HOST + "_" + logfile).getAbsolutePath();
	}

	private static void runWriteThread() {
		if (writeThread != null) {
			try {
				writeThread.interrupt();
			} catch (Exception e) {
				log.error("interrupt write thread error", e);
			}
		}
		writeThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						timerLock.lock();
						condition.await(intervalUnit, TimeUnit.SECONDS);
					} catch (Exception e) {
						log.error("wait error", e);
					} finally {
						timerLock.unlock();
					}
					Monitor.write();
				}
			}

		}, WRITETHREAD_NAME);
		writeThread.start();

	}

	private static void write() {
		write(true);
	}

	private static void write(boolean checkInterval) {
		Map<Key, Value> temp = new HashMap<Key, Value>(data.size());
		for (Map.Entry<Key, Value> entry : data.entrySet()) {
			long current = System.currentTimeMillis();
			Key key = entry.getKey();
			Value value = entry.getValue();
			if (((!checkInterval || (current - key.getLastWriteTime())
					/ intervalUnit > key.getInterval()))
					&& (value.getLong() > 0 || value.getDouble() > 0)) {
				key.setLastWriteTime(current);
				output(statLogger, key, value);
				temp.put(key, new Value(value.getLong(), value.getDouble()));
			}
		}
		for (Map.Entry<Key, Value> entry : temp.entrySet()) {
			Key key = entry.getKey();
			Value value = data.get(key);
			value.add(-entry.getValue().getLong(), -entry.getValue()
					.getDouble());
		}
		if (currentSystemInterval % systemIntervalMultiple == 0)
			printSystemInfo();
		currentSystemInterval++;
		if (currentSystemInterval > systemIntervalMultiple)
			currentSystemInterval -= systemIntervalMultiple;
	}

	private static void output(Logger logger, Key key, Value value) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(TOKEN);
		sb.append(value);
		sb.append(TOKEN);
		sb.append(sysdate());
		logger.info(sb.toString());
	}

	private static String sysdate() {
		return String.valueOf(System.currentTimeMillis());
	}

	private static Value getValue(Key key) {
		Value value = data.get(key);
		if (value == null) {
			mapLock.lock();
			try {
				value = data.get(key);
				if (value == null) {
					value = new Value(0);
					data.put(key, value);
				}
			} finally {
				mapLock.unlock();
			}
		}
		return value;
	}

	public static Number[] add(Key key, long c, double d) {
		if (!writeThread.isAlive())
			runWriteThread();
		Value value = getValue(key);
		return value.add(c, d);
	}

	public static long add(Key key, long c) {
		return (Long) add(key, c, 0)[0];
	}

	public static double add(Key key, double d) {
		return (Double) add(key, 0, d)[1];
	}

	public static long add(Key key) {
		return add(key, 1);
	}

	private static final MemoryMXBean memoryMXBean = ManagementFactory
			.getMemoryMXBean();
	private static final ThreadMXBean threadMXBean = ManagementFactory
			.getThreadMXBean();

	public static void printSystemInfo() {
		MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
		// MEMORY
		Key key = new Key("JVM", 0, false, "MEMORY", "SITUATION");
		Value value = new Value(memoryUsage.getMax(), memoryUsage.getUsed());
		output(systemLogger, key, value);
		// CPU
		key = new Key("JVM", 0, false, "CPU", "USAGE");
		value = new Value(threadMXBean.getCurrentThreadCpuTime(), threadMXBean
				.getCurrentThreadUserTime());
		output(systemLogger, key, value);
		// THREAD
		key = new Key("JVM", 0, false, "THREAD", "TOTAL");
		value = new Value(threadMXBean.getPeakThreadCount(), threadMXBean
				.getDaemonThreadCount());
		// TODO monitor disk usage
		output(systemLogger, key, value);
	}

}
