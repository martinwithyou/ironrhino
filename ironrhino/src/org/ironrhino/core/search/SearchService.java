package org.ironrhino.core.search;

import java.util.List;

import org.ironrhino.core.model.ResultPage;

public interface SearchService<T> {

	public ResultPage search(ResultPage<T> resultPage);

	public List search(SearchCriteria searchCriteria);

	public ResultPage search(ResultPage<T> resultPage,  Mapper<T,T> mapper);

	public List search(SearchCriteria searchCriteria,  Mapper<T,T> mapper);

	public static interface Mapper<S,T> {

		public T map(S source);

	}

}
