package org.ironrhino.core.search;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryFilterBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.engine.SearchEngineQueryParseException;
import org.compass.core.support.search.CompassSearchResults;
import org.springframework.util.Assert;

public class CompassSearchService {

	protected Log log = LogFactory.getLog(CompassSearchService.class);

	private Compass compass;

	private CompassTemplate compassTemplate;

	public CompassSearchResults search(final CompassCriteria criteria) {
		if (criteria == null)
			return null;
		return (CompassSearchResults) getCompassTemplate().execute(
				new CompassCallback() {
					public Object doInCompass(CompassSession session) {
						return performSearch(criteria, session);
					}
				});
	}

	protected CompassSearchResults performSearch(CompassCriteria criteria,
			CompassSession session) {
		int pageSize = criteria.getPageSize();
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
		int pageNo = criteria.getPageNo();
		if (pageSize < 0 || pageSize == Integer.MAX_VALUE) {
			doProcessBeforeDetach(criteria, session, hits, -1, -1);
			detachedHits = hits.detach();
		} else {
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

	protected CompassQuery buildQuery(CompassCriteria criteria,
			CompassSession session) {
		CompassQuery query = session.queryBuilder().queryString(
				criteria.getQuery().trim()).toQuery();

		if (criteria.getAliases() != null)
			query.setAliases(criteria.getAliases());
		if (criteria.getBoost() != null)
			query.setBoost(criteria.getBoost());

		if (criteria.getConditions().size() > 0) {
			CompassQueryFilterBuilder builder = session.queryFilterBuilder();
			if (criteria.getConditions().size() == 1) {
				CompassCondition condition = criteria.getConditions()
						.iterator().next();
				if (condition.getType() == CompassConditionType.BETWEEN)
					query.setFilter(builder.between(condition.getName(),
							condition.getLow(), condition.getHigh(), true,
							false));
				else if (condition.getType() == CompassConditionType.GE)
					query.setFilter(builder.ge(condition.getName(), condition
							.getValue()));
				else if (condition.getType() == CompassConditionType.GT)
					query.setFilter(builder.gt(condition.getName(), condition
							.getValue()));
				else if (condition.getType() == CompassConditionType.LE)
					query.setFilter(builder.le(condition.getName(), condition
							.getValue()));
				else if (condition.getType() == CompassConditionType.LT)
					query.setFilter(builder.lt(condition.getName(), condition
							.getValue()));
			} else {
				CompassQueryFilterBuilder.CompassBooleanQueryFilterBuilder cc = builder
						.bool();
				for (CompassCondition condition : criteria.getConditions())
					if (condition.getType() == CompassConditionType.BETWEEN)
						cc = cc.and(builder.between(condition.getName(),
								condition.getLow(), condition.getHigh(), true,
								false));
					else if (condition.getType() == CompassConditionType.GE)
						cc = cc.and(builder.ge(condition.getName(), condition
								.getValue()));
					else if (condition.getType() == CompassConditionType.GT)
						cc = cc.and(builder.gt(condition.getName(), condition
								.getValue()));
					else if (condition.getType() == CompassConditionType.LE)
						cc = cc.and(builder.le(condition.getName(), condition
								.getValue()));
					else if (condition.getType() == CompassConditionType.LT)
						cc = cc.and(builder.lt(condition.getName(), condition
								.getValue()));
				query.setFilter(cc.toFilter());
			}
		}
		for (CompassSort sort : criteria.getSorts()) {
			query.addSort(sort.getName(), sort.getType(), sort.getDirection());
		}
		return query;
	}

	protected void doProcessBeforeDetach(CompassCriteria criteria,
			CompassSession session, CompassHits hits, int from, int size) {
		// do it in browser use javascript
		// highlight(criteria, session, hits, from, size);
	}

	protected void highlight(CompassCriteria criteria, CompassSession session,
			CompassHits hits, int from, int size) {
		int hitsLength = hits.getLength();
		if (from < 0) {
			from = 0;
			size = hits.getLength();
		}
		String[] highlightFields = criteria.getHighlightFields();
		if (highlightFields == null) {
			return;
		}
		for (int i = from; i < (from + size) && i < hitsLength; i++)
			for (String highlightField : highlightFields)
				hits.highlighter(i).fragment(highlightField);
	}

	protected void doProcessAfterDetach(CompassCriteria criteria,
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
