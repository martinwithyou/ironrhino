package org.ironrhino.online.action;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.compass.core.support.search.CompassSearchResults;
import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.search.CompassCriteria;
import org.ironrhino.core.search.CompassSearchService;
import org.ironrhino.online.support.SearchHitsControl;

import com.opensymphony.xwork2.ActionSupport;

public class SearchAction extends ActionSupport {

	private CompassSearchService compassSearchService;

	private String q;

	private int pn;

	private int ps = 10;

	private CompassSearchResults searchResults;

	private SearchHitsControl searchHitsControl;

	private List<AggregateResult> suggestions;

	public List<AggregateResult> getSuggestions() {
		return suggestions;
	}

	public void setSearchHitsControl(SearchHitsControl searchHitsControl) {
		this.searchHitsControl = searchHitsControl;
	}

	public CompassSearchResults getSearchResults() {
		return searchResults;
	}

	public void setCompassSearchService(
			CompassSearchService compassSearchService) {
		this.compassSearchService = compassSearchService;
	}

	public int getPn() {
		return pn;
	}

	public void setPn(int pn) {
		this.pn = pn;
	}

	public int getPs() {
		return ps;
	}

	public void setPs(int ps) {
		this.ps = ps;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	@SkipValidation
	public String execute() {
		if (StringUtils.isNotBlank(q)) {
			String query = q.trim();
			CompassCriteria cc = new CompassCriteria();
			cc.setQuery(query);
			cc.setAliases(new String[] { "product" });
			cc.ge("product.open", Boolean.TRUE);
			if (pn > 0)
				cc.setPageNo(pn);
			if (ps > 0)
				cc.setPageSize(ps);
			searchResults = compassSearchService.search(cc);
			if (searchHitsControl != null) {
				searchHitsControl.put(query, searchResults.getTotalHits());
			}
		}
		return "result";
	}

	@SkipValidation
	public String suggest() {
		if (StringUtils.isNotBlank(q)) {
			String keyword = q.trim();
			suggestions = searchHitsControl.suggest(keyword);
		}
		return "suggest";
	}

	public String formatScore(float score) {
		if (score > 0.999)
			return "100%";
		else
			return NumberUtils.format(NumberUtils.round(
					new BigDecimal(score).multiply(new BigDecimal(100)), 1)
					.doubleValue(), 1)
					+ "%";
	}
}
