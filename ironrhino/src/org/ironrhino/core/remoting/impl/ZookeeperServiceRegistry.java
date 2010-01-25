package org.ironrhino.core.remoting.impl;

import java.io.IOException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.ironrhino.core.util.AppInfo;

@Singleton
@Named("serviceRegistry")
public class ZookeeperServiceRegistry extends AbstractServiceRegistry implements
		Watcher, ChildrenCallback {

	private Log log = LogFactory.getLog(getClass());

	private ZooKeeper zooKeeper;

	// private String connectString = "localhost:2181/remoting";
	private String connectString;

	private int sessionTimeout = 3000;

	private int maxRetryTimes = 5;

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	@Override
	public void prepare() {
		try {
			if (connectString != null)
				zooKeeper = new ZooKeeper(connectString, sessionTimeout, this);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

	@PreDestroy
	public void destroy() {
		if (zooKeeper != null)
			try {
				zooKeeper.close();
			} catch (InterruptedException e) {
			}
	}

	@Override
	protected void register(String serviceName) {
		if (zooKeeper != null)
			register(serviceName, AppInfo.getHostAddress(), maxRetryTimes);
	}

	@Override
	protected void discover(String serviceName) {
		if (zooKeeper != null)
			zooKeeper.getChildren("/" + serviceName, true, this, null);
	}

	private void register(String serviceName, String address, int retryTimes) {
		retryTimes--;
		String node = new StringBuilder().append('/').append(serviceName)
				.append('/').append(address).toString();
		byte[] data = "".getBytes();
		try {

			zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			log.info("register service [" + serviceName + "@" + address + "]");
		} catch (NoNodeException e) {
			if (retryTimes < 0) {
				log.error("error creating node:" + node, e);
				return;
			}
			node = new StringBuilder().append('/').append(serviceName)
					.toString();
			try {
				zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				node = new StringBuilder().append('/').append(serviceName)
						.append('/').append(address).toString();
				zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL);
			} catch (Exception e1) {
				log.info(e1);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
				}
				register(serviceName, address, retryTimes);
			}
			log.info("register service [" + serviceName + "@" + address + "]");
		} catch (Exception e1) {
			if (retryTimes < 0) {
				log.error("error creating node:" + node, e1);
				return;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			register(serviceName, address, retryTimes);
		}
	}

	@Override
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

	@Override
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
