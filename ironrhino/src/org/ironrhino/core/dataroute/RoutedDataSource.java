package org.ironrhino.core.dataroute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

public class RoutedDataSource extends AbstractDataSource {

	private GroupedDataSource mainGroup;

	private List<GroupedDataSource> groups;

	public void setMainGroup(GroupedDataSource mainGroup) {
		this.mainGroup = mainGroup;
	}

	public void setGroups(List<GroupedDataSource> groups) {
		this.groups = groups;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(mainGroup);
		if (groups != null)
			groups.add(0, mainGroup);
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		DataSource ds = null;
		String groupName = DataRouteContext.getGroupName();
		if (groupName != null) {
			for (GroupedDataSource d : groups) {
				if (groupName.equals(d.getGroupName())) {
					ds = d;
					break;
				}
			}
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