package org.ironrhino.common.action;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.spring.ApplicationContextConsole;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig
public class ConsoleAction extends BaseAction {

	private static final long serialVersionUID = 8180265410790553918L;

	private String cmd;

	@Autowired
	private transient ApplicationContextConsole applicationContextConsole;

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	@Override
	@InputConfig(resultName = "success")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "cmd", trim = true, key = "validation.required") })
	public String execute() {
		try {
			Object o = applicationContextConsole.execute(cmd);
			addActionMessage(getText("operate.success") + ":"
					+ String.valueOf(o));
		} catch (Exception e) {
			addActionError(getText("error") + ":" + e.getMessage());
		}
		return SUCCESS;
	}

}
