package org.ironrhino.core.sequence;

import java.util.Calendar;
import java.util.Date;

import org.ironrhino.core.util.NumberUtils;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractCyclicSequence implements CyclicSequence,
		InitializingBean {

	private CycleType cycleType = CycleType.DAY;

	private String sequenceName;

	private int paddingLength = 5;

	public CycleType getCycleType() {
		return cycleType;
	}

	public void setCycleType(CycleType cycleType) {
		this.cycleType = cycleType;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public int getPaddingLength() {
		return paddingLength;
	}

	public void setPaddingLength(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	@Override
	public int nextIntValue() {
		String s = nextStringValue();
		return Integer.valueOf(s.substring(cycleType.getPattern().length()));
	}

	@Override
	public long nextLongValue() {
		return Long.valueOf(nextStringValue());
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
		case MINUTE:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
					&& now.get(Calendar.HOUR_OF_DAY) == cal
							.get(Calendar.HOUR_OF_DAY) && now
						.get(Calendar.MINUTE) == cal.get(Calendar.MINUTE));
		case HOUR:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now
						.get(Calendar.HOUR_OF_DAY) == cal
					.get(Calendar.HOUR_OF_DAY));
		case DAY:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
					&& now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now
						.get(Calendar.DAY_OF_YEAR) == cal
					.get(Calendar.DAY_OF_YEAR));
		case MONTH:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && now
					.get(Calendar.MONTH) == cal.get(Calendar.MONTH));
		case YEAR:
			return (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR));
		default:
			return true;
		}
	}

	protected static String getStringValue(Date date, CycleType cycleType,
			int paddingLength, int nextId) {
		if (date == null)
			date = new Date();
		return cycleType.format(date)
				+ NumberUtils.format(nextId, paddingLength);
	}

}
