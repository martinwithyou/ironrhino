package org.ironrhino.core.remoting;

import java.util.List;
import java.util.Map;

public interface ServiceRegistry {

	public Map<String, List<String>> getImportServices();

	public Map<String, Object> getExportServices();

}