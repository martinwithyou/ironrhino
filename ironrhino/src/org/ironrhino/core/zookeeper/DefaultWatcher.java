package org.ironrhino.core.zookeeper;

import java.util.Collection;
import java.util.List;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class DefaultWatcher implements Watcher, ChildrenCallback, DataCallback {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired(required = false)
	private Collection<WatchedEventListener> eventListeners;

	private ZooKeeper zooKeeper;

	@Override
	public void process(WatchedEvent event) {
		if (zooKeeper == null)
			zooKeeper = applicationContext.getBean(ZooKeeper.class);
		if (event.getType() == Event.EventType.None) {
			switch (event.getState()) {
			case SyncConnected:
				return;
			case Disconnected:
				return;
			case Expired:
				return;
			default:
				return;
			}
		}
		if (eventListeners != null)
			for (WatchedEventListener listener : eventListeners) {
				if (listener.supports(event.getPath())) {
					if (event.getType() == Event.EventType.NodeCreated) {
						listener.onNodeCreated(event.getPath());
					} else if (event.getType() == Event.EventType.NodeDeleted) {
						listener.onNodeDeleted(event.getPath());
					} else if (event.getType() == Event.EventType.NodeDataChanged) {
						zooKeeper.getData(event.getPath(), true, this, null);
					} else if (event.getType() == Event.EventType.NodeChildrenChanged) {
						zooKeeper
								.getChildren(event.getPath(), true, this, null);
					}
				}
			}

	}

	@Override
	public void processResult(int rc, String path, Object ctx,
			List<String> children) {
		if (zooKeeper == null)
			zooKeeper = applicationContext.getBean(ZooKeeper.class);
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
		if (eventListeners != null)
			for (WatchedEventListener listener : eventListeners) {
				if (listener.supports(path)) {
					listener.onNodeChildrenChanged(path, children);
				}
			}
	}

	@Override
	public void processResult(int rc, String path, Object ctx, byte[] data,
			Stat stat) {
		if (zooKeeper == null)
			zooKeeper = applicationContext.getBean(ZooKeeper.class);
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
			zooKeeper.getData(path, true, this, null);
			return;
		}
		if (eventListeners != null)
			for (WatchedEventListener listener : eventListeners) {
				if (listener.supports(path)) {
					listener.onNodeDataChanged(path, data);
				}
			}

	}

}
