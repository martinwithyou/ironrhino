package org.ironrhino.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;

public class BaseTreeableEntity<T extends BaseTreeableEntity> extends
		Entity<Long> implements Treeable<T>, Ordered {

	private static final long serialVersionUID = 2462271646391940930L;

	protected Long id;

	protected String fullId;

	protected String name;

	protected int level;

	protected int displayOrder;

	protected T parent;

	protected Collection<T> children = new HashSet<T>(0);

	@NotInJson
	public String getFullId() {
		return fullId;
	}

	public void setFullId(String fullId) {
		this.fullId = fullId;
	}

	@SearchableId(converter = "long")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@NotInJson
	public boolean isNew() {
		return id == null || id == 0;
	}

	@NotInCopy
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@NaturalId
	@SearchableProperty(boost = 2)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotInJson
	public String getFullname() {
		String fullname = name;
		BaseTreeableEntity e = this;
		while ((e = (BaseTreeableEntity) e.getParent()) != null) {
			fullname = e.getName() + fullname;
		}
		return fullname;
	}

	@NotInJson
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

	public T getDescendantOrSelfById(Long id) {
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

	public List<T> getDescendants() {
		List<T> ids = new ArrayList<T>();
		if (!this.isLeaf())
			for (Object obj : this.getChildren()) {
				collect((T) obj, ids);
			}
		return ids;
	}

	public List<T> getDescendantsAndSelf() {
		List<T> ids = new ArrayList<T>();
		collect((T) this, ids);
		return ids;
	}

	private void collect(T node, Collection<T> coll) {
		coll.add(node);
		if (node.isLeaf())
			return;
		for (Object obj : node.getChildren()) {
			collect((T) obj, coll);
		}
	}
}
