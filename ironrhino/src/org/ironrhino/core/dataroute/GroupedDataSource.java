package org.ironrhino.core.dataroute;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Monitor;
import org.ironrhino.core.util.RoundRobin;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

public class GroupedDataSource extends AbstractDataSource implements
		BeanNameAware {

	private static Log log = LogFactory.getLog(GroupedDataSource.class);

	private int maxRetryTimes = 3;

	// inject starts
	private String masterName;

	private Map<String, Integer> writeSlaveNames;

	private Map<String, Integer> readSlaveNames;

	// inject end

	private String groupName;

	private DataSource master;

	private Map<String, DataSource> writeSlaves = new HashMap<String, DataSource>();

	private Map<String, DataSource> readSlaves = new HashMap<String, DataSource>();

	private RoundRobin<String> readRoundRobin;

	private RoundRobin<String> writeRoundRobin;

	@Autowired
	private BeanFactory beanFactory;

	@Override
	public void setBeanName(String beanName) {
		this.groupName = beanName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public void setReadSlaveNames(Map<String, Integer> readSlaveNames) {
		this.readSlaveNames = readSlaveNames;
	}

	public void setWriteSlaveNames(Map<String, Integer> writeSlaveNames) {
		this.writeSlaveNames = writeSlaveNames;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(masterName);
		master = (DataSource) beanFactory.getBean(masterName);
		if (writeSlaveNames != null && writeSlaveNames.size() > 0) {
			for (String name : writeSlaveNames.keySet())
				writeSlaves.put(name, (DataSource) beanFactory.getBean(name));
			writeSlaves.put(masterName, master);
			writeRoundRobin = new RoundRobin(writeSlaveNames,
					new RoundRobin.UsableChecker<String>() {
						public boolean isUsable(String target) {
							DataSource ds = writeSlaves.get(target);
							return ds != null;
						}
					});
		}
		if (readSlaveNames != null && readSlaveNames.size() > 0) {
			for (String name : readSlaveNames.keySet())
				readSlaves.put(name, (DataSource) beanFactory.getBean(name));
			readRoundRobin = new RoundRobin(readSlaveNames,
					new RoundRobin.UsableChecker<String>() {
						public boolean isUsable(String target) {
							DataSource ds = readSlaves.get(target);
							return ds != null;
						}
					});
		}
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		return getConnection(username, password, 0);
	}

	public Connection getConnection(String username, String password,
			int retryTimes) throws SQLException {
		DataSource ds = null;
		String dbname = null;
		if (DataRouteContext.isReadonly() && readRoundRobin != null) {
			dbname = readRoundRobin.pick();
			ds = readSlaves.get(dbname);
		}
		if (ds == null && writeRoundRobin != null) {
			dbname = writeRoundRobin.pick();
			ds = writeSlaves.get(dbname);
		}
		if (ds == null) {
			dbname = masterName;
			ds = master;
		}
		try {
			Connection conn = (username == null) ? ds.getConnection() : ds
					.getConnection(username, password);

			Monitor
					.add(new Key("dataroute", true, groupName, dbname,
							"success"));
			return conn;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			// retry
			Monitor
					.add(new Key("dataroute", true, groupName, dbname, "failed"));
			if (retryTimes == maxRetryTimes)
				return null;
			else
				return getConnection(username, password, retryTimes + 1);

		}
	}

	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

}