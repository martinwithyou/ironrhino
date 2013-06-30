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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultWatcher implements Watcher, ChildrenCallback, DataCallback,
		ApplicationContextAware {

	private ZooKeeper zooKeeper;

	private Collection<WatchedEventListener> eventListeners;

	public void injectZooKeeper(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	@Override
	public void process(WatchedEvent event) {
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
		for (WatchedEventListener listener : eventListeners) {
			if (listener.supports(event.getPath())) {
				if (event.getType() == Event.EventType.NodeCreated) {
					listener.onNodeCreated(event.getPath());
				} else if (event.getType() == Event.EventType.NodeDeleted) {
					listener.onNodeDeleted(event.getPath());
				} else if (event.getType() == Event.EventType.NodeDataChanged) {
					zooKeeper.getData(event.getPath(), true, this, null);
				} else if (event.getType() == Event.EventType.NodeChildrenChanged) {
					zooKeeper.getChildren(event.getPath(), true, this, null);
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
		for (WatchedEventListener listener : eventListeners) {
			if (listener.supports(path)) {
				listener.onNodeChildrenChanged(path, children);
			}
		}
	}

	@Override
	public void processResult(int rc, String path, Object ctx, byte[] data,
			Stat stat) {
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
		for (WatchedEventListener listener : eventListeners) {
			if (listener.supports(path)) {
				listener.onNodeDataChanged(path, data);
			}
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		eventListeners = ctx.getBeansOfType(WatchedEventListener.class)
				.values();

	}

}
