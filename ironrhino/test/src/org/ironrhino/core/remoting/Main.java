package org.ironrhino.core.remoting;

import org.ironrhino.ums.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "org/ironrhino/core/remoting/client.xml" });
		UserManager hessianUserManager = ctx.getBean("hessianUserManager",
				UserManager.class);
		System.out.println(hessianUserManager.suggestName("test@google.com"));
		UserManager httpInvokerUserManager = ctx.getBean(
				"httpInvokerUserManager", UserManager.class);
		System.out.println(httpInvokerUserManager
				.suggestName("test@google.com"));
		ctx.close();

	}

}
