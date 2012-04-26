package org.ironrhino.core.search.compass;

import java.util.Set;

import org.compass.annotations.Searchable;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;
import org.compass.spring.LocalCompassBeanPostProcessor;
import org.ironrhino.core.util.ClassScaner;

public class LocalCompassBean extends org.compass.spring.LocalCompassBean {

	public LocalCompassBean() {
		super();
		setPostProcessor(new LocalCompassBeanPostProcessor() {
			public void process(CompassConfiguration config)
					throws ConfigurationException {
				Set<Class<?>> set = ClassScaner.scanAnnotated(
						ClassScaner.getAppPackages(), Searchable.class);
				for (Class<?> c : set)
					config.addClass(c);
			}
		});
	}
}
