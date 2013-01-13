package org.ironrhino.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;

@Searchable(root = false, alias = "simpleElement", boost = 3)
public class SimpleElement implements Serializable {

	private static final long serialVersionUID = -2465127797400483349L;

	private String value;

	public SimpleElement() {
	}

	public SimpleElement(String value) {
		this.value = value;
	}

	@SearchableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static void fillCollectionWithString(
			Collection<SimpleElement> collection, String string) {
		if (StringUtils.isBlank(string)) {
			collection.clear();
			return;
		}
		String[] array = string.split(",");
		Set<String> set = new LinkedHashSet<String>();
		collection.clear();
		for (String value : array) {
			value = value.trim();
			if (!"".equals(value))
				set.add(value);
		}
		for (String value : set)
			collection.add(new SimpleElement(value));
	}
}