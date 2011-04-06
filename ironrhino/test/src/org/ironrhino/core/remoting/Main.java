package org.ironrhino.core.remoting;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.ironrhino.security.service.UserManager;

public class Main {

	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:org/ironrhino/core/remoting/test.xml");
		UserManager um = ctx.getBean(UserManager.class);
		System.out.println(um.suggestUsername("test@google.com"));
		ctx.close();
	}

}
