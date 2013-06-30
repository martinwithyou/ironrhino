package org.ironrhino.core.sequence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.springframework.jdbc.datasource.DataSourceUtils;

public class DatabaseCyclicSequenceDelegate extends
		AbstractDatabaseCyclicSequence {

	private AbstractDatabaseCyclicSequence seq = null;

	@Override
	public void afterPropertiesSet() throws java.lang.Exception {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		DatabaseMetaData dbmd = con.getMetaData();
		String databaseProductName = dbmd.getDatabaseProductName()
				.toLowerCase();
		con.close();
		if (databaseProductName.contains("mysql"))
			seq = new MySQLCyclicSequence();
		else if (databaseProductName.contains("postgres"))
			seq = new PostgreSQLCyclicSequence();
		else if (databaseProductName.contains("oracle"))
			seq = new PostgreSQLCyclicSequence();
		else if (databaseProductName.contains("db2"))
			seq = new DB2CyclicSequence();
		else if (databaseProductName.contains("hsql"))
			seq = new HSQLCyclicSequence();
		else if (databaseProductName.contains("h2"))
			seq = new H2CyclicSequence();
		else
			throw new RuntimeException("not implemented for database "
					+ databaseProductName);
		seq.setDataSource(getDataSource());
		seq.setCacheSize(getCacheSize());
		seq.setCycleType(getCycleType());
		seq.setPaddingLength(getPaddingLength());
		seq.setTableName(getTableName());
		seq.setSequenceName(getSequenceName());
		seq.afterPropertiesSet();
	}

	@Override
	public String nextStringValue() {
		return seq.nextStringValue();
	}

}
