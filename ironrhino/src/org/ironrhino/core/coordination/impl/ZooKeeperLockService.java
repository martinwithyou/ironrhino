package org.ironrhino.core.coordination.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.recipes.lock.WriteLock;
import org.ironrhino.core.coordination.LockService;
import org.ironrhino.core.metadata.ClusterProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("lockService")
@ClusterProfile
public class ZooKeeperLockService implements LockService {

	public static final String DEFAULT_ZOOKEEPER_PATH = "/lock";

	protected Logger log = LoggerFactory.getLogger(getClass());

	private ZooKeeper zooKeeper;

	private String zooKeeperPath = DEFAULT_ZOOKEEPER_PATH;

	private Map<String, WriteLock> locks = new ConcurrentHashMap<String, WriteLock>();

	public void setZooKeeperPath(String zooKeeperPath) {
		this.zooKeeperPath = zooKeeperPath;
	}

	public void setZooKeeper(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	@Override
	public boolean tryLock(String name) {
		WriteLock lock = locks.get(name);
		if (lock == null)
			synchronized (name.intern()) {
				lock = locks.get(name);
				if (lock == null) {
					lock = new WriteLock(zooKeeper, zooKeeperPath + "/" + name,
							ZooDefs.Ids.OPEN_ACL_UNSAFE);
					locks.put(name, lock);
				}
			}
		try {
			return lock.lock() && lock.isOwner();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean tryLock(String name, long timeout, TimeUnit unit) {
		WriteLock lock = locks.get(name);
		if (lock == null)
			synchronized (name.intern()) {
				lock = locks.get(name);
				if (lock == null) {
					lock = new WriteLock(zooKeeper, zooKeeperPath + "/" + name,
							ZooDefs.Ids.OPEN_ACL_UNSAFE);
					locks.put(name, lock);
				}
			}
		try {
			if (!lock.lock())
				return false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		long millisTimeout = unit.toMillis(timeout);
		long start = System.currentTimeMillis();
		while (!lock.isOwner()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if ((System.currentTimeMillis() - start) >= millisTimeout)
				break;
		}
		return lock.isOwner();
	}

	@Override
	public void lock(String name) {
		WriteLock lock = locks.get(name);
		if (lock == null)
			synchronized (name.intern()) {
				lock = locks.get(name);
				if (lock == null) {
					lock = new WriteLock(zooKeeper, zooKeeperPath + "/" + name,
							ZooDefs.Ids.OPEN_ACL_UNSAFE);
					locks.put(name, lock);
				}
			}
		try {
			if (!lock.lock()) {
				throw new Exception("execute lock operation failed");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		while (!lock.isOwner()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void unlock(String name) {
		WriteLock lock = locks.get(name);
		if (lock != null)
			lock.unlock();
	}

}
