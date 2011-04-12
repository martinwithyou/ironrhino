package org.ironrhino.core.remoting.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.JsonUtils;

public class ZookeeperServiceRegistry extends AbstractServiceRegistry implements
		Watcher, ChildrenCallback {

	private ExecutorService executorService;

	private ZooKeeper zooKeeper;

	// private String connectString = "localhost:2181/remoting";
	private String connectString;

	private int sessionTimeout = 3000;

	private int maxRetryTimes = 5;

	private Map<String, String> discoveredServices = new HashMap<String, String>();

	private boolean ready;

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
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
			if (StringUtils.isNotBlank(connectString))
				zooKeeper = new ZooKeeper(connectString, sessionTimeout, this);
		} catch (IOException e) {
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
				doWriteDiscoveredServices(host, services, 3);

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
			List<String> children = zooKeeper.getChildren("/" + serviceName,
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
		String node = new StringBuilder().append('/').append(host).toString();
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
		String node = new StringBuilder().append('/').append(serviceName)
				.append('/').append(host).toString();
		byte[] data = "".getBytes();
		try {
			zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			onRegister(serviceName, host);
		} catch (NoNodeException e) {
			String parentNode = new StringBuilder().append('/')
					.append(serviceName).toString();
			try {
				zooKeeper.create(parentNode, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			} catch (Exception e1) {
				log.info(e1.getMessage(), e1);
				try {
					Thread.sleep(2000);
					zooKeeper.create(parentNode, data,
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				} catch (Exception ie) {
				}
			}
			doRegister(serviceName, host, retryTimes + 1);
		} catch (Exception e1) {
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
				String serviceName = path.substring(1);
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
		String serviceName = path.substring(1);
		importServices.put(serviceName, children);
	}

}
