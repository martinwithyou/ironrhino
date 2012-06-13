package org.ironrhino.daq.action;

import java.util.Date;

import javax.inject.Inject;

import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.daq.model.Acquisition;
import org.ironrhino.security.model.UserRole;

@AutoConfig
@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_ANONYMOUS)
public class AcquireAction extends BaseAction {

	private static final long serialVersionUID = 8377480901050862919L;

	@Inject
	private EntityManager<Acquisition> entityManager;

	@Override
	public String execute() {
		if (requestBody != null) {
			try {
				Acquisition acq = JsonUtils.fromJson(requestBody,
						Acquisition.class);
				acq.setTime(new Date());
				acq.setIp(RequestUtils.getRemoteAddr(ServletActionContext
						.getRequest()));
				entityManager.save(acq);
			} catch (Exception e) {
				addActionError(e.getMessage());
				return JSON;
			}
		}
		return NONE;
	}

}
