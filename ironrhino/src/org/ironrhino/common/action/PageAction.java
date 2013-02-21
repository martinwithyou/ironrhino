package org.ironrhino.common.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.ironrhino.common.Constants;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.hibernate.CriterionUtils;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.model.LabelValue;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.elasticsearch.ElasticSearchCriteria;
import org.ironrhino.core.search.elasticsearch.ElasticSearchService;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.struts.TemplateProvider;
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
	private transient ElasticSearchService<Page> elasticSearchService;

	@Autowired
	private transient TemplateProvider templateProvider;

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
		if (StringUtils.isBlank(keyword) || elasticSearchService == null) {
			DetachedCriteria dc = pageManager.detachedCriteria();
			Criterion filtering = CriterionUtils.filter(page, "id", "pagepath",
					"title");
			if (filtering != null)
				dc.add(filtering);
			if (StringUtils.isNotBlank(keyword))
				dc.add(CriterionUtils.like(keyword, MatchMode.ANYWHERE,
						"pagepath", "title"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("pagepath"));
			if (resultPage == null)
				resultPage = new ResultPage<Page>();
			resultPage.setCriteria(dc);
			resultPage = pageManager.findByResultPage(resultPage);
		} else {
			String query = keyword.trim();
			ElasticSearchCriteria criteria = new ElasticSearchCriteria();
			criteria.setQuery(query);
			criteria.setTypes(new String[] { "page" });
			criteria.addSort("displayOrder", false);
			criteria.addSort("pagepath", false);
			if (resultPage == null)
				resultPage = new ResultPage<Page>();
			resultPage.setCriteria(criteria);
			resultPage = elasticSearchService.search(resultPage);
		}
		return LIST;
	}

	@Override
	public String input() {
		String id = getUid();
		if (StringUtils.isNotBlank(id)) {
			page = pageManager.get(id);
			if (page == null)
				page = pageManager.findByNaturalId(id);
			if (page == null && !id.startsWith("/"))
				page = pageManager.findByNaturalId("/" + id);
		} else if (page != null) {
			if (page.getId() != null)
				page = pageManager.get(page.getId());
			else if (page.getPagepath() != null)
				page = pageManager.findByNaturalId(page.getPagepath());
		}
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
				page.setPagepath(path);
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
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.pagepath", trim = true, key = "validation.required") })
	public String save() {
		String path = page.getPagepath().trim().toLowerCase();
		if (!path.startsWith("/"))
			path = "/" + path;
		page.setPagepath(path);
		if (page.isNew()) {
			if (pageManager.findByNaturalId(page.getPagepath()) != null) {
				addFieldError("page.pagepath",
						getText("validation.already.exists"));
				return INPUT;
			}
		} else {
			Page temp = page;
			page = pageManager.get(page.getId());
			if (!page.getPagepath().equals(temp.getPagepath())
					&& pageManager.findByNaturalId(temp.getPagepath()) != null) {
				addFieldError("page.pagepath",
						getText("validation.already.exists"));
				return INPUT;
			}
			page.setPagepath(temp.getPagepath());
			page.setTags(temp.getTags());
			page.setDisplayOrder(temp.getDisplayOrder());
			page.setTitle(temp.getTitle());
			page.setHead(temp.getHead());
			page.setContent(temp.getContent());
		}
		pageManager.save(page);
		addActionMessage(getText("save.success"));
		return JSON;
	}

	@JsonConfig(propertyName = "page")
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.pagepath", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.content", trim = true, key = "validation.required") })
	public String draft() {
		String path = page.getPagepath().trim().toLowerCase();
		if (!path.startsWith("/"))
			path = "/" + path;
		page.setPagepath(path);
		if (page.isNew()) {
			if (pageManager.findByNaturalId(page.getPagepath()) != null) {
				addFieldError("page.pagepath",
						getText("validation.already.exists"));
				return INPUT;
			}
		} else {
			Page p = pageManager.get(page.getId());
			if (!page.getPagepath().equals(p.getPagepath())
					&& pageManager.findByNaturalId(page.getPagepath()) != null) {
				addFieldError("page.pagepath",
						getText("validation.already.exists"));
				return INPUT;
			}
		}
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
			pageManager.delete((Serializable[]) id);
			addActionMessage(getText("delete.success"));
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

	private List<LabelValue> suggestions;

	public List<LabelValue> getSuggestions() {
		return suggestions;
	}

	@JsonConfig(root = "suggestions")
	public String suggest() {
		if (StringUtils.isBlank(keyword))
			return NONE;
		Map<String, Integer> map = pageManager.findMatchedTags(keyword);
		suggestions = new ArrayList<LabelValue>(map.size());
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			LabelValue lv = new LabelValue();
			lv.setValue(entry.getKey());
			lv.setLabel(new StringBuilder(entry.getKey()).append("(")
					.append(entry.getValue()).append(")").toString());
			suggestions.add(lv);
		}
		return JSON;
	}

	private Map<String, String> files;

	@Inject
	private transient FileStorage fileStorage;

	@Inject
	private transient SettingControl settingControl;

	private String suffix;

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Map<String, String> getFiles() {
		return files;
	}

	@JsonConfig(root = "files")
	public String files() {
		String path = getHomePath(getUid());
		List<String> list = fileStorage.listFiles(path);

		files = new LinkedHashMap<String, String>();
		String[] suffixes = null;
		if (StringUtils.isNotBlank(suffix))
			suffixes = suffix.toLowerCase().split(",");
		for (String s : list) {
			if (suffixes != null) {
				boolean matches = false;
				for (String sf : suffixes)
					if (s.toLowerCase().endsWith("." + sf))
						matches = true;
				if (!matches)
					continue;
			}
			files.put(
					s,
					new StringBuilder(templateProvider.getAssetsBase())
							.append(settingControl.getStringValue(
									Constants.SETTING_KEY_FILE_STORAGE_PATH,
									"/assets")).append(path).append("/")
							.append(s).toString());
		}
		return JSON;
	}

	public static String getHomePath(String id) {
		return UploadAction.UPLOAD_DIR + "/page/" + id;
	}

}