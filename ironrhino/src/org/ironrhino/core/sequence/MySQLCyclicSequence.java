package org.ironrhino.core.sequence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.ironrhino.core.util.NumberUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.incrementer.AbstractColumnMaxValueIncrementer;

public class MySQLCyclicSequence extends AbstractColumnMaxValueIncrementer
		implements CyclicSequence {

	private long nextId = 0;

	private long maxId = 0;

	private CycleType cycleType = CycleType.day;

	@Override
	public CycleType getCycleType() {
		return cycleType;
	}

	public void setCycleType(CycleType cycleType) {
		this.cycleType = cycleType;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		checkTable();
	}

	protected void checkTable() {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTables(null, null, "%", null);
			boolean tableExists = false;
			while (rs.next()) {
				if (getIncrementerName().equalsIgnoreCase(rs.getString(3))) {
					tableExists = true;
					break;
				}
			}
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			String columnName = getColumnName();
			if (tableExists) {
				rs = stmt.executeQuery("SELECT * FROM " + getIncrementerName());
				boolean columnExists = false;
				ResultSetMetaData metadata = rs.getMetaData();
				for (int i = 0; i < metadata.getColumnCount(); i++) {
					if (columnName.equalsIgnoreCase(metadata
							.getColumnName(i + 1))) {
						columnExists = true;
						break;
					}
				}
				if (columnExists) {
					JdbcUtils.closeResultSet(rs);
				} else {
					stmt.execute("ALTER TABLE `" + getIncrementerName()
							+ "` ADD " + columnName
							+ " INT NOT NULL DEFAULT 0,ADD " + columnName
							+ "_TIMESTAMP BIGINT DEFAULT UNIX_TIMESTAMP()");
				}
			} else {
				stmt.execute("CREATE TABLE `" + getIncrementerName() + "` ("
						+ columnName + " INT NOT NULL DEFAULT 0," + columnName
						+ "_TIMESTAMP BIGINT) ");
				stmt.execute("INSERT INTO `" + getIncrementerName()
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
	protected synchronized long getNextKey() throws DataAccessException {
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
						+ getIncrementerName());
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
				boolean same = inSameCycle(cycleType, lastInsertTimestamp,
						thisTimestamp);
				if (same)
					stmt.executeUpdate("update " + getIncrementerName()
							+ " set " + columnName + " = last_insert_id("
							+ columnName + " + " + getCacheSize() + "),"
							+ columnName + "_TIMESTAMP = UNIX_TIMESTAMP()");
				else
					stmt.executeUpdate("update " + getIncrementerName()
							+ " set " + columnName + " = last_insert_id("
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
		return getLongValue(thisTimestamp, getCycleType(), getPaddingLength(),
				(int) nextId);
	}

	@Override
	public long nextLongValue() throws DataAccessException {
		return getNextKey();
	}

	@Override
	public int nextIntValue() throws DataAccessException {
		String s = nextStringValue();
		return Integer.valueOf(s.substring(s.length() - getPaddingLength(),
				s.length()));
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		return String.valueOf(nextLongValue());
	}

	private static boolean inSameCycle(CycleType cycleType,
			Date lastInsertTimestamp, Date thisTimestamp) {
		if (lastInsertTimestamp == null)
			return true;
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastInsertTimestamp);
		Calendar now = Calendar.getInstance();
		now.setTime(thisTimestamp);
		switch (cycleType) {
		case minute:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
					&& now.get(Calendar.HOUR_OF_DAY) == cal
							.get(Calendar.HOUR_OF_DAY) && now
						.get(Calendar.MINUTE) == cal.get(Calendar.MINUTE));
		case hour:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now
						.get(Calendar.HOUR_OF_DAY) == cal
					.get(Calendar.HOUR_OF_DAY));
		case day:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now
						.get(Calendar.DAY_OF_YEAR) == cal
					.get(Calendar.DAY_OF_YEAR));
		case month:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && now
					.get(Calendar.MONTH) == cal.get(Calendar.MONTH));
		case year:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR));
		default:
			return true;
		}
	}

	private static long getLongValue(Date date, CycleType cycleType,
			int paddingLength, int nextId) {
		String pattern = "";
		switch (cycleType) {
		case minute:
			pattern = "yyyyMMddHHmm";
			break;
		case hour:
			pattern = "yyyyMMddHH";
			break;
		case day:
			pattern = "yyyyMMdd";
			break;
		case month:
			pattern = "yyyyMM";
			break;
		case year:
			pattern = "yyyy";
			break;
		default:
			break;
		}
		if (date == null)
			date = new Date();
		String s = new SimpleDateFormat(pattern).format(date)
				+ NumberUtils.format(nextId, paddingLength);
		return Long.valueOf(s);
	}

}
