package org.ironrhino.core.spring;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.incrementer.AbstractColumnMaxValueIncrementer;

public class CyclicMySQLMaxValueIncrementer extends
		AbstractColumnMaxValueIncrementer {

	private static final String VALUE_SQL = "select last_insert_id()";

	private long nextId = 0;

	private long maxId = 0;

	private Calendar lastInserted;

	private String cycleType = "day";

	public String getCycleType() {
		return cycleType;
	}

	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}

	protected synchronized long getNextKey() throws DataAccessException {
		if (this.maxId == this.nextId) {

			Connection con = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				// Increment the sequence column...
				String columnName = getColumnName();
				if (inSameCycle())
					stmt.executeUpdate("update " + getIncrementerName()
							+ " set " + columnName + " = last_insert_id("
							+ columnName + " + " + getCacheSize() + ")");
				else
					stmt.executeUpdate("update " + getIncrementerName()
							+ " set " + columnName + " = last_insert_id("
							+ getCacheSize() + ")");
				lastInserted = Calendar.getInstance();
				// Retrieve the new max of the sequence column...
				ResultSet rs = stmt.executeQuery(VALUE_SQL);
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
		return this.nextId;
	}

	private boolean inSameCycle() {
		if (lastInserted == null)
			return true;
		Calendar now = Calendar.getInstance();
		if ("minute".equalsIgnoreCase(cycleType))
			return (now.get(Calendar.YEAR) == lastInserted.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == lastInserted
							.get(Calendar.MONTH)
					&& now.get(Calendar.HOUR_OF_DAY) == lastInserted
							.get(Calendar.HOUR_OF_DAY) && now
					.get(Calendar.MINUTE) == lastInserted.get(Calendar.MINUTE));
		else if ("hour".equalsIgnoreCase(cycleType))
			return (now.get(Calendar.YEAR) == lastInserted.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == lastInserted
							.get(Calendar.MONTH) && now
					.get(Calendar.HOUR_OF_DAY) == lastInserted
					.get(Calendar.HOUR_OF_DAY));
		else if ("day".equalsIgnoreCase(cycleType))
			return (now.get(Calendar.YEAR) == lastInserted.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == lastInserted
							.get(Calendar.MONTH) && now
					.get(Calendar.DAY_OF_YEAR) == lastInserted
					.get(Calendar.DAY_OF_YEAR));
		else if ("month".equalsIgnoreCase(cycleType))
			return (now.get(Calendar.YEAR) == lastInserted.get(Calendar.YEAR) && now
					.get(Calendar.MONTH) == lastInserted.get(Calendar.MONTH));
		else if ("year".equalsIgnoreCase(cycleType))
			return (now.get(Calendar.YEAR) == lastInserted.get(Calendar.YEAR));
		return true;
	}
}
