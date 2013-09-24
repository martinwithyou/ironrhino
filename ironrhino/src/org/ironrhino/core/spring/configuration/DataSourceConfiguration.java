package org.ironrhino.core.spring.configuration;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.jdbc.DatabaseProduct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jolbox.bonecp.BoneCPDataSource;

@Configuration
public class DataSourceConfiguration {

	@Value("${jdbc.driverClassName:}")
	private String driverClass;

	@Value("${jdbc.url:jdbc:mysql:///${app.name}?createDatabaseIfNotExist=true&autoReconnectForPools=true&useUnicode=true&characterEncoding=UTF-8&useServerPrepStmts=true&tinyInt1isBit=false}")
	private String jdbcUrl;

	@Value("${jdbc.username:root}")
	private String username;

	@Value("${jdbc.password:}")
	private String password;

	@Value("${dataSource.maxActive:50}")
	private int maxConnectionsPerPartition;

	@Value("${dataSource.initialSize:5}")
	private int minConnectionsPerPartition;

	@Value("${dataSource.statementsCacheSize:50}")
	private int statementsCacheSize;

	@Value("${dataSource.idleConnectionTestPeriodInMinutes:1}")
	private int idleConnectionTestPeriodInMinutes;

	@Value("${dataSource.idleMaxAgeInMinutes:60}")
	private int idleMaxAgeInMinutes;

	@Value("${dataSource.maxConnectionAgeInSeconds:14400}")
	private int maxConnectionAgeInSeconds;

	@Value("${jdbc.connectionTestStatement:}")
	private String connectionTestStatement;

	public @Bean(destroyMethod = "close")
	DataSource dataSource() {
		BoneCPDataSource ds = new BoneCPDataSource();
		if (StringUtils.isNotBlank(driverClass))
			ds.setDriverClass(driverClass);
		ds.setJdbcUrl(jdbcUrl);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setMaxConnectionsPerPartition(maxConnectionsPerPartition);
		ds.setMinConnectionsPerPartition(minConnectionsPerPartition);
		ds.setStatementsCacheSize(statementsCacheSize);
		ds.setIdleConnectionTestPeriodInMinutes(idleConnectionTestPeriodInMinutes);
		ds.setIdleMaxAgeInMinutes(idleMaxAgeInMinutes);
		ds.setMaxConnectionAgeInSeconds(maxConnectionAgeInSeconds);
		DatabaseProduct databaseProduct = DatabaseProduct.parse(jdbcUrl);
		if (StringUtils.isBlank(connectionTestStatement)
				&& databaseProduct != null)
			connectionTestStatement = databaseProduct.getValidationQuery();
		ds.setConnectionTestStatement(connectionTestStatement);
		return ds;
	}

	public @Bean
	JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

}
