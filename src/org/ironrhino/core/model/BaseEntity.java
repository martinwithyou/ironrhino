package org.ironrhino.core.model;

import org.apache.commons.lang3.StringUtils;
import org.compass.annotations.Index;
import org.compass.annotations.SearchableId;
import org.ironrhino.core.metadata.NotInJson;

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

	@NotInJson
	public boolean isNew() {
		return id == null || StringUtils.isBlank(id);
	}

}