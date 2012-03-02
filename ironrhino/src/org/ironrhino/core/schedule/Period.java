package org.ironrhino.core.schedule;

public enum Period {

	DAY_START, WEEK_START, MONTH_START, YEAR_START;

	private String fullname;

	private Period() {
		fullname = new StringBuilder(getClass().getName()).append(".")
				.append(name()).toString();
	}

	public String getFullname() {
		return fullname;
	}
}