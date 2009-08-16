package org.ironrhino.online.model;

import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.model.BaseEntity;

public class SearchHits extends BaseEntity {

	@NaturalId
	private String keyword;

	private int daily;

	private int monthly;

	private int yearly;

	private int total;

	private int totalHits;

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public int getDaily() {
		return daily;
	}

	public void setDaily(int daily) {
		this.daily = daily;
	}

	public int getMonthly() {
		return monthly;
	}

	public void setMonthly(int monthly) {
		this.monthly = monthly;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getYearly() {
		return yearly;
	}

	public void setYearly(int yearly) {
		this.yearly = yearly;
	}

}
