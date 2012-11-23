package org.ironrhino.core.hibernate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import javax.persistence.Entity;

import org.ironrhino.core.util.ClassScaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFactoryBean extends
		org.springframework.orm.hibernate4.LocalSessionFactoryBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Class<?>[] annotatedClasses;

	public void setAnnotatedClasses(Class<?>[] annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	public void afterPropertiesSet() throws IOException {
		Set<Class<?>> classes = ClassScaner.scanAnnotated(
				ClassScaner.getAppPackages(), Entity.class);
		if (annotatedClasses != null)
			classes.addAll(Arrays.asList(annotatedClasses));
		annotatedClasses = classes.toArray(new Class<?>[0]);
		logger.info("annotatedClasses: ");
		for (Class<?> clz : annotatedClasses)
			logger.info(clz.getName());
		super.setAnnotatedClasses(annotatedClasses);
		super.afterPropertiesSet();
	}
}
