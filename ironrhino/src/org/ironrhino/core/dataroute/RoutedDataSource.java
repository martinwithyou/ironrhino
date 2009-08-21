package org.ironrhino.core.dataroute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

public class RoutedDataSource extends AbstractDataSource {

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

}