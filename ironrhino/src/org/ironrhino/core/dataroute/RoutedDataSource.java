package org.ironrhino.core.dataroute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

public class RoutedDataSource extends AbstractDataSource {

	private Log log = LogFactory.getLog(getClass());

	private DataSource mainGroup;

	private Map<String, DataSource> routingMap;

	// inject DataSource with groupName
	public void setRoutingMap(Map<String, DataSource> map) {
		if (routingMap != null)
			throw new IllegalArgumentException("already injected by groupList");
		this.routingMap = map;
	}

	// inject GroupedDataSource included groupName
	public void setGroups(List<GroupedDataSource> list) {
		if (routingMap != null)
			throw new IllegalArgumentException("already injected by routingMap");
		routingMap = new LinkedHashMap<String, DataSource>();
		for (GroupedDataSource gds : list)
			routingMap.put(gds.getGroupName(), gds);
		mainGroup = list.get(0);
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(routingMap);
		mainGroup = routingMap.values().iterator().next();
		Assert.notNull(mainGroup);
		startCheckThread();
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		DataSource ds = null;
		String groupName = DataRouteContext.getName();
		if (groupName != null) {
			ds = routingMap.get(groupName);
		} else {
			ds = mainGroup;
		}
		if (ds == null)
			throw new IllegalArgumentException("group name '" + groupName
					+ "' not found");
		return ds.getConnection(username, password);
	}

	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	private Thread checkThread;
	private Lock timerLock = new ReentrantLock();
	private Condition condition = timerLock.newCondition();

	private void startCheckThread() {
		checkThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					timerLock.lock();
					try {
						if (condition.await(60, TimeUnit.SECONDS))
							log.debug("await returns true");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						timerLock.unlock();
					}
					if (mainGroup instanceof GroupedDataSource)
						((GroupedDataSource) mainGroup).checkDeadDataSources();
					for (DataSource ds : routingMap.values())
						if (ds instanceof GroupedDataSource)
							((GroupedDataSource) ds).checkDeadDataSources();
				}
			}
		}, "CHECK_DEAD_DATASOURCE");
		checkThread.start();
	}
}