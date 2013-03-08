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

public abstract class AbstractSequenceCyclicSequence extends
		AbstractDatabaseCyclicSequence {

	protected abstract String getQuerySequenceStatement();

	protected abstract String getCreateSequenceStatement();

	protected abstract String getRestartSequenceStatement();

	protected abstract String getCurrentTimestampFunction();

	protected String getTimestampColumnType() {
		return "BIGINT";
	}

	protected String getCreateTableStatement() {
		return "CREATE TABLE " + getTableName() + " (" + getSequenceName()
				+ "_TIMESTAMP " + getTimestampColumnType() + ") ";
	}

	protected String getAddColumnStatement() {
		return "ALTER TABLE " + getTableName() + " ADD " + getSequenceName()
				+ "_TIMESTAMP " + getTimestampColumnType() + " DEFAULT "
				+ getCurrentTimestampFunction();
	}

	protected String getInsertStatement() {
		return "INSERT INTO " + getTableName() + " VALUES("
				+ getCurrentTimestampFunction() + ")";
	}

	public void afterPropertiesSet() {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			checkDatabaseProductName(dbmd.getDatabaseProductName());
			ResultSet rs = dbmd.getTables(null, null, "%", null);
			boolean tableExists = false;
			while (rs.next()) {
				if (getTableName().equalsIgnoreCase(rs.getString(3))) {
					tableExists = true;
					break;
				}
			}
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			String columnName = getSequenceName();
			if (tableExists) {
				rs = stmt.executeQuery("SELECT * FROM " + getTableName());
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
					stmt.execute(getAddColumnStatement());
					stmt.execute(getCreateSequenceStatement());
				}
			} else {
				stmt.execute(getCreateTableStatement());
				stmt.execute(getInsertStatement());
				stmt.execute(getCreateSequenceStatement());
			}
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException(ex.getMessage(), ex);
		} finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		Date lastInsertTimestamp = null;
		Date thisTimestamp = null;
		long nextId = 0;
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			String columnName = getSequenceName();
			rs = stmt
					.executeQuery("select  " + columnName + "_TIMESTAMP,"
							+ getCurrentTimestampFunction() + " from "
							+ getTableName());
			try {
				rs.next();
				lastInsertTimestamp = new Date(rs.getLong(1) * 1000);
				thisTimestamp = new Date(rs.getLong(2) * 1000);
			} finally {
				JdbcUtils.closeResultSet(rs);
			}
			boolean same = inSameCycle(lastInsertTimestamp, thisTimestamp);
			stmt.executeUpdate("update " + getTableName() + " set "
					+ columnName + "_TIMESTAMP = "
					+ getCurrentTimestampFunction());
			if (!same)
				stmt.execute(getRestartSequenceStatement());
			rs = stmt.executeQuery(getQuerySequenceStatement());
			try {
				rs.next();
				nextId = rs.getLong(1);
			} finally {
				JdbcUtils.closeResultSet(rs);
			}
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException(
					"Could not obtain next value of sequence", ex);
		} finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
		return getStringValue(thisTimestamp, getCycleType(),
				getPaddingLength(), (int) nextId);
	}

}
