package org.ironrhino.common.util;

public interface EditAware {

	public Object getOldValue(String propertyName);

	public boolean isEdited(String propertyName);

}
