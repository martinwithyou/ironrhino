package org.ironrhino.core.sequence;

import java.util.Date;

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

		public String getPattern() {
			return format.getPattern();
		}

		public String format(Date date) {
			return format.format(date);
		}

	}

}