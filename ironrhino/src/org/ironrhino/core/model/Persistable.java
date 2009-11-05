package org.ironrhino.core.model;

import java.io.Serializable;

public interface Persistable<T extends Serializable> extends Serializable {

	public boolean isNew();
	
	public T getId();

}
