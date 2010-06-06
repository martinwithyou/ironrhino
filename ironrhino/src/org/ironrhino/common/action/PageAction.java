package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.struts.BaseAction;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig
public class PageAction extends BaseAction {

	private static final long serialVersionUID = 67252386921293136L;

	private Page page;

	private boolean draft;

	private ResultPage<Page> resultPage;

	@Inject
	private transient PageManager pageManager;

	private String cmsPath = "/p/";

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
	public String execute() {
		DetachedCriteria dc = pageManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage<Page>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("path"));
		resultPage = pageManager.findByResultPage(resultPage);
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
	@JsonConfig(propertyName = { "page" })
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
			page.setTitle(temp.getTitle());
			page.setContent(temp.getContent());
		}
		pageManager.save(page);
		addActionMessage(getText("save.success"));
		return INPUT;
	}

	@JsonConfig(propertyName = { "page" })
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
						addActionError(temp.getPath()
								+ getText("delete.forbidden",
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

}
