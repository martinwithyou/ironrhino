package org.ironrhino.common.service;

import java.util.Date;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Page;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.springframework.transaction.annotation.Transactional;

@Singleton@Named("pageManager")
public class PageManagerImpl extends BaseManagerImpl<Page> implements
		PageManager {

	public static final String DELIMITER = "@@@@@@@@@@";

	@Override
	@Transactional
	@FlushCache(key = "${args[0].path}", namespace = "page")
	public void save(Page page) {
		page.setDraft(null);
		page.setDraftDate(null);
		super.save(page);
	}

	@Override
	@Transactional
	@FlushCache(key = "${args[0].path}", namespace = "page")
	public void delete(Page page) {
		super.delete(page);
	}

	@Transactional(readOnly = true)
	@CheckCache(key = "${args[0]}", namespace = "page")
	public Page getByPath(String path) {
		Page page = findByNaturalId(path);
		if (page != null)
			page.setDraft(null);
		return page;
	}

	@Override
	@Transactional
	public Page saveDraft(Page page) {
		Page p = get(page.getId());
		if (p == null) {
			p = page;
		}
		p.setDraftDate(new Date());
		p.setDraft(page.getPath()
				+ DELIMITER
				+ (StringUtils.isBlank(page.getTitle()) ? "" : page.getTitle()
						+ DELIMITER) + page.getContent());
		super.save(p);
		pullDraft(p);
		return p;
	}

	@Transactional(readOnly = true)
	public Page getDraftByPath(String path) {
		Page page = findByNaturalId(path);
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
		String array[] = StringUtils.split(page.getDraft(), DELIMITER, 3);
		page.setPath(array[0]);
		page.setContent(array[array.length - 1]);
		if (array.length == 3)
			page.setTitle(array[1]);
		else
			page.setTitle(null);
	}

}
