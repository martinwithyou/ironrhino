package org.ironrhino.core.monitor.analysis;

import java.io.Serializable;
import java.util.List;

import org.ironrhino.core.monitor.Value;

public class Node implements Serializable {

	private String id;

	private String name;

	private Node parent;

	private List<Node> children;

	private Value value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

}
