package org.ironrhino.core.lb;

public interface Policy<T> {

	public T pick();
	
}
