package org.ironrhino;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.ironrhino.security.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitData {

	static BaseManager baseManager;

	public static void main(String... strings) throws Exception {
		System.setProperty("app.name", "ironrhino");
		System.setProperty("ironrhino.home", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		System.setProperty("ironrhino.context", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {
						"resources/spring/applicationContext-common.xml",
						"resources/spring/applicationContext-ds.xml",
						"resources/spring/applicationContext-hibernate.xml" });
		UserManager userManager = (UserManager) ctx.getBean("userManager");
		User admin = new User();
		admin.setUsername("admin");
		admin.setLegiblePassword("password");
		admin.setEnabled(true);
		admin.getRoles().add(UserRole.ROLE_ADMINISTRATOR);
		userManager.save(admin);
	}

}
