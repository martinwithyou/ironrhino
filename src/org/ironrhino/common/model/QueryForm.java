package org.ironrhino.common.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class QueryForm implements Serializable {

	private static final long serialVersionUID = 834165502921095177L;

	private String keyword;

	private Status status;

	private Date startDate;

	private Date endDate;

	public QueryForm() {

	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void fill(DetachedCriteria dc, String... propertyNames) {
		String datePropertyName = "date";
		String keywordPropertyName = "name";
		String statusPropertyName = "status";
		if (propertyNames.length > 0)
			datePropertyName = propertyNames[0];
		if (propertyNames.length > 1)
			keywordPropertyName = propertyNames[1];
		if (propertyNames.length > 2)
			statusPropertyName = propertyNames[2];
		if (startDate != null && endDate == null)
			dc.add(Restrictions.ge(datePropertyName, startDate));
		if (startDate == null && endDate != null)
			dc.add(Restrictions.le(datePropertyName, endDate));
		if (startDate != null && endDate != null)
			dc.add(Restrictions.between(datePropertyName, startDate, endDate));
		if (StringUtils.isNotBlank(keyword))
			dc.add(Restrictions.ilike(keywordPropertyName, keyword,
					MatchMode.ANYWHERE));
		if (status != null)
			dc.add(Restrictions.eq(statusPropertyName, status));
	}
}