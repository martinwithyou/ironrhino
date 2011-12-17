package org.ironrhino.core.search;

import java.util.List;

import org.ironrhino.core.model.ResultPage;

public interface SearchService {

	public ResultPage search(ResultPage resultPage);

	public List search(SearchCriteria searchCriteria);

}
