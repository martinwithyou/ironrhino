package org.ironrhino.core.search;

import java.util.Set;

import org.compass.annotations.Searchable;
import org.ironrhino.core.util.ClassScaner;

public class LocalCompassBean extends org.compass.spring.LocalCompassBean {

	public LocalCompassBean() {
		super();
		Set<Class> set = ClassScaner.scanAnnotated(
				ClassScaner.getAppPackages(), Searchable.class);
		String[] classMappings = new String[set.size()];
		int i = 0;
		for (Class c : set)
			classMappings[i++] = c.getName();
		setClassMappings(classMappings);
	}
}
