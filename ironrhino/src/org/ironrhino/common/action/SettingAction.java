package org.ironrhino.common.action;

import java.util.Collection;
import java.util.Map;

import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

@AutoConfig
public class SettingAction extends BaseAction {

	private static final long serialVersionUID = -7824355496392523420L;

	@Autowired
	private transient SettingControl settingControl;

	public Map<String, String> getSettings() {
		return settingControl.getAll();
	}

	public Collection<Map.Entry<String, String>> getList() {
		return getSettings().entrySet();
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	@Override
	public String save() {
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

}
