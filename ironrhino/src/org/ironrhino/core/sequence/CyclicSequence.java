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

		MINUTE("yyyyMMddHHmm") {
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				if (lastCal == null)
					return true;
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)
						&& nowCal.get(Calendar.MONTH) == lastCal
								.get(Calendar.MONTH)
						&& nowCal.get(Calendar.HOUR_OF_DAY) == lastCal
								.get(Calendar.HOUR_OF_DAY) && nowCal
						.get(Calendar.MINUTE) <= lastCal.get(Calendar.MINUTE));

			}
		},
		HOUR("yyyyMMddHH") {
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				if (lastCal == null)
					return true;
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)
						&& nowCal.get(Calendar.MONTH) == lastCal
								.get(Calendar.MONTH) && nowCal
						.get(Calendar.HOUR_OF_DAY) <= lastCal
						.get(Calendar.HOUR_OF_DAY));
			}

		},
		DAY("yyyyMMdd") {
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				if (lastCal == null)
					return true;
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)
						&& nowCal.get(Calendar.MONTH) == lastCal
								.get(Calendar.MONTH) && nowCal
						.get(Calendar.DAY_OF_YEAR) <= lastCal
						.get(Calendar.DAY_OF_YEAR));
			}

		},
		MONTH("yyyyMM") {
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				if (lastCal == null)
					return true;
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR) && nowCal
						.get(Calendar.MONTH) <= lastCal.get(Calendar.MONTH));
			}

		},
		YEAR("yyyy") {
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				if (lastCal == null)
					return true;
				return (nowCal.get(Calendar.YEAR) <= lastCal.get(Calendar.YEAR));
			}

		};
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

		boolean isSameCycle(Date last, Date now) {
			if (last == null)
				return true;
			Calendar lastCalendar = Calendar.getInstance();
			lastCalendar.setTime(last);
			Calendar nowCalendar = Calendar.getInstance();
			nowCalendar.setTime(now);
			return isSameCycle(lastCalendar, nowCalendar);
		}

		protected abstract boolean isSameCycle(Calendar lastCalendar,
				Calendar nowCalendar);

	}

}