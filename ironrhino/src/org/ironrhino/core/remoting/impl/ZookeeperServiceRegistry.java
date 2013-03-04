package org.ironrhino.core.remoting.impl;

import static org.ironrhino.core.metadata.Profiles.CLUSTER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ironrhino.core.event.InstanceLifecycleEvent;
import org.ironrhino.core.event.InstanceShutdownEvent;
import org.ironrhino.core.remoting.ExportServicesEvent;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.zookeeper.WatchedEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

@Singleton
@Named("serviceRegistry")
@Profile(CLUSTER)
public class ZookeeperServiceRegistry extends AbstractServiceRegistry implements
		WatchedEventListener {

	public static final String DEFAULT_ZOOKEEPER_PATH = "/remoting";

	@Autowired(required = false)
	private ExecutorService executorService;

	@Inject
	private ZooKeeper zooKeeper;

	@Value("${serviceRegistry.zooKeeperPath:" + DEFAULT_ZOOKEEPER_PATH + "}")
	private String zooKeeperPath = DEFAULT_ZOOKEEPER_PATH;

	@Value("${serviceRegistry.maxRetryTimes:5}")
	private int maxRetryTimes = 5;

	private Map<String, String> discoveredServices = new HashMap<String, String>();

	private boolean ready;

	@Override
	public void prepare() {
		try {
			Stat stat = zooKeeper.exists(zooKeeperPath, false);
			if (stat == null)
				zooKeeper.create(zooKeeperPath, null,
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@Override
	public void onReady() {
		writeDiscoveredServices();
		ready = true;
	}

	@Override
	protected void lookup(String serviceName) {
		doLookup(serviceName, 3);
	}

	public void doRegister(String serviceName, String host) {
		doRegister(serviceName, host, maxRetryTimes);
	}

	@Override
	protected void onDiscover(String serviceName, String host) {
		super.onDiscover(serviceName, host);
		discoveredServices.put(serviceName, host);
		if (ready)
			writeDiscoveredServices();
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
		if (executorService != null)
			executorService.execute(runnable);
		else
			runnable.run();
	}

	private void doRegister(String serviceName, String host, int retryTimes) {
		retryTimes--;
		if (retryTimes < -1) {
			log.error("error register " + serviceName + "@" + host);
			return;
		}
		String node = new StringBuilder().append(zooKeeperPath).append("/")
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

	private void doLookup(String serviceName, int retryTimes) {
		retryTimes--;
		try {
			List<String> children = zooKeeper.getChildren(new StringBuilder(
					zooKeeperPath).append("/").append(serviceName).toString(),
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
		String node = new StringBuilder().append(zooKeeperPath).append("/")
				.append(host).toString();
		byte[] data = services.getBytes();
		try {
			Stat stat = zooKeeper.exists(node, false);
			if (stat == null) {
				zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL);
			} else {
				zooKeeper.setData(node, data, stat.getVersion());
			}
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

	@Override
	public boolean supports(String path) {
		if (path != null && path.startsWith(zooKeeperPath)) {
			String serviceName = path.substring(zooKeeperPath.length() + 1);
			return importServices.containsKey(serviceName);
		}
		return false;
	}

	@Override
	public void onNodeChildrenChanged(String path, List<String> children) {
		String serviceName = path.substring(zooKeeperPath.length() + 1);
		importServices.put(serviceName, children);
	}

	@Override
	public void onNodeCreated(String path) {

	}

	@Override
	public void onNodeDeleted(String path) {

	}

	@Override
	public void onNodeDataChanged(String path, byte[] data) {

	}

	@Override
	public void onApplicationEvent(InstanceLifecycleEvent event) {
		if (event instanceof ExportServicesEvent
				|| event instanceof InstanceShutdownEvent)
			// zookeeper has NodeChildrenChanged
			return;
		else
			super.onApplicationEvent(event);
	}

}