package org.ironrhino.core.sequence;

import java.util.Date;

import org.ironrhino.core.util.NumberUtils;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractCyclicSequence implements CyclicSequence,
		InitializingBean {

	protected Date lastTimestamp;

	protected Date thisTimestamp;

	private CycleType cycleType = CycleType.DAY;

	private String sequenceName;

	private int paddingLength = 5;

	@Override
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

	protected String getStringValue(Date date, int paddingLength, int nextId) {
		if (date == null)
			date = new Date();
		return getCycleType().format(date)
				+ NumberUtils.format(nextId, paddingLength);
	}

}
