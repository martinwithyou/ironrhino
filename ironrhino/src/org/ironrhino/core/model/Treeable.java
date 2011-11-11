package org.ironrhino.core.model;

import java.util.Collection;

public interface Treeable<T extends Treeable> {

	public int getLevel();

	public T getParent();

	public Collection<T> getChildren();
}
