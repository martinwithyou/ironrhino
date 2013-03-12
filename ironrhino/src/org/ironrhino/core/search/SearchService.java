package org.ironrhino.core.search;

import java.util.List;
import java.util.Map;

import org.ironrhino.core.model.ResultPage;

@SuppressWarnings("rawtypes")
public interface SearchService<T> {

	public ResultPage search(ResultPage<T> resultPage);

	public List search(SearchCriteria searchCriteria);

	public ResultPage search(ResultPage<T> resultPage, Mapper<T> mapper);

	public List search(SearchCriteria searchCriteria, Mapper<T> mapper);

	public List search(SearchCriteria searchCriteria, Mapper<T> mapper,
			int limit);

	public Map<String, Integer> countTermsByField(
			SearchCriteria searchCriteria, String field);

	public static interface Mapper<T> {

		public T map(T source);

	}

}
