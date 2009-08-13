package org.ironrhino.core.monitor.analysis.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;

public class TreeNode implements Serializable {

	private int id;

	private TreeNode parent;

	private List<TreeNode> children = new ArrayList<TreeNode>();

	private Key key;

	private Value value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children == null || children.size() == 0;
	}

	public int getLevel() {
		int level = 1;
		TreeNode node = this;
		while ((node = node.getParent()) != null)
			level++;
		return level;
	}

	public String getName() {
		return key.getNames()[getLevel() - 1];
	}

	public TreeNode getDescendantOrSelfByKey(Key key) {
		if (key.equals(this.getKey()))
			return this;
		for (TreeNode t : getChildren()) {
			if (key.equals(t.getKey())) {
				return t;
			} else {
				TreeNode tt = t.getDescendantOrSelfByKey(key);
				if (tt != null)
					return tt;
			}
		}
		return null;
	}

}
