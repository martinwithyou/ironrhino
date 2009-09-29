package org.ironrhino.core.model;

import java.io.Serializable;
import java.util.Map;

public interface Customizable {

	public static final String CUSTOM_COMPONENT_NAME = "customProperties";

	public Map<String, Serializable> getCustomProperties();

	public void setCustomProperties(Map<String, Serializable> customProperties);

}
