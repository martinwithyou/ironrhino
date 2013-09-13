package org.ironrhino.security.component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.metadata.Setup;
import org.ironrhino.core.metadata.SetupParameter;
import org.ironrhino.core.security.role.UserRole;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.service.UserManager;
import org.springframework.core.Ordered;

@Named
@Singleton
public class AdminUserSetup {

	@Inject
	private UserManager userManager;

	@Setup
	public User setup(
			@SetupParameter(defaultValue = "admin", displayOrder = Ordered.HIGHEST_PRECEDENCE, label = "admin.username") String username,
			@SetupParameter(defaultValue = "password", displayOrder = Ordered.HIGHEST_PRECEDENCE + 1, label = "admin.password") String password)
			throws Exception {
		if (userManager.countAll() > 0)
			return null;
		User admin = new User();
		admin.setUsername(username);
		admin.setPassword(CodecUtils.digest(password));
		admin.setEnabled(true);
		admin.getRoles().add(UserRole.ROLE_ADMINISTRATOR);
		userManager.save(admin);
		return admin;
	}

}
