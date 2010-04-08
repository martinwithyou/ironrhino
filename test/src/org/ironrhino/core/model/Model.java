package org.ironrhino.core.model;

import java.util.ArrayList;
import java.util.List;

import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseEntity;

import com.opensymphony.xwork2.util.CreateIfNull;

public class Model extends BaseEntity {

	private static final long serialVersionUID = 2262572451135688507L;

	@NaturalId
	private String name;

	private String value;

	@NotInJson
	private String password;

	@NotInCopy
	@NotInJson
	@CreateIfNull
	private List<Model> relations = new ArrayList<Model>();

	@NaturalId
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NaturalId
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getLevel() {
		return 0;
	}

	public List<Model> getRelations() {
		return relations;
	}

	public void setRelations(List<Model> relations) {
		this.relations = relations;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
