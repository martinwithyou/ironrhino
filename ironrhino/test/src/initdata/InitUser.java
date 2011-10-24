package initdata;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitUser {

	static BaseManager baseManager;

	public static void main(String... strings) throws Exception {
		System.setProperty("app.name", "ironrhino");
		System.setProperty("ironrhino.home", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		System.setProperty("ironrhino.context", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "initdata/applicationContext-initdata.xml",
						"resources/spring/applicationContext-ds.xml",
						"resources/spring/applicationContext-hibernate.xml" });
		BaseManager baseManager = (BaseManager) ctx.getBean("baseManager");
		User admin = new User();
		admin.setUsername("admin");
		admin.setPassword(CodecUtils.digest("password"));
		admin.setEnabled(true);
		admin.getRoles().add(UserRole.ROLE_ADMINISTRATOR);
		baseManager.save(admin);
	}

}
