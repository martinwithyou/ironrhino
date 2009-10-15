package org.ironrhino.common.action;

import java.util.Collection;
import java.util.Map;

import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.util.Element;
import com.opensymphony.xwork2.util.Key;

@AutoConfig
public class SettingAction extends BaseAction {

	private static final long serialVersionUID = -7824355496392523420L;

	@Autowired
	private transient SettingControl settingControl;

	@Key(String.class)
	@Element(String.class)
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

}
