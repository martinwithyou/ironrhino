package org.ironrhino.core.search;

import java.util.List;

import org.ironrhino.core.model.ResultPage;

public interface SearchService {

	public ResultPage search(ResultPage resultPage);

	public List search(SearchCriteria searchCriteria);

	public ResultPage search(ResultPage resultPage, Mapper mapper);

	public List search(SearchCriteria searchCriteria, Mapper mapper);

	public static interface Mapper {

		public Object map(Object source);

	}

}
