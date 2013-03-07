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
					if (columnName.equalsIgnoreCase(metadata
							.getColumnName(i + 1))) {
						columnExists = true;
						break;
					}
				}
				JdbcUtils.closeResultSet(rs);
				if (!columnExists) {
					stmt.execute("ALTER TABLE `" + getSequenceName() + "` ADD "
							+ columnName + " INT NOT NULL DEFAULT 0,ADD "
							+ columnName
							+ "_TIMESTAMP BIGINT DEFAULT UNIX_TIMESTAMP()");
				}
			} else {
				stmt.execute("CREATE TABLE `" + getSequenceName() + "` ("
						+ columnName + " INT NOT NULL DEFAULT 0," + columnName
						+ "_TIMESTAMP BIGINT) ");
				stmt.execute("INSERT INTO `" + getSequenceName()
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
				String columnName = getColumnName();
				rs = stmt.executeQuery("select  " + columnName
						+ "_TIMESTAMP,UNIX_TIMESTAMP() from "
						+ getSequenceName());
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
				boolean same = inSameCycle(getCycleType(), lastInsertTimestamp,
						thisTimestamp);
				if (same)
					stmt.executeUpdate("update " + getSequenceName() + " set "
							+ columnName + " = last_insert_id(" + columnName
							+ " + " + getCacheSize() + ")," + columnName
							+ "_TIMESTAMP = UNIX_TIMESTAMP()");
				else
					stmt.executeUpdate("update " + getSequenceName() + " set "
							+ columnName + " = last_insert_id("
							+ getCacheSize() + ")," + columnName
							+ "_TIMESTAMP = UNIX_TIMESTAMP()");
				rs = stmt.executeQuery("select last_insert_id()");
				try {
					if (!rs.next()) {
						throw new DataAccessResourceFailureException(
								"last_insert_id() failed after executing an update");
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
		return getStringValue(thisTimestamp, getCycleType(),
				getPaddingLength(), (int) nextId);
	}

}
