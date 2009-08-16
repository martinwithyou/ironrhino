package org.ironrhino.core.model;

import java.util.Collection;
import java.util.HashSet;

import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;

public class BaseTreeableEntity<T extends BaseTreeableEntity> extends
		AbstractTreeableEntity<T> implements Ordered {

	protected int displayOrder;

	protected T parent;

	protected Collection<T> children = new HashSet<T>(0);

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int compareTo(Object object) {
		if (!(object instanceof BaseTreeableEntity))
			return 0;
		BaseTreeableEntity entity = (BaseTreeableEntity) object;
		if (this.getDisplayOrder() != entity.getDisplayOrder())
			return this.getDisplayOrder() - entity.getDisplayOrder();
		return this.getName().compareTo(entity.getName());
	}

	@NotInCopy
	@NotInJson
	public Collection<T> getChildren() {
		return children;
	}

	public void setChildren(Collection<T> children) {
		this.children = children;
	}

	public boolean isLeaf() {
		return this.children == null || this.children.size() == 0;
	}

	public boolean isHasChildren() {
		return !isLeaf();
	}

	public boolean isRoot() {
		return this.parent == null;
	}

	@NotInCopy
	@NotInJson
	public T getParent() {
		return parent;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}

	public T getDescendantOrSelfById(Integer id) {
		if (id == null)
			throw new IllegalArgumentException("id must not be null");
		if (id.equals(this.getId()))
			return (T) this;
		for (T t : getChildren()) {
			if (id.equals(t.getId())) {
				return t;
			} else {
				T tt = (T) t.getDescendantOrSelfById(id);
				if (tt != null)
					return tt;
			}
		}
		return null;
	}
}
