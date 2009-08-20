package org.ironrhino.online.model;

import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.model.BaseEntity;

public class SearchHitsHistory extends BaseEntity {

	private static final long serialVersionUID = -7107753132364748415L;

	@NaturalId
	private String keyword;

	@NaturalId
	private int month;

	@NaturalId
	private int year;

	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

}
