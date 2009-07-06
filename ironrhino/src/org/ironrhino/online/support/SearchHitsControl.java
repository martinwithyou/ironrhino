package org.ironrhino.online.support;

import java.util.List;

import org.ironrhino.common.model.AggregateResult;

public interface SearchHitsControl {

	public void consume();
	
	public void put(String keywords, int totalHits);

	public abstract List<AggregateResult> suggest(String keyword);

}