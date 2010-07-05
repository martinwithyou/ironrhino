package org.ironrhino.common.action;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.security.model.UserRole;

@AutoConfig
public class DisplayPageAction extends BaseAction {

	private static final long serialVersionUID = -5865373753326653067L;

	private Page page;

	private boolean preview;

	@Inject
	private transient PageManager pageManager;

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public Page getPage() {
		return page;
	}

	@Override
	public String execute() {
		if (preview) {
			if (AuthzUtils.getRoleNames().contains(UserRole.ROLE_ADMINISTRATOR))
				page = pageManager.getDraftByPath(getUid());
		} else
			page = pageManager.getByPath(getUid());
		if (page == null) {
			try {
				ServletActionContext.getResponse().sendError(404);
				return NONE;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return SUCCESS;
	}

}
