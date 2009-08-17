package org.ironrhino.core.ext.spring;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.lb.Policy;
import org.ironrhino.core.lb.PolicyFactory;
import org.ironrhino.core.lb.RoundRobinPolicyFactory;
import org.ironrhino.core.lb.TargetWrapper;
import org.ironrhino.core.lb.UsableChecker;
import org.ironrhino.core.monitor.Monitor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.AbstractDataSource;

public class ReadWriteRouteDataSource extends AbstractDataSource {

	public static final String DEFAULT_DATASOURCE_NAME = "defaultDataSource";
	public static final int DEFAULT_DATASOURCE_WEIGHT = 1;

	private Log log = LogFactory.getLog(this.getClass());

	private static ThreadLocal<Boolean> readonly = new ThreadLocal<Boolean>();

	private Policy<String> readPolicy;

	private Policy<String> writePolicy;

	@Autowired
	private BeanFactory beanFactory;

	private Map<String, Integer> writeDataSourceNames;

	private Map<String, Integer> readDataSourceNames;

	@Autowired
	@Qualifier(DEFAULT_DATASOURCE_NAME)
	private DataSource defaultDataSource;

	private Map<String, DataSource> writeDataSources = new HashMap<String, DataSource>();

	private Map<String, DataSource> readDataSources = new HashMap<String, DataSource>();

	public static void setReadonly(boolean bl) {
		readonly.set(bl);
	}

	public static boolean isReadonly() {
		Boolean bl = readonly.get();
		return bl != null && bl.booleanValue();
	}

	public void setWriteDataSourceNames(
			Map<String, Integer> writeDataSourceNames) {
		this.writeDataSourceNames = writeDataSourceNames;
	}

	public Map<String, DataSource> getWriteDataSources() {
		return writeDataSources;
	}

	public Map<String, DataSource> getReadDataSources() {
		return readDataSources;
	}

	public void setReadDataSourceNames(Map<String, Integer> readDataSourceNames) {
		this.readDataSourceNames = readDataSourceNames;
	}

	public void setDefaultDataSource(DataSource defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		PolicyFactory<String> pf = new RoundRobinPolicyFactory<String>();
		if (writeDataSourceNames != null && writeDataSourceNames.size() > 0) {
			writeDataSourceNames.put(DEFAULT_DATASOURCE_NAME,
					DEFAULT_DATASOURCE_WEIGHT);
			for (String name : writeDataSourceNames.keySet())
				writeDataSources.put(name, (DataSource) beanFactory
						.getBean(name));
			writePolicy = pf.getPolicy(writeDataSourceNames,
					new UsableChecker<String>() {
						public boolean isUsable(
								TargetWrapper<String> targetWrapper) {
							DataSource ds = writeDataSources.get(targetWrapper
									.getTarget());
							return ds != null;
						}
					});
		}
		if (readDataSourceNames != null && readDataSourceNames.size() > 0) {
			for (String name : readDataSourceNames.keySet())
				readDataSources.put(name, (DataSource) beanFactory
						.getBean(name));
			readPolicy = pf.getPolicy(readDataSourceNames,
					new UsableChecker<String>() {
						public boolean isUsable(
								TargetWrapper<String> targetWrapper) {
							DataSource ds = readDataSources.get(targetWrapper
									.getTarget());
							return ds != null;
						}
					});
		}
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		DataSource ds = null;
		String dbname = null;
		if (isReadonly() && readPolicy != null) {
			dbname = readPolicy.pick();
			if (dbname != null)
				ds = readDataSources.get(dbname);
		}
		if (ds == null) {
			if (writePolicy != null) {
				dbname = writePolicy.pick();
				if (dbname != null)
					ds = writeDataSources.get(dbname);
			}
			if (ds == null) {
				dbname = DEFAULT_DATASOURCE_NAME;
				ds = defaultDataSource;
			}
		}
		try {
			Connection conn = (username == null) ? ds.getConnection() : ds
					.getConnection(username, password);

			Monitor.add("dbconnection", dbname, "success");
			return conn;
		} catch (SQLException ex) {
			// retry once
			Monitor.add("dbconnection", dbname, "success");
			log.error(ds.toString(), ex);
			if (isReadonly() && readPolicy != null) {
				dbname = readPolicy.pick();
				ds = readDataSources.get(dbname);
			}
			if (ds == null) {
				if (writePolicy != null) {
					dbname = writePolicy.pick();
					ds = readDataSources.get(dbname);
				}
				if (ds == null) {
					dbname = DEFAULT_DATASOURCE_NAME;
					ds = defaultDataSource;
				}
			}
			Connection conn = (username == null) ? ds.getConnection() : ds
					.getConnection(username, password);
			Monitor.add("dbconnection", dbname, "success");
			return conn;
		}
	}

	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

}