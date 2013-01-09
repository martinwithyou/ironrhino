package org.ironrhino.core.coordination.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.coordination.LockService;
import org.springframework.context.annotation.Profile;

@Singleton
@Named("lockService")
@Profile("default")
public class StandaloneLockService implements LockService {

	private ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

	@Override
	public boolean tryLock(String name) {
		Lock lock = locks.get(name);
		if (lock == null) {
			locks.putIfAbsent(name, new ReentrantLock());
			lock = locks.get(name);
		}
		return lock.tryLock();
	}

	@Override
	public boolean tryLock(String name, long timeout, TimeUnit unit)
			throws InterruptedException {
		Lock lock = locks.get(name);
		if (lock == null) {
			locks.putIfAbsent(name, new ReentrantLock());
			lock = locks.get(name);
		}
		return lock.tryLock(timeout, unit);
	}

	@Override
	public void lock(String name) {
		Lock lock = locks.get(name);
		if (lock == null) {
			locks.putIfAbsent(name, new ReentrantLock());
			lock = locks.get(name);
		}
		lock.lock();
	}

	@Override
	public void unlock(String name) {
		Lock lock = locks.get(name);
		if (lock == null)
			throw new IllegalArgumentException("Lock '" + name
					+ " ' doesn't exists");
		lock.unlock();
	}
}
