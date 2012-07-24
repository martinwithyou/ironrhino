package org.ironrhino.core.sequence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

public class PostgreSQLCyclicSequence extends AbstractCyclicSequence {

	public void afterPropertiesSet() {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			checkDatabaseProductName(dbmd.getDatabaseProductName());
			ResultSet rs = dbmd.getTables(null, null, "%", null);
			boolean tableExists = false;
			while (rs.next()) {
				if (getSequenceName().equalsIgnoreCase(rs.getString(3))) {
					tableExists = true;
					break;
				}
			}
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			String columnName = getColumnName();
			if (tableExists) {
				rs = stmt.executeQuery("SELECT * FROM " + getSequenceName());
				boolean columnExists = false;
				ResultSetMetaData metadata = rs.getMetaData();
				for (int i = 0; i < metadata.getColumnCount(); i++) {
					if ((columnName + "_TIMESTAMP").equalsIgnoreCase(metadata
							.getColumnName(i + 1))) {
						columnExists = true;
						break;
					}
				}
				JdbcUtils.closeResultSet(rs);
				if (!columnExists) {
					stmt.execute("ALTER TABLE " + getSequenceName() + " ADD "
							+ columnName
							+ "_TIMESTAMP BIGINT DEFAULT now()::abstime::int4");
					stmt.execute("CREATE  SEQUENCE " + getActualSequenceName()
							+ " CACHE " + getCacheSize());
				}
			} else {
				stmt.execute("CREATE TABLE " + getSequenceName() + " ("
						+ columnName + "_TIMESTAMP BIGINT) ");
				stmt.execute("INSERT INTO " + getSequenceName()
						+ " VALUES(now()::abstime::int4)");
				stmt.execute("CREATE  SEQUENCE " + getActualSequenceName()
						+ " CACHE " + getCacheSize());
			}
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException(ex.getMessage(), ex);
		} finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}

	@Override
	public long nextLongValue() throws DataAccessException {
		Date lastInsertTimestamp = null;
		Date thisTimestamp = null;
		long nextId = 0;
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			String columnName = getColumnName();
			rs = stmt.executeQuery("select  " + columnName
					+ "_TIMESTAMP,now()::abstime::int4 from "
					+ getSequenceName());
			try {
				rs.next();
				lastInsertTimestamp = new Date(rs.getLong(1) * 1000);
				thisTimestamp = new Date(rs.getLong(2) * 1000);
			} finally {
				JdbcUtils.closeResultSet(rs);
			}
			boolean same = inSameCycle(getCycleType(), lastInsertTimestamp,
					thisTimestamp);
			stmt.executeUpdate("update " + getSequenceName() + " set "
					+ columnName + "_TIMESTAMP = now()::abstime::int4");
			if (!same)
				stmt.execute("ALTER SEQUENCE " + getActualSequenceName()
						+ " RESTART WITH 1");
			rs = stmt.executeQuery("select nextval('" + getActualSequenceName()
					+ "')");
			try {
				rs.next();
				nextId = rs.getLong(1);
			} finally {
				JdbcUtils.closeResultSet(rs);
			}
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException(
					"Could not obtain nextval()", ex);
		} finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
		return getLongValue(thisTimestamp, getCycleType(), getPaddingLength(),
				(int) nextId);
	}

}
