package org.ironrhino.core.sequence;

public interface CyclicSequence {

	public CycleType getCycleType();

	public long nextLongValue();

	public int nextIntValue();

	public String nextStringValue();

	static enum CycleType {
		minute, hour, day, month, year
	}

}