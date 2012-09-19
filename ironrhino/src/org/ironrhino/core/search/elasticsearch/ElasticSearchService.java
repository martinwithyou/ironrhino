package org.ironrhino.core.search.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.SearchCriteria;
import org.ironrhino.core.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class ElasticSearchService<T> implements SearchService<T> {

	protected Logger log = LoggerFactory.getLogger(ElasticSearchService.class);

	@Inject
	private Client client;

	@Inject
	private IndexManager indexManager;

	public ResultPage<T> search(ResultPage resultPage) {
		return search(resultPage, null);
	}

	public ResultPage<T> search(ResultPage resultPage, Mapper mapper) {
		ElasticSearchCriteria criteria = (ElasticSearchCriteria) resultPage
				.getCriteria();
		if (criteria == null)
			return resultPage;
		String[] indices = criteria.getIndices();
		if (indices == null || indices.length == 0)
			indices = new String[] { indexManager.getIndexName() };
		SearchRequestBuilder srb = client.prepareSearch(indices);
		String[] types = criteria.getTypes();
		srb.setTypes(types);
		QueryStringQueryBuilder qb = new QueryStringQueryBuilder(
				criteria.getQuery());
		qb.defaultOperator(Operator.AND);
		srb.setQuery(qb);
		srb.setFrom(resultPage.getStart());
		srb.setSize(resultPage.getPageSize());
		if (criteria.getMinScore() > 0)
			srb.setMinScore(criteria.getMinScore());
		Map<String, Boolean> sorts = criteria.getSorts();
		for (Map.Entry<String, Boolean> entry : sorts.entrySet())
			srb.addSort(entry.getKey(), entry.getValue() ? SortOrder.DESC
					: SortOrder.ASC);
		try {
			SearchResponse response = srb.execute().get();
			SearchHits shs = response.getHits();
			if (shs != null) {
				resultPage.setTotalRecord(shs.getTotalHits());
				List list = new ArrayList(shs.getHits().length);
				resultPage.setResult(list);
				for (SearchHit sh : shs.getHits()) {
					Object data = indexManager.searchHitToEntity(sh);
					data = mapper == null ? data : mapper.map(data);
					if (data != null)
						list.add(data);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return resultPage;
	}

	public List<T> search(SearchCriteria searchCriteria) {
		return search(searchCriteria, null);
	}

	public List<T> search(SearchCriteria searchCriteria, Mapper mapper) {
		ElasticSearchCriteria criteria = (ElasticSearchCriteria) searchCriteria;
		if (criteria == null)
			return null;
		String[] indices = criteria.getIndices();
		if (indices == null || indices.length == 0)
			indices = new String[] { indexManager.getIndexName() };
		SearchRequestBuilder srb = client.prepareSearch(indices);
		String[] types = criteria.getTypes();
		if (types != null && types.length > 0)
			srb.setTypes(types);
		srb.setQuery(new QueryStringQueryBuilder(criteria.getQuery()));
		Map<String, Boolean> sorts = criteria.getSorts();
		for (Map.Entry<String, Boolean> entry : sorts.entrySet())
			srb.addSort(entry.getKey(), entry.getValue() ? SortOrder.DESC
					: SortOrder.ASC);
		List list = null;
		try {
			SearchResponse response = srb.execute().get();
			SearchHits shs = response.getHits();
			list = new ArrayList(shs.getHits().length);
			for (SearchHit sh : shs.getHits()) {
				Object data = indexManager.searchHitToEntity(sh);
				data = mapper == null ? data : mapper.map(data);
				if (data != null)
					list.add(data);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return list;
	}
}
