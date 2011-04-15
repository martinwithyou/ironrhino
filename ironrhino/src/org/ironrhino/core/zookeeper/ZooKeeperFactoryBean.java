package org.ironrhino.core.zookeeper;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

@Singleton
@Named("zooKeeper")
public class ZooKeeperFactoryBean implements FactoryBean<ZooKeeper>,
		InitializingBean, DisposableBean {

	private ZooKeeper zooKeeper;

	private DefaultWatcher defaultWatcher;

	private String connectString;

	private int sessionTimeout = 10000;

	public void setDefaultWatcher(DefaultWatcher defaultWatcher) {
		this.defaultWatcher = defaultWatcher;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public void afterPropertiesSet() throws Exception {
		if (StringUtils.isNotBlank(connectString)) {
			zooKeeper = new ZooKeeper(connectString, sessionTimeout,
					defaultWatcher);
			defaultWatcher.injectZooKeeper(zooKeeper);
		}
		;
	}

	public void destroy() throws Exception {
		if (zooKeeper != null)
			zooKeeper.close();
	}

	public ZooKeeper getObject() throws Exception {
		return zooKeeper;
	}

	public Class<? extends ZooKeeper> getObjectType() {
		return ZooKeeper.class;
	}

	public boolean isSingleton() {
		return true;
	}
}