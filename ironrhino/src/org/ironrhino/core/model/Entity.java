package org.ironrhino.core.model;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.ironrhino.common.util.AnnotationUtils;
import org.ironrhino.core.metadata.NaturalId;

public abstract class Entity implements Serializable {

	public abstract boolean isNew();

	@Override
	public int hashCode() {
		Map<String, Object> map = AnnotationUtils
				.getAnnotatedPropertyNameAndValues(this, NaturalId.class);
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.getId());
		for (Object value : map.values())
			builder.append(value);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (!(this.getClass().equals(object.getClass())))
			return false;
		Entity that = (Entity) object;
		return this.toIdentifiedString().equals(that.toIdentifiedString());
	}

	private String toIdentifiedString() {
		Map<String, Object> map = AnnotationUtils
				.getAnnotatedPropertyNameAndValues(this, NaturalId.class);
		return getClass().getName() + "{id=" + getId() + ",naturalId="
				+ map.toString() + "}";
	}

	@Override
	public String toString() {
		Map<String, Object> map = AnnotationUtils
				.getAnnotatedPropertyNameAndValues(this, NaturalId.class);
		return getClass().getName() + "{id=" + getId() + ",naturalId="
				+ map.toString() + "}";
	}

	private Object getId() {
		try {
			return BeanUtils.getProperty(this, "id");
		} catch (Exception e) {
			return null;
		}
	}

}
