package org.ironrhino.core.remoting;

import org.ironrhino.security.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"classpath*:org/ironrhino/core/remoting/test_without_zk.xml");
		UserManager userManagerHessian = (UserManager) ctx
				.getBean("userManagerHessian");
		System.out.println(userManagerHessian
				.suggestUsername("test@google.com"));
		UserManager userManagerHttpInvoker = (UserManager) ctx
				.getBean("userManagerHttpInvoker");
		System.out.println(userManagerHttpInvoker
				.suggestUsername("test@google.com"));
		UserManager userManagerJsonCall = (UserManager) ctx
				.getBean("userManagerJsonCall");
		System.out.println(userManagerJsonCall
				.suggestUsername("test@google.com"));
		int loop = 1000;
		long time = System.currentTimeMillis();
		for (int i = 0; i < loop; i++)
			userManagerHessian.suggestUsername("test@google.com");
		System.out.println("hessian:" + (System.currentTimeMillis() - time)
				+ "ms");
		time = System.currentTimeMillis();
		for (int i = 0; i < loop; i++)
			userManagerHttpInvoker.suggestUsername("test@google.com");
		System.out.println("httpinvoker:" + (System.currentTimeMillis() - time)
				+ "ms");
		time = System.currentTimeMillis();
		for (int i = 0; i < loop; i++)
			userManagerJsonCall.suggestUsername("test@google.com");
		System.out.println("jsoncall:" + (System.currentTimeMillis() - time)
				+ "ms");
		ctx.close();
	}

}
