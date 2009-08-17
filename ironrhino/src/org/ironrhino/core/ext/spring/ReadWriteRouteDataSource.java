package org.ironrhino.core.ext.spring;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.ironrhino.core.lb.Policy;
import org.ironrhino.core.lb.PolicyFactory;
import org.ironrhino.core.lb.RoundRobinPolicyFactory;
import org.ironrhino.core.lb.TargetWrapper;
import org.ironrhino.core.lb.UsableChecker;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

public class ReadWriteRouteDataSource extends AbstractDataSource {

	private static ThreadLocal<Boolean> readonly = new ThreadLocal<Boolean>();

	private DataSource masterDataSource;

	private Map<DataSource, Integer> slaveDataSources;

	private Policy<DataSource> policy;

	public static void setReadonly(boolean bl) {
		readonly.set(bl);
	}

	public static boolean isReadonly() {
		Boolean bl = readonly.get();
		return bl != null && bl.booleanValue();
	}

	public void setMasterDataSource(DataSource masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public void setSlaveDataSources(Map<DataSource, Integer> slaveDataSources) {
		this.slaveDataSources = slaveDataSources;
	}

	@PostConstruct
	public void afterPropertiesSet() {
		Assert.notNull(masterDataSource);
		if (slaveDataSources != null && slaveDataSources.size() > 0) {
			PolicyFactory<DataSource> pf = new RoundRobinPolicyFactory<DataSource>();
			policy = pf.getPolicy(slaveDataSources,
					new UsableChecker<DataSource>() {
						public boolean isUsable(
								TargetWrapper<DataSource> targetWrapper) {
							DataSource ds = targetWrapper.getTarget();
							// TODO check usable
							return ds != null;
						}
					});
		}
	}

	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}

	protected DataSource determineTargetDataSource() {
		if (isReadonly() && policy != null)
			return policy.pick();
		return masterDataSource;
	}

	public static void main(String... strings) {
		setReadonly(true);
		System.out.println(isReadonly());
	}

}