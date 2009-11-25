package org.ironrhino.hessian;

import org.ironrhino.ums.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "org/ironrhino/hessian/client.xml" });
		UserManager um = ctx.getBean(UserManager.class);
		System.out.println(um.suggestName("test@google.com"));
		ctx.close();

	}

}
