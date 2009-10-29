package org.ironrhino.common.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

import com.opensymphony.xwork2.util.Element;
import com.opensymphony.xwork2.util.Key;

@AutoConfig
public class AmmapAction extends BaseAction {

	private static final long serialVersionUID = 4241262885347832652L;

	private Map<String, String> settings = new HashMap<String, String>();

	@Key(String.class)
	@Element(String.class)
	public Map<String, String> getSettings() {
		return settings;
	}

	@Override
	public String execute() {

		return SUCCESS;
	}

	public String settings() {
		ServletActionContext.getResponse().setContentType("application/xml");
		return "settings";
	}
}
