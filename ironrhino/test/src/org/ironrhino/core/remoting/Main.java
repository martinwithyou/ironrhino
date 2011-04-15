package org.ironrhino.core.remoting;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.ironrhino.security.service.UserManager;

public class Main {

	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:org/ironrhino/core/remoting/test.xml");
		UserManager userManagerHessian = (UserManager)ctx.getBean("userManagerHessian");
		System.out.println(userManagerHessian.suggestUsername("test@google.com"));
		System.out.println(userManagerHessian.suggestUsername("test@google.com"));
		System.out.println(userManagerHessian.suggestUsername("test@google.com"));
		System.out.println(userManagerHessian.suggestUsername("test@google.com"));
		UserManager userManagerHttpInvoker = (UserManager)ctx.getBean("userManagerHttpInvoker");
		System.out.println(userManagerHttpInvoker.suggestUsername("test@google.com"));
		System.out.println(userManagerHttpInvoker.suggestUsername("test@google.com"));
		System.out.println(userManagerHttpInvoker.suggestUsername("test@google.com"));
		System.out.println(userManagerHttpInvoker.suggestUsername("test@google.com"));
		ctx.close();
	}

}
