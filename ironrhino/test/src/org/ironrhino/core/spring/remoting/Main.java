package org.ironrhino.core.spring.remoting;

import org.ironrhino.ums.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "org/ironrhino/core/spring/remoting/client.xml" });
		UserManager hessianUserManager = ctx.getBean("hessianUserManager",
				UserManager.class);
		System.out.println(hessianUserManager.suggestName("test@google.com"));
		long time = System.currentTimeMillis();
		int loop = 1000;
		for (int i = 0; i < loop; i++)
			hessianUserManager.suggestName("test@google.com");
		System.out.println(System.currentTimeMillis() - time);

		UserManager httpInvokerUserManager = ctx.getBean(
				"httpInvokerUserManager", UserManager.class);
		System.out.println(httpInvokerUserManager
				.suggestName("test@google.com"));
		time = System.currentTimeMillis();
		for (int i = 0; i < loop; i++)
			httpInvokerUserManager.suggestName("test@google.com");
		System.out.println(System.currentTimeMillis() - time);
		ctx.close();

	}

}
