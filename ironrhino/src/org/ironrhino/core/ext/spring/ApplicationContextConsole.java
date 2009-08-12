package org.ironrhino.core.ext.spring;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class ApplicationContextConsole {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	private ApplicationContext ctx;

	private Map<String, Object> beans;

	public Object execute(String expression) throws Exception {
		if (beans == null) {
			beans = new HashMap<String, Object>();
			String[] beanNames = ctx.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				if (StringUtils.isAlphanumeric(beanName)
						&& ctx.isSingleton(beanName))
					beans.put(beanName, ctx.getBean(beanName));
			}
		}
		try {
			return MVEL.eval(expression, beans);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void main(String...strings){
		System.out.println(MVEL.eval("a='haha\nhaha\n';a",new HashMap()));
	}
}