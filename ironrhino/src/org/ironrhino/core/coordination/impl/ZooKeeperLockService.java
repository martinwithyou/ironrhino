package org.ironrhino.core.coordination.impl;

import static org.ironrhino.core.metadata.Profiles.CLUSTER;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.recipes.lock.WriteLock;
import org.ironrhino.core.coordination.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

@Singleton
@Named("lockService")
@Profile(CLUSTER)
public class ZooKeeperLockService implements LockService {

	public static final String DEFAULT_ZOOKEEPER_PATH = "/lock";

	protected Logger log = LoggerFactory.getLogger(getClass());

	private ZooKeeper zooKeeper;

	private String zooKeeperPath = DEFAULT_ZOOKEEPER_PATH;

	private ConcurrentHashMap<String, WriteLock> locks = new ConcurrentHashMap<String, WriteLock>();

	public void setZooKeeperPath(String zooKeeperPath) {
		this.zooKeeperPath = zooKeeperPath;
	}

	public void setZooKeeper(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	@Override
	public boolean tryLock(String name) {
		WriteLock lock = locks.get(name);
		if (lock == null) {
			locks.putIfAbsent(name, new WriteLock(zooKeeper, zooKeeperPath
					+ "/" + name, ZooDefs.Ids.OPEN_ACL_UNSAFE));
			lock = locks.get(name);
		}
		try {
			return lock.lock() && lock.isOwner();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean tryLock(String name, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (timeout <= 0)
			return tryLock(name);
		WriteLock lock = locks.get(name);
		if (lock == null) {
			locks.putIfAbsent(name, new WriteLock(zooKeeper, zooKeeperPath
					+ "/" + name, ZooDefs.Ids.OPEN_ACL_UNSAFE));
			lock = locks.get(name);
		}
		try {
			if (!lock.lock())
				return false;
		} catch (KeeperException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		long millisTimeout = unit.toMillis(timeout);
		long start = System.currentTimeMillis();
		while (!lock.isOwner()) {
			Thread.sleep(100);
			if ((System.currentTimeMillis() - start) >= millisTimeout)
				break;
		}
		return lock.isOwner();
	}

	@Override
	public void lock(String name) {
		WriteLock lock = locks.get(name);
		if (lock == null) {
			locks.putIfAbsent(name, new WriteLock(zooKeeper, zooKeeperPath
					+ "/" + name, ZooDefs.Ids.OPEN_ACL_UNSAFE));
			lock = locks.get(name);
		}
		try {
			if (!lock.lock()) {
				throw new RuntimeException("execute lock operation failed");
			}
		} catch (KeeperException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("execute lock operation failed");
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("execute lock operation failed");
		}
		while (!lock.isOwner()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException("execute lock operation failed");
			}
		}
	}

	@Override
	public void unlock(String name) {
		WriteLock lock = locks.get(name);
		if (lock == null)
			throw new IllegalArgumentException("Lock '" + name
					+ " ' doesn't exists");
		lock.unlock();
	}

}
