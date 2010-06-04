package org.ironrhino.core.model;

import org.apache.commons.lang.StringUtils;
import org.compass.annotations.Index;
import org.compass.annotations.SearchableId;

public class BaseEntity extends Entity<String> {

	private static final long serialVersionUID = 5290168777920037800L;
	protected String id;

	@SearchableId(converter = "string", index = Index.NOT_ANALYZED)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (StringUtils.isNotBlank(id))
			this.id = id;
	}

	public boolean isNew() {
		return id == null || StringUtils.isBlank(id);
	}

}