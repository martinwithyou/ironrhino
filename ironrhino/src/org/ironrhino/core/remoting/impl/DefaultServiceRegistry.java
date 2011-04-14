package org.ironrhino.core.remoting.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.JsonUtils;

public class DefaultServiceRegistry extends AbstractServiceRegistry implements
		Watcher, ChildrenCallback {

	public static final String DEFAULT_ZOOKEEPER_PATH = "/remoting";

	private ExecutorService executorService;

	private ZooKeeper zooKeeper;

	// private String zookeeperConnectString = "localhost:2181";
	private String zookeeperConnectString;

	private String zookeeperPath = DEFAULT_ZOOKEEPER_PATH;

	private int zookeeperSessionTimeout = 10000;

	private int maxRetryTimes = 5;

	private Map<String, String> discoveredServices = new HashMap<String, String>();

	private boolean ready;

	public void setZookeeperConnectString(String zookeeperConnectString) {
		this.zookeeperConnectString = zookeeperConnectString;
	}

	public void setZookeeperPath(String zookeeperPath) {
		this.zookeeperPath = zookeeperPath;
	}

	public void setZookeeperSessionTimeout(int zookeeperSessionTimeout) {
		this.zookeeperSessionTimeout = zookeeperSessionTimeout;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void prepare() {
		try {
			if (StringUtils.isNotBlank(zookeeperConnectString)) {
				zooKeeper = new ZooKeeper(zookeeperConnectString,
						zookeeperSessionTimeout, this);
				Stat stat = zooKeeper.exists(zookeeperPath, false);
				if (stat == null)
					zooKeeper.create(zookeeperPath, null,
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@Override
	protected void onDiscover(String serviceName, String host) {
		super.onDiscover(serviceName, host);
		discoveredServices.put(serviceName, host);
		if (ready) {
			writeDiscoveredServices();
		}
	}

	@Override
	public void onReady() {
		writeDiscoveredServices();
		ready = true;
	}

	@PreDestroy
	public void destroy() {
		if (zooKeeper != null)
			try {
				zooKeeper.close();
			} catch (InterruptedException e) {
			}
	}

	public void register(String serviceName) {
		if (zooKeeper != null)
			doRegister(serviceName, AppInfo.getHostAddress(), maxRetryTimes);
	}

	@Override
	protected void lookup(String serviceName) {
		if (zooKeeper != null) {
			doLookup(serviceName, 3);
		}
	}

	protected void writeDiscoveredServices() {
		if (discoveredServices.size() == 0)
			return;
		final String host = AppInfo.getHostAddress();
		final String services = JsonUtils.toJson(discoveredServices);
		Runnable runnable = new Runnable() {
			public void run() {
				doWriteDiscoveredServices(host, services, maxRetryTimes);
			}
		};
		if (executorService != null) {
			executorService.execute(runnable);
		} else {
			new Thread(runnable).start();
		}
	}

	private void doLookup(String serviceName, int retryTimes) {
		retryTimes--;
		try {
			List<String> children = zooKeeper.getChildren(new StringBuilder(
					zookeeperPath).append("/").append(serviceName).toString(),
					true);
			if (children != null && children.size() > 0)
				importServices.put(serviceName, children);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (retryTimes < 0)
				return;
			doLookup(serviceName, retryTimes);
		}
	}

	private void doWriteDiscoveredServices(String host, String services,
			int retryTimes) {
		retryTimes--;
		String node = new StringBuilder().append(zookeeperPath).append("/")
				.append(host).toString();
		byte[] data = services.getBytes();
		try {
			zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
		} catch (Exception e1) {
			if (retryTimes < 0) {
				log.error("error writeDiscoveredServices for " + node, e1);
				return;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			doWriteDiscoveredServices(host, services, retryTimes);
		}
	}

	private void doRegister(String serviceName, String host, int retryTimes) {
		retryTimes--;
		if (retryTimes < -1) {
			log.error("error register " + serviceName + "@" + host);
			return;
		}
		String node = new StringBuilder().append(zookeeperPath).append("/")
				.append(serviceName).toString();
		try {
			Stat stat = zooKeeper.exists(node, false);
			if (stat == null)
				zooKeeper.create(node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			node = new StringBuilder(node).append('/').append(host).toString();
			zooKeeper.create(node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			onRegister(serviceName, host);
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			doRegister(serviceName, host, retryTimes);
		}
	}

	public void process(WatchedEvent event) {
		if (event.getType() == Event.EventType.None) {
			switch (event.getState()) {
			case SyncConnected:
				return;
			case Expired:
				return;
			}
		} else if (event.getType() == Event.EventType.NodeChildrenChanged) {
			String path = event.getPath();
			if (path != null) {
				String serviceName = path.substring(zookeeperPath.length() + 1);
				if (importServices.containsKey(serviceName)) {
					zooKeeper.getChildren(path, true, this, null);
				}
			}
		}
	}

	public void processResult(int rc, String path, Object ctx,
			List<String> children) {
		Code code = Code.get(rc);
		switch (code) {
		case OK:
			break;
		case NONODE:
			return;
		case SESSIONEXPIRED:
		case NOAUTH:
			return;
		default:
			// Retry
			zooKeeper.getChildren(path, true, this, null);
			return;
		}
		String serviceName = path.substring(zookeeperPath.length() + 1);
		importServices.put(serviceName, children);
	}

}
