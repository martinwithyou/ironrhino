package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.compass.core.CompassHit;
import org.compass.core.support.search.CompassSearchResults;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.CompassCriteria;
import org.ironrhino.core.search.CompassSearchService;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class PageAction extends BaseAction {

	private static final long serialVersionUID = 67252386921293136L;

	private Page page;

	private boolean draft;

	private ResultPage<Page> resultPage;

	@Inject
	private transient PageManager pageManager;

	private String cmsPath = "/p/";

	@Autowired(required = false)
	private transient CompassSearchService compassSearchService;

	@com.opensymphony.xwork2.inject.Inject(value = "ironrhino.cmsPath", required = false)
	public void setCmsPath(String val) {
		cmsPath = val;
	}

	public boolean isDraft() {
		return draft;
	}

	public String getCmsPath() {
		if (cmsPath.endsWith("/"))
			return cmsPath.substring(0, cmsPath.length() - 1);
		return cmsPath;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public ResultPage<Page> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Page> resultPage) {
		this.resultPage = resultPage;
	}

	@Override
	public String list() {
		if (StringUtils.isBlank(keyword) || compassSearchService == null) {
			DetachedCriteria dc = pageManager.detachedCriteria();
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("path"));
			if (resultPage == null)
				resultPage = new ResultPage<Page>();
			resultPage.setDetachedCriteria(dc);
			resultPage = pageManager.findByResultPage(resultPage);
		} else {
			String query = keyword.trim();
			CompassCriteria cc = new CompassCriteria();
			cc.setQuery(query);
			cc.setAliases(new String[] { "page" });
			cc.addSort("displayOrder", "INT", false);
			cc.addSort("path", null, false);
			if (resultPage == null)
				resultPage = new ResultPage();
			cc.setPageNo(resultPage.getPageNo());
			cc.setPageSize(resultPage.getPageSize());
			CompassSearchResults searchResults = compassSearchService
					.search(cc);
			resultPage.setTotalRecord(searchResults.getTotalHits());
			CompassHit[] hits = searchResults.getHits();
			if (hits != null) {
				List list = new ArrayList(hits.length);
				for (CompassHit ch : searchResults.getHits()) {
					list.add(ch.getData());
				}
				resultPage.setResult(list);
			} else {
				resultPage.setResult(Collections.EMPTY_LIST);
			}
		}
		return LIST;
	}
	
	public static void main(String[] args) {
		String keyword = "tags:test AND tags:haha";
		String tag = keyword.replace("tags:", "");
		tag = tag.replace(" AND ", ",");
		System.out.println(tag);
	}

	@Override
	public String input() {
		page = pageManager.get(getUid());
		if (page == null) {
			page = new Page();
			if (StringUtils.isNotBlank(keyword) && keyword.startsWith("tags:")) {
				String tags = keyword.replace("tags:", "");
				tags = tags.replace(" AND ", ",");
				String tag = tags.split(",")[0];
				int count = pageManager.findListByTag(tag).size();
				String path = null;
				while (true) {
					path = "/" + tag + (++count);
					if (pageManager.getByPath(path) == null)
						break;
				}
				page.setPath(path);
				page.setTagsAsString(tags);
			}
		} else {
			if (StringUtils.isNotBlank(page.getDraft())) {
				draft = true;
				pageManager.pullDraft(page);
			}
		}
		return INPUT;
	}

	@Override
	@JsonConfig(propertyName = "page")
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.path", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.content", trim = true, key = "validation.required") })
	public String save() {
		String path = page.getPath().trim().toLowerCase();
		if (!path.startsWith("/"))
			path = "/" + path;
		page.setPath(path);
		if (page.isNew()) {
			if (pageManager.findByNaturalId(page.getPath()) != null) {
				addFieldError("page.path", getText("validation.already.exists"));
				return INPUT;
			}
		} else {
			Page temp = page;
			page = pageManager.get(page.getId());
			if (!page.getPath().equals(temp.getPath())
					&& pageManager.findByNaturalId(temp.getPath()) != null) {
				addFieldError("page.path", getText("validation.already.exists"));
				return INPUT;
			}
			page.setPath(temp.getPath());
			page.setTags(temp.getTags());
			page.setDisplayOrder(temp.getDisplayOrder());
			page.setTitle(temp.getTitle());
			page.setContent(temp.getContent());
		}
		pageManager.save(page);
		addActionMessage(getText("save.success"));
		return INPUT;
	}

	@JsonConfig(propertyName = "page")
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.path", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.content", trim = true, key = "validation.required") })
	public String draft() {
		String path = page.getPath().trim().toLowerCase();
		if (!path.startsWith("/"))
			path = "/" + path;
		page.setPath(path);
		page = pageManager.saveDraft(page);
		pageManager.pullDraft(page);
		draft = true;
		return INPUT;
	}

	public String drop() {
		page = pageManager.dropDraft(page.getId());
		return INPUT;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			List<Page> list;
			if (id.length == 1) {
				list = new ArrayList<Page>(1);
				list.add(pageManager.get(id[0]));
			} else {
				DetachedCriteria dc = pageManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = pageManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (Page temp : list) {
					if (!pageManager.canDelete(temp)) {
						addActionError(getText("delete.forbidden",
								new String[] { temp.getPath() }));
						deletable = false;
						break;
					}
				}
				if (deletable) {
					for (Page temp : list)
						pageManager.delete(temp);
					addActionMessage(getText("delete.success"));
				}
			}
		}
		return SUCCESS;
	}

	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.content", trim = true, key = "validation.required") })
	public String editme() {
		String content = page.getContent();
		page = pageManager.get(getUid());
		page.setContent(content);
		pageManager.save(page);
		addActionMessage(getText("save.success"));
		return JSON;
	}
}
