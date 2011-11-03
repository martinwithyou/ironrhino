package org.ironrhino.core.model;

import java.util.List;

public interface Treeable<T extends Treeable> {

	public int getLevel();

	public T getParent();

	public List<T> getChildren();
}
