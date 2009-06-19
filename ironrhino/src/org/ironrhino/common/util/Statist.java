package org.ironrhino.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Statist {

	public static final long INTERVAL = 60000;

	private static Log log = LogFactory.getLog(Statist.class);
	private static Lock startLock = new ReentrantLock();
	private static Lock lock = new ReentrantLock();
	private static Condition condition = lock.newCondition();
	private static Thread writeThread = newWriteThread();
	private static Map<String, AtomicLong> data = new ConcurrentHashMap<String, AtomicLong>();

	static {
		writeThread.start();
	}

	private static Thread newWriteThread() {
		return new Thread(new Runnable() {
			public void run() {
				while (true) {
					lock.lock();
					try {
						condition.await(INTERVAL, TimeUnit.MILLISECONDS);
						doWrite();
					} catch (Exception e) {
					} finally {
						lock.unlock();
					}
				}
			}
		});
	}

	private static void runWriteThread() {
		startLock.lock();
		try {
			writeThread.interrupt();
			writeThread = newWriteThread();
			writeThread.start();
		} finally {
			startLock.unlock();
		}
	}

	private static void doWrite() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis()).append("::::");
		for (String label : data.keySet()) {
			if (data.get(label).longValue() > 0) {
				long c = data.get(label).longValue();
				data.get(label).addAndGet(-c);
				sb.append(label).append("=").append(c).append(";");
			}
		}
		log.info(sb);
	}

	public static long add(String label, long count) {
		long c = count;
		if (!data.containsKey(label))
			data.put(label, new AtomicLong(count));
		else
			c = data.get(label).addAndGet(count);
		if (!writeThread.isAlive())
			runWriteThread();
		return c;
	}

	public static long add(String label) {
		return add(label, 1);
	}
}
