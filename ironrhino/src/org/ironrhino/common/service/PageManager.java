package org.ironrhino.common.service;

import java.util.List;

import org.ironrhino.common.model.Page;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.service.BaseManager;

public interface PageManager extends BaseManager<Page> {

	public void save(Page page);
	
	public void delete(Page page);

	public Page getByPath(String path);

	public Page saveDraft(Page page);

	public Page getDraftByPath(String path);

	public Page dropDraft(String id);

	public void pullDraft(Page page);
	
	public List<Page> findListByTag(String tag);
	
	public List<Page> findListByTag(String... tag);
	
	public ResultPage<Page> findResultPageByTag(ResultPage<Page> resultPage,String tag);
	
	public ResultPage<Page> findResultPageByTag(ResultPage<Page> resultPage,String... tag);

}
