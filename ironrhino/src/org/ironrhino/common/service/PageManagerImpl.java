package org.ironrhino.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.compass.core.CompassHit;
import org.compass.core.support.search.CompassSearchResults;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Page;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.search.CompassCriteria;
import org.ironrhino.core.search.CompassSearchService;
import org.ironrhino.core.service.BaseManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@Named("pageManager")
public class PageManagerImpl extends BaseManagerImpl<Page> implements
		PageManager {

	public static final String DELIMITER = "@@@@@@@@@@";

	@Autowired(required = false)
	private transient CompassSearchService compassSearchService;

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

	@Transactional
	public Page saveDraft(Page page) {
		Page p = get(page.getId());
		boolean isnew = false;
		if (p == null) {
			isnew = true;
			p = page;
		}
		p.setDraftDate(new Date());
		p.setDraft(page.getPath()
				+ DELIMITER
				+ (StringUtils.isBlank(page.getTitle()) ? "" : page.getTitle()
						+ DELIMITER) + page.getContent());
		if (isnew) {
			p.setTitle("");
			p.setContent("");
		}
		super.save(p);
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

	@Transactional
	public Page dropDraft(String id) {
		Page page = get(id);
		page.setDraft(null);
		page.setDraftDate(null);
		super.save(page);
		return page;
	}

	public void pullDraft(Page page) {
		String array[] = StringUtils.split(page.getDraft(), DELIMITER, 3);
		page.setPath(array[0]);
		page.setContent(array[array.length - 1]);
		if (array.length == 3)
			page.setTitle(array[1]);
		else
			page.setTitle(null);
	}

	public List<Page> getListByTag(String tag) {
		if (StringUtils.isBlank(tag))
			return Collections.EMPTY_LIST;
		List list;
		if (compassSearchService != null) {
			String query = "tags:" + tag;
			CompassCriteria cc = new CompassCriteria();
			cc.setQuery(query);
			cc.setAliases(new String[] { "page" });
			CompassSearchResults searchResults = compassSearchService
					.search(cc);
			CompassHit[] hits = searchResults.getHits();
			if (hits == null)
				return Collections.EMPTY_LIST;
			list = new ArrayList(hits.length);
			for (CompassHit ch : searchResults.getHits()) {
				list.add(ch.getData());
			}
		} else {
			DetachedCriteria dc = detachedCriteria();
			dc.add(Restrictions.or(Restrictions.eq("tagsAsString", tag),
					Restrictions.or(Restrictions.like("tagsAsString",
							tag + ",", MatchMode.START), Restrictions.or(
							Restrictions.like("tagsAsString", "," + tag,
									MatchMode.END), Restrictions.like(
									"tagsAsString", "," + tag + ",",
									MatchMode.ANYWHERE)))));
			list = findListByCriteria(dc);
		}
		Collections.sort(list);
		return list;
	}

}
