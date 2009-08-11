package org.ironrhino.core.monitor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	public static final long INTERVAL_UNIT = 10; // 10senconds

	public static final String WRITETHREAD_NAME = "MONITOR-WRITE";

	public static final String ENCODING = "UTF-8";

	private static final String DATE_STYLE = "'.'yyyy-MM-dd";

	private static final String LAYOUT = "%m%n";

	public static final String TOKEN = "|";

	public static final String FILE_NAME = "monitor.log";

	public static String host = "localhost";

	private static Logger statLog = Logger.getLogger("Sampling");

	private static Log log = LogFactory.getLog(Monitor.class);

	private static Lock timerLock = new ReentrantLock();

	private static Lock mapLock = new ReentrantLock();

	private static Condition condition = timerLock.newCondition();

	private static Thread writeThread = null;

	private static Map<Key, Value> data = new ConcurrentHashMap<Key, Value>(50);

	static {
		// add log
		File dir = new File(System.getProperty("user.home"), "logs");
		if (!dir.exists() && dir.mkdirs())
			log.error("mkdir error:" + dir.getAbsolutePath());
		File file = new File(dir, FILE_NAME);
		PatternLayout layout = new PatternLayout(LAYOUT);
		FileAppender appender = null;
		try {
			appender = new DailyRollingFileAppender(layout, file
					.getAbsolutePath(), DATE_STYLE);
			appender.setAppend(true);
			appender.setEncoding(ENCODING);
			statLog.addAppender(appender);
			statLog.setLevel(Level.INFO);
			statLog.setAdditivity(false);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		// get host name
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
		runWriteThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				write(false);
			}
		});
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
						condition.await(INTERVAL_UNIT, TimeUnit.SECONDS);
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
					/ INTERVAL_UNIT > key.getInterval()))
					&& (value.getLong() > 0 || value.getDouble() > 0)) {
				key.setLastWriteTime(current);
				output(key, value);
				temp.put(key, new Value(value.getLong(), value.getDouble()));
			}
		}
		for (Map.Entry<Key, Value> entry : temp.entrySet()) {
			Key key = entry.getKey();
			Value value = data.get(key);
			value.add(-entry.getValue().getLong(), -entry.getValue()
					.getDouble());
		}
		printJvmInfo();
	}

	private static void output(Key key, Value value) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(TOKEN);
		sb.append(value);
		sb.append(TOKEN);
		sb.append(sysdate());
		sb.append(TOKEN);
		sb.append(host);
		statLog.info(sb.toString());
	}

	private static String sysdate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
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

	private static void printJvmInfo() {
		MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
		// MEMORY
		Key key = new Key("JVM", "MEMORY", "SITUATION");
		// Value value = new Value(memoryUsage.getMax());
		Value value = new Value(memoryUsage.getUsed());
		output(key, value);
		// CPU
		key = new Key("JVM", "CPU", "USAGE");
		value = new Value(threadMXBean.getCurrentThreadCpuTime());
		output(key, value);
		// THREAD
		key = new Key("JVM", "THREAD", "TOTAL");
		value = new Value(threadMXBean.getDaemonThreadCount());
		output(key, value);
	}

}
