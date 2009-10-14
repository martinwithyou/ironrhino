package org.ironrhino.core.stat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
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

public class StatLog {

	private static final Logger statLogger = Logger.getLogger("Stat");

	private static final Logger systemLogger = Logger.getLogger("System");

	private static final Log log = LogFactory.getLog(StatLog.class);

	private static final Lock timerLock = new ReentrantLock();

	private static final Lock mapLock = new ReentrantLock();

	private static final Condition condition = timerLock.newCondition();

	private static final Map<Key, Value> data = new ConcurrentHashMap<Key, Value>(
			50);

	private static Thread writeThread;

	private static int currentSystemInterval;

	private static FileAppender statLogAppender;

	private static FileAppender systemLogAppender;

	static {
		PatternLayout layout = new PatternLayout(StatLogSettings.LAYOUT);
		FileAppender appender = null;
		try {
			appender = new DailyRollingFileAppender(layout, StatLogSettings
					.getLogFile(StatLogSettings.STAT_LOG_FILE_NAME),
					StatLogSettings.DATE_STYLE);
			appender.setAppend(true);
			appender.setEncoding(StatLogSettings.ENCODING);
			appender.activateOptions();
			statLogger.addAppender(appender);
			statLogger.setLevel(Level.INFO);
			statLogger.setAdditivity(false);

			appender = new DailyRollingFileAppender(layout, StatLogSettings
					.getLogFile(StatLogSettings.SYSTEM_LOG_FILE_NAME),
					StatLogSettings.DATE_STYLE);
			appender.setAppend(true);
			appender.setEncoding(StatLogSettings.ENCODING);
			appender.activateOptions();
			systemLogger.addAppender(appender);
			systemLogger.setLevel(Level.INFO);
			systemLogger.setAdditivity(false);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		startNewThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				write(false);
				if (systemLogAppender != null)
					systemLogAppender.close();
				if (systemLogAppender != null)
					statLogAppender.close();
			}
		});
	}

	private static void runWriteThread() {
		if (writeThread.isAlive())
			return;
		try {
			writeThread.interrupt();
		} catch (Exception e) {
			log.error("interrupt write thread error", e);
		}
		startNewThread();
	}

	private static void startNewThread() {
		writeThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					timerLock.lock();
					try {
						if (condition.await(StatLogSettings.getIntervalUnit(),
								TimeUnit.SECONDS))
							log.debug("await returns true");
					} catch (Exception e) {
						log.error("wait error", e);
					} finally {
						timerLock.unlock();
					}
					StatLog.write();
				}
			}

		}, StatLogSettings.WRITETHREAD_NAME);
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
					/ StatLogSettings.getIntervalUnit() > key
					.getIntervalMultiple()))
					&& (value.getLongValue() > 0 || value.getDoubleValue() > 0)) {
				key.setLastWriteTime(current);
				output(statLogger, key, value);
				temp.put(key, new Value(value.getLongValue(), value
						.getDoubleValue()));
			}
		}
		for (Map.Entry<Key, Value> entry : temp.entrySet()) {
			Key key = entry.getKey();
			Value value = data.get(key);
			value.add(-entry.getValue().getLongValue(), -entry.getValue()
					.getDoubleValue());
		}
		if (currentSystemInterval % StatLogSettings.getSystemIntervalMultiple() == 0)
			printSystemInfo();
		currentSystemInterval++;
		if (currentSystemInterval > StatLogSettings.getSystemIntervalMultiple())
			currentSystemInterval -= StatLogSettings
					.getSystemIntervalMultiple();
	}

	private static void output(Logger logger, Key key, Value value) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(StatLogSettings.TOKEN);
		sb.append(value);
		sb.append(StatLogSettings.TOKEN);
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

	public static long add(String... names) {
		return add(new Key(names), 1);
	}

	public static long add(Key key) {
		return add(key, 1);
	}

	private static final MemoryMXBean memoryMXBean = ManagementFactory
			.getMemoryMXBean();
	private static final ThreadMXBean threadMXBean = ManagementFactory
			.getThreadMXBean();

	public static void printSystemInfo() {
		try {
			MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
			// MEMORY
			Key key = new Key("JVM", 0, false, "MEMORY", "SITUATION");
			Value value = new Value(memoryUsage.getMax(), memoryUsage.getUsed());
			output(systemLogger, key, value);
			// CPU
			key = new Key("JVM", 0, false, "CPU", "USAGE");
			value = new Value(threadMXBean.getCurrentThreadCpuTime(),
					threadMXBean.getCurrentThreadUserTime());
			output(systemLogger, key, value);
			// THREAD
			key = new Key("JVM", 0, false, "THREAD", "TOTAL");
			value = new Value(threadMXBean.getPeakThreadCount(), threadMXBean
					.getDaemonThreadCount());
			// TODO monitor disk usage
			output(systemLogger, key, value);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

}
