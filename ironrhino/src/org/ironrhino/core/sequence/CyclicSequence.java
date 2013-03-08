package org.ironrhino.core.sequence;

import java.util.Calendar;
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

		public boolean isSameCycle(Date last, Date now) {
			if (last == null)
				return true;
			Calendar lastCal = Calendar.getInstance();
			lastCal.setTime(last);
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTime(now);
			switch (this) {
			case MINUTE:
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)
						&& nowCal.get(Calendar.MONTH) == lastCal
								.get(Calendar.MONTH)
						&& nowCal.get(Calendar.HOUR_OF_DAY) == lastCal
								.get(Calendar.HOUR_OF_DAY) && nowCal
						.get(Calendar.MINUTE) <= lastCal.get(Calendar.MINUTE));
			case HOUR:
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)
						&& nowCal.get(Calendar.MONTH) == lastCal
								.get(Calendar.MONTH) && nowCal
						.get(Calendar.HOUR_OF_DAY) <= lastCal
						.get(Calendar.HOUR_OF_DAY));
			case DAY:
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)
						&& nowCal.get(Calendar.MONTH) == lastCal
								.get(Calendar.MONTH) && nowCal
						.get(Calendar.DAY_OF_YEAR) <= lastCal
						.get(Calendar.DAY_OF_YEAR));
			case MONTH:
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR) && nowCal
						.get(Calendar.MONTH) <= lastCal.get(Calendar.MONTH));
			case YEAR:
				return (nowCal.get(Calendar.YEAR) <= lastCal.get(Calendar.YEAR));
			default:
				return true;
			}
		}

	}

}