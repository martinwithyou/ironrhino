package org.ironrhino.core.remoting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.ironrhino.security.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) throws IOException {
		testFunction();
	}

	public static void testPerformance() throws IOException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "org/ironrhino/core/remoting/client.xml" });

		UserManager httpInvokerUserManager = ctx.getBean(
				"httpInvokerUserManager", UserManager.class);
		for (int i = 0; i < 10; i++)
			System.out.println(httpInvokerUserManager.loadUserByUsername("test"
					+ i));
		System.out.println("-------------------------------------------------");
		long time = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++)
			httpInvokerUserManager.loadUserByUsername("test");
		System.out.println(System.currentTimeMillis() - time);
		ctx.close();

	}

	public static void testFunction() throws IOException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "org/ironrhino/core/remoting/client.xml" });
		UserManager hessianUserManager = ctx.getBean("hessianUserManager",
				UserManager.class);
		UserManager httpInvokerUserManager = ctx.getBean(
				"httpInvokerUserManager", UserManager.class);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.equals("exit"))
				break;
			try {
				System.out.println(hessianUserManager.loadUserByUsername(line));
				System.out.println(httpInvokerUserManager
						.loadUserByUsername(line));
			} catch (Exception e) {
				System.out.println("error:" + e.getMessage());
			}
		}
		ctx.close();

	}

}
