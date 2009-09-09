package org.ironrhino.common.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;

import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig
public class PageAction extends BaseAction {

	private static final long serialVersionUID = 67252386921293136L;

	private Page page;

	private boolean draft;

	private ResultPage<Page> resultPage;

	private transient PageManager pageManager;

	private String cmsPath = "/p/";

	@Inject(value = "ironrhino.cmsPath", required = false)
	public void setCmsPath(String val) {
		cmsPath = val;
	}

	public boolean isDraft() {
		return draft;
	}

	public String getCmsPath() {
		if (cmsPath.endsWith("/"))
			return cmsPath.substring(0,cmsPath.length()-1);
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

	public void setPageManager(PageManager pageManager) {
		this.pageManager = pageManager;
	}

	@Override
	public String execute() {
		DetachedCriteria dc = pageManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage<Page>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("path"));
		resultPage = pageManager.getResultPage(resultPage);
		return LIST;
	}

	@Override
	public String input() {
		page = pageManager.get(getUid());
		if (page == null) {
			page = new Page();
		} else {
			if (StringUtils.isNotBlank(page.getDraft())) {
				draft = true;
				pageManager.pullDraft(page);
			}
		}
		return INPUT;
	}

	@Override
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.path", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "page.content", trim = true, key = "validation.required") })
	public String save() {
		String path = page.getPath().trim().toLowerCase();
		if (!path.startsWith("/"))
			path = "/" + path;
		page.setPath(path);
		if (page.isNew()) {
			if (pageManager.getByNaturalId(page.getPath()) != null) {
				addFieldError("page.path", getText("validation.already.exists"));
				return INPUT;
			}
		} else {
			Page temp = page;
			page = pageManager.get(page.getId());
			if (!page.getPath().equals(temp.getPath())
					&& pageManager.getByNaturalId(temp.getPath()) != null) {
				addFieldError("page.path", getText("validation.already.exists"));
				return INPUT;
			}
			page.setPath(temp.getPath());
			page.setTitle(temp.getTitle());
			page.setContent(temp.getContent());
		}
		pageManager.save(page);
		addActionMessage(getText("save.success"));
		return INPUT;
	}

	public String draft() {
		page = pageManager.saveDraft(page);
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
			DetachedCriteria dc = pageManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Page> list = pageManager.getListByCriteria(dc);
			if (list.size() > 0) {
				for (Page page : list)
					pageManager.delete(page);
				addActionMessage(getText("delete.success"));
			}
		}
		return SUCCESS;
	}

}
