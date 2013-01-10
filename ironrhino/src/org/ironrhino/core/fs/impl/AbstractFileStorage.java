package org.ironrhino.core.fs.impl;

import org.ironrhino.core.fs.FileStorage;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractFileStorage implements FileStorage {

	@Value("${fileStorage.uri:file:///tmp}")
	protected String uri;

	public void setUri(String uri) {
		this.uri = uri;
	}

}