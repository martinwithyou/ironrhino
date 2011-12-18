package org.ironrhino.core.search.compass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassHit;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQuery.SortDirection;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.support.search.CompassSearchResults;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.SearchCriteria;
import org.ironrhino.core.search.SearchService;
import org.ironrhino.core.search.SearchStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class CompassSearchService implements SearchService {

	protected Logger log = LoggerFactory.getLogger(CompassSearchService.class);

	@Inject
	private Compass compass;

	@Autowired(required = false)
	private SearchStat searchStat;

	private CompassTemplate compassTemplate;

	public ResultPage search(ResultPage resultPage) {
		return search(resultPage, null);
	}

	public ResultPage search(ResultPage resultPage, Mapper mapper) {
		CompassSearchResults searchResults = search(
				(CompassSearchCriteria) resultPage.getCriteria(),
				resultPage.getPageNo(), resultPage.getPageSize());
		resultPage.setTotalRecord(searchResults.getTotalHits());
		CompassHit[] hits = searchResults.getHits();
		if (hits != null) {
			List list = new ArrayList(hits.length);
			for (CompassHit ch : searchResults.getHits()) {
				Object data = mapper == null ? ch.getData() : mapper.map(ch
						.getData());
				if (data != null)
					list.add(data);
				else
					resultPage.setTotalRecord(resultPage.getTotalRecord() - 1);
			}
			resultPage.setResult(list);
		}
		return resultPage;
	}

	public List search(SearchCriteria searchCriteria) {
		return search(searchCriteria, null);
	}

	public List search(SearchCriteria searchCriteria, Mapper mapper) {
		CompassSearchResults searchResults = search(
				(CompassSearchCriteria) searchCriteria, -1, -1);
		CompassHit[] hits = searchResults.getHits();
		if (hits != null) {
			List list = new ArrayList(hits.length);
			for (CompassHit ch : searchResults.getHits()) {
				Object data = mapper == null ? ch.getData() : mapper.map(ch
						.getData());
				if (data != null)
					list.add(data);
			}
			return list;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public CompassSearchResults search(final CompassSearchCriteria criteria,
			final int pageNo, final int pageSize) {
		if (criteria == null)
			return null;
		CompassSearchResults searchResults = (CompassSearchResults) getCompassTemplate()
				.execute(new CompassCallback() {
					public Object doInCompass(CompassSession session) {
						return performSearch(criteria, pageNo, pageSize,
								session);
					}
				});
		if (searchStat != null)
			searchStat.put(criteria.getQuery(), searchResults.getTotalHits());
		return searchResults;
	}

	protected CompassSearchResults performSearch(
			CompassSearchCriteria criteria, int pageNo, int pageSize,
			CompassSession session) {
		long time = System.currentTimeMillis();
		CompassQuery query = null;
		try {
			query = buildQuery(criteria, session);
		} catch (SearchEngineQueryParseException e) {
			log.error(e.getMessage(), e);
			return new CompassSearchResults(null, 1, 0);
		}
		CompassHits hits = query.hits();
		int hitsLength = hits.getLength();
		if (hitsLength == 0)
			return new CompassSearchResults(null, System.currentTimeMillis()
					- time, 0);

		CompassDetachedHits detachedHits;
		CompassSearchResults.Page[] pages = null;
		if (pageSize < 0 || pageSize == Integer.MAX_VALUE) {
			doProcessBeforeDetach(criteria, session, hits, -1, -1);
			detachedHits = hits.detach();
		} else {
			if (pageSize > ResultPage.MAX_RECORDS_PER_PAGE)
				pageSize = ResultPage.MAX_RECORDS_PER_PAGE;
			int totalPages = hitsLength % pageSize == 0 ? hitsLength / pageSize
					: hitsLength / pageSize + 1;
			if (pageNo < 1)
				pageNo = 1;
			if (pageNo > totalPages)
				pageNo = totalPages;

			int from = (pageNo - 1) * pageSize;
			doProcessBeforeDetach(criteria, session, hits, from, pageSize);
			detachedHits = hits.detach(from, pageSize);
			doProcessAfterDetach(criteria, session, detachedHits);
			pages = new CompassSearchResults.Page[totalPages];
			for (int i = 0; i < pages.length; i++) {
				pages[i] = new CompassSearchResults.Page();
				pages[i].setFrom(i * pageSize + 1);
				pages[i].setSize(pageSize);
				pages[i].setTo((i + 1) * pageSize);
				if (from >= (pages[i].getFrom() - 1) && from < pages[i].getTo())
					pages[i].setSelected(true);
				else
					pages[i].setSelected(false);
			}
			if (totalPages > 0) {
				CompassSearchResults.Page lastPage = pages[totalPages - 1];
				if (lastPage.getTo() > hitsLength) {
					lastPage.setSize(hitsLength - lastPage.getFrom());
					lastPage.setTo(hitsLength);
				}
			}
		}
		time = System.currentTimeMillis() - time;
		CompassSearchResults searchResults = new CompassSearchResults(
				detachedHits.getHits(), time, hitsLength);
		searchResults.setPages(pages);
		return searchResults;
	}

	protected CompassQuery buildQuery(CompassSearchCriteria criteria,
			CompassSession session) {
		String queryString = criteria.getQuery().replaceAll("\\\\", "").trim();
		if (queryString.startsWith(":"))
			queryString = queryString.substring(1);
		if (queryString.endsWith(":"))
			queryString = queryString.substring(0, queryString.length() - 1);
		CompassQuery query = session.queryBuilder().queryString(queryString)
				.toQuery();
		if (criteria.getAliases() != null)
			query.setAliases(criteria.getAliases());
		if (criteria.getBoost() != null)
			query.setBoost(criteria.getBoost());
		if (criteria.getFilter() != null)
			query.setFilter(criteria.getFilter());
		for (Map.Entry<String, Boolean> entry : criteria.getSorts().entrySet()) {
			query.addSort(entry.getKey(),
					entry.getValue() ? SortDirection.REVERSE
							: SortDirection.AUTO);
		}
		return query;
	}

	protected void doProcessBeforeDetach(CompassSearchCriteria criteria,
			CompassSession session, CompassHits hits, int from, int size) {
		// do it in browser use javascript
		// highlight(criteria, session, hits, from, size);
	}

	protected void doProcessAfterDetach(CompassSearchCriteria criteria,
			CompassSession session, CompassDetachedHits hits) {

	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(compass);
		this.compassTemplate = new CompassTemplate(compass);
		this.compassTemplate.setReadOnly(true);
	}

	public void setCompass(Compass compass) {
		this.compass = compass;
	}

	protected CompassTemplate getCompassTemplate() {
		return this.compassTemplate;
	}

}
