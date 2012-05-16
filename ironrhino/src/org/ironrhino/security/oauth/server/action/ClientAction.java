package org.ironrhino.security.oauth.server.action;

import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.EntityAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.ironrhino.security.oauth.server.model.Client;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class ClientAction extends EntityAction {

	private static final long serialVersionUID = -4833030589707102084L;

	private Client client;

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	@InputConfig(resultName = "apply")
	@Authorize(ifAllGranted = UserRole.ROLE_BUILTIN_USER)
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "client.name", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "client.redirectUri", trim = true, key = "validation.required") })
	public String apply() {
		BaseManager<Client> entityManager = getEntityManager(Client.class);
		client.setOwner(AuthzUtils.getUserDetails(User.class));
		entityManager.save(client);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

}
