package org.ironrhino.common.action;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.struts.BaseAction;

public class ColumnPageAction extends BaseAction {

	private static final long serialVersionUID = -7189565572156313486L;

	@Inject
	protected PageManager pageManager;

	@Inject
	protected SettingControl settingControl;

	protected String column;

	protected String[] columns;

	protected ResultPage<Page> resultPage;

	private String actionName;

	protected Page page;

	public ResultPage<Page> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Page> resultPage) {
		this.resultPage = resultPage;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String[] getColumns() {
		return columns;
	}

	public Page getPage() {
		return page;
	}

	public String getActionName() {
		if (actionName == null) {
			actionName = getClass().getSimpleName();
			if (actionName.endsWith("Action"))
				actionName = actionName.substring(0, actionName.length() - 6);
			actionName = StringUtils.uncapitalize(actionName);
		}
		return actionName;
	}

	@Override
	public String execute() {
		return list();
	}

	@Override
	public String list() {
		columns = settingControl.getStringArray(getActionName() + ".column");
		column = getUid();
		if (StringUtils.isBlank(column) && columns != null
				&& columns.length > 0)
			column = columns[0];
		if (resultPage == null)
			resultPage = new ResultPage<Page>();
		if (column == null)
			resultPage = pageManager.findResultPageByTag(resultPage,
					getActionName());
		else
			resultPage = pageManager.findResultPageByTag(resultPage,
					new String[] { getActionName(), column });
		return "column";
	}

	public String p() {
		columns = settingControl.getStringArray(getActionName() + ".column");
		String path = getUid();
		if (StringUtils.isNotBlank(path)) {
			path = "/" + path;
			page = pageManager.getByPath(path);
		}
		return "columnpage";
	}
}
