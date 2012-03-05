package org.ironrhino.core.coordination.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.coordination.LockService;

@Singleton
@Named("lockService")
public class StandaloneLockService implements LockService {

	private Map<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

	@Override
	public boolean tryLock(String name) {
		Lock lock = locks.get(name);
		if (lock == null)
			synchronized (name.intern()) {
				lock = locks.get(name);
				if (lock == null) {
					lock = new ReentrantLock();
					locks.put(name, lock);
				}
			}
		return lock.tryLock();
	}

	@Override
	public boolean tryLock(String name, long timeout, TimeUnit unit) {
		Lock lock = locks.get(name);
		if (lock == null)
			synchronized (name.intern()) {
				lock = locks.get(name);
				if (lock == null) {
					lock = new ReentrantLock();
					locks.put(name, lock);
				}
			}
		try {
			return lock.tryLock(timeout, unit);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public void lock(String name) {
		Lock lock = locks.get(name);
		if (lock == null)
			synchronized (name.intern()) {
				lock = locks.get(name);
				if (lock == null) {
					lock = new ReentrantLock();
					locks.put(name, lock);
				}
			}
		lock.lock();
	}

	@Override
	public void unlock(String name) {
		Lock lock = locks.get(name);
		if (lock != null)
			lock.unlock();
	}

}
