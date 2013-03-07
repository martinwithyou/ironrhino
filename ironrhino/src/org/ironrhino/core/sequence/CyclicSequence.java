package org.ironrhino.core.sequence;

import org.apache.commons.lang3.time.FastDateFormat;

public interface CyclicSequence {

	public CycleType getCycleType();

	public long nextLongValue();

	public int nextIntValue();

	public String nextStringValue();

	static enum CycleType {

		MINUTE("yyyyMMddHHmm"), HOUR("yyyyMMddHH"), DAY("yyyyMMdd"), MONTH(
				"yyyyMM"), YEAR("yyyy");
		private FastDateFormat format;

		private CycleType(String pattern) {
			this.format = FastDateFormat.getInstance(pattern);
		}

		public FastDateFormat getFormat() {
			return format;
		}

	}

}