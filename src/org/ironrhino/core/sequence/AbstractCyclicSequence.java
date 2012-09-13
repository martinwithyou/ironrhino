package org.ironrhino.core.sequence;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.ironrhino.core.util.NumberUtils;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractCyclicSequence implements CyclicSequence,
		InitializingBean {

	private DataSource dataSource;

	private CycleType cycleType = CycleType.day;

	private String sequenceName;

	private String columnName;

	private int paddingLength;

	private int cacheSize = 1;

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public CycleType getCycleType() {
		return cycleType;
	}

	public void setCycleType(CycleType cycleType) {
		this.cycleType = cycleType;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getPaddingLength() {
		return paddingLength;
	}

	public void setPaddingLength(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	protected void checkDatabaseProductName(String databaseProductName) {
		String impl = getClass().getSimpleName();
		impl = impl.substring(0, impl.indexOf("CyclicSequence"));
		if (!databaseProductName.toLowerCase().contains(impl.toLowerCase()))
			throw new RuntimeException(getClass()
					+ " is not compatibility with " + databaseProductName);
	}

	@Override
	public int nextIntValue() {
		String s = nextStringValue();
		return Integer.valueOf(s.substring(s.length() - getPaddingLength(),
				s.length()));
	}

	@Override
	public String nextStringValue() {
		return String.valueOf(nextLongValue());
	}

	protected String getActualSequenceName() {
		return new StringBuilder(getSequenceName()).append("_")
				.append(getColumnName()).append("_SEQ").toString();
	}

	protected static boolean inSameCycle(CycleType cycleType,
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

	protected static long getLongValue(Date date, CycleType cycleType,
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
