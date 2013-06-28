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

public class MySQLCyclicSequence extends AbstractDatabaseCyclicSequence {

	private long nextId = 0;

	private long maxId = 0;

	public void afterPropertiesSet() {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
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
					if (columnName.equalsIgnoreCase(metadata
							.getColumnName(i + 1))) {
						columnExists = true;
						break;
					}
				}
				JdbcUtils.closeResultSet(rs);
				if (!columnExists) {
					stmt.execute("ALTER TABLE `" + getTableName() + "` ADD "
							+ columnName + " INT NOT NULL DEFAULT 0,ADD "
							+ columnName
							+ "_TIMESTAMP BIGINT DEFAULT UNIX_TIMESTAMP()");
				}
			} else {
				stmt.execute("CREATE TABLE `" + getTableName() + "` ("
						+ columnName + " INT NOT NULL DEFAULT 0," + columnName
						+ "_TIMESTAMP BIGINT) ");
				stmt.execute("INSERT INTO `" + getTableName()
						+ "` VALUES(0,UNIX_TIMESTAMP())");
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
		if (this.maxId == this.nextId) {
			Connection con = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = con.createStatement();
				DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				// Increment the sequence column...
				String columnName = getSequenceName();
				rs = stmt.executeQuery("SELECT  " + columnName
						+ "_TIMESTAMP,UNIX_TIMESTAMP() FROM " + getTableName());
				try {
					rs.next();
					Long last = rs.getLong(1);
					if (last < 10000000000L) // no mills
						last *= 1000;
					lastInsertTimestamp = new Date(last);
					thisTimestamp = new Date(rs.getLong(2) * 1000);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
				boolean same = getCycleType().isSameCycle(lastInsertTimestamp,
						thisTimestamp);
				if (same)
					stmt.executeUpdate("UPDATE " + getTableName() + " SET "
							+ columnName + " = LAST_INSERT_ID(" + columnName
							+ " + " + getCacheSize() + ")," + columnName
							+ "_TIMESTAMP = UNIX_TIMESTAMP()");
				else
					stmt.executeUpdate("UPDATE " + getTableName() + " SET "
							+ columnName + " = LAST_INSERT_ID("
							+ getCacheSize() + ")," + columnName
							+ "_TIMESTAMP = UNIX_TIMESTAMP()");
				con.commit();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				try {
					if (!rs.next()) {
						throw new DataAccessResourceFailureException(
								"LAST_INSERT_ID() failed after executing an update");
					}
					this.maxId = rs.getLong(1);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
				this.nextId = this.maxId - getCacheSize() + 1;
			} catch (SQLException ex) {
				throw new DataAccessResourceFailureException(
						"Could not obtain last_insert_id()", ex);
			} finally {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		} else {
			this.nextId++;
		}
		return getStringValue(thisTimestamp, getPaddingLength(), (int) nextId);
	}

}
