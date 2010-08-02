package org.ironrhino.common.action;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.core.struts.BaseAction;

public class SeriesPageAction extends BaseAction {

	private static final long serialVersionUID = -6230162538415715316L;

	@Inject
	protected PageManager pageManager;

	protected List<Page> pages;

	protected Page page;

	protected Page previousPage;

	protected Page nextPage;

	public Page getPreviousPage() {
		return previousPage;
	}

	public Page getNextPage() {
		return nextPage;
	}

	public Page getPage() {
		return page;
	}

	public List<Page> getPages() {
		return pages;
	}

	public String getActionName() {
		String actionName = getClass().getSimpleName();
		if (actionName.endsWith("Action"))
			actionName = actionName.substring(0, actionName.length() - 6);
		actionName = StringUtils.uncapitalize(actionName);
		return actionName;
	}

	@Override
	public String execute() {
		return p();
	}

	public String p() {
		pages = pageManager.getListByTag(getActionName());
		String path = getUid();
		if (StringUtils.isNotBlank(path)) {
			path = "/" + path;
			for (int i = 0; i < pages.size(); i++) {
				Page p = pages.get(i);
				if (p.getPath().equals(path)) {
					page = p;
					if (i > 0)
						previousPage = pages.get(i - 1);
					if (i < pages.size() - 1)
						nextPage = pages.get(i + 1);
					break;
				}
			}
		}
		if (page == null && pages.size() > 0)
			page = pages.get(0);
		return "chapter";
	}

}
