package org.ironrhino.core.model;

import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.annotation.NotInCopy;

public abstract class AbstractTreeableEntity<T extends AbstractTreeableEntity>
		extends Entity implements Treeable<T> {
	private Integer id;

	private String fullId;

	protected String name;

	protected int level;

	public String getFullId() {
		return fullId;
	}

	public void setFullId(String fullId) {
		this.fullId = fullId;
	}

	@SearchableId(converter = "int")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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

	public String getFullname() {
		String fullname = name;
		AbstractTreeableEntity e = this;
		while ((e = (AbstractTreeableEntity) e.getParent()) != null) {
			fullname = e.getName() + fullname;
		}
		return fullname;
	}

}
