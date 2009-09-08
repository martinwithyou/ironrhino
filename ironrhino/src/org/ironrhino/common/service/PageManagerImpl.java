package org.ironrhino.common.service;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Page;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.springframework.transaction.annotation.Transactional;

public class PageManagerImpl extends BaseManagerImpl<Page> implements
		PageManager {

	public static final String DELIMITER = "/*--DELIMITER--*/";

	@Override
	@Transactional
	@FlushCache(key = "${args[0].path}", namespace = "page")
	public void save(Page page) {
		page.setDraft(null);
		page.setDraftDate(null);
		super.save(page);
	}

	@Transactional(readOnly = true)
	@CheckCache(key = "${args[0]}", namespace = "page")
	public Page getByPath(String path) {
		return getByNaturalId(path);
	}

	@Override
	@Transactional
	public Page saveDraft(Page page) {
		Page p = get(page.getId());
		p.setDraftDate(new Date());
		p.setDraft((StringUtils.isBlank(page.getTitle()) ? "" : page.getTitle()
				+ DELIMITER)
				+ page.getContent());
		super.save(p);
		pullDraft(p);
		return p;
	}

	@Transactional(readOnly = true)
	public Page getDraftByPath(String path) {
		Page page = getByNaturalId(path);
		if (page == null || StringUtils.isBlank(page.getDraft()))
			return null;
		pullDraft(page);
		return page;
	}

	@Override
	@Transactional
	public Page dropDraft(String id) {
		Page page = get(id);
		save(page);
		return page;
	}

	@Override
	public void pullDraft(Page page) {
		String array[] = StringUtils.split(page.getDraft(), DELIMITER, 2);
		page.setContent(array[array.length - 1]);
		if (array.length == 2)
			page.setTitle(array[0]);
		else
			page.setTitle(null);
		page.setDraft(null);
	}

}
