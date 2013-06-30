package org.ironrhino.common.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.common.model.TreeNode;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.util.BeanUtils;
import org.springframework.context.ApplicationListener;

@Singleton
@Named("treeNodeControl")
public class TreeNodeControl implements
		ApplicationListener<EntityOperationEvent> {

	private volatile TreeNode tree;

	@Inject
	private EntityManager<TreeNode> entityManager;

	public void buildTreeNodeTree() {
		entityManager.setEntityClass(TreeNode.class);
		tree = entityManager.loadTree();
	}

	public TreeNode getTree() {
		if (tree == null)
			synchronized (this) {
				if (tree == null)
					buildTreeNodeTree();
			}
		return tree;
	}

	public TreeNode getTree(String name) {
		TreeNode subtree = null;
		for (TreeNode t : tree.getChildren())
			if (t.getName().equals(name)) {
				addLevel(t, 1);
				subtree = t;
				break;
			}
		return subtree;
	}

	private void addLevel(TreeNode treeNode, int delta) {
		treeNode.setLevel(treeNode.getLevel() + delta);
		for (TreeNode t : treeNode.getChildren())
			addLevel(t, delta);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void create(TreeNode treeNode) {
		TreeNode parent;
		String fullId = treeNode.getFullId();
		if (treeNode.getId().toString().equals(fullId)) {
			parent = tree;
		} else {
			String parentId = fullId.substring(0, fullId.lastIndexOf('.'));
			if (parentId.indexOf('.') > -1)
				parentId = parentId.substring(parentId.lastIndexOf('.') + 1);
			parent = tree.getDescendantOrSelfById(Long.valueOf(parentId));
		}
		TreeNode r = new TreeNode();
		r.setChildren(new ArrayList<TreeNode>());
		BeanUtils.copyProperties(treeNode, r, new String[] { "parent",
				"children" });
		r.setParent(parent);
		parent.getChildren().add(r);
		if (parent.getChildren() instanceof List)
			Collections.sort((List) parent.getChildren());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void update(TreeNode treeNode) {
		TreeNode r = tree.getDescendantOrSelfById(treeNode.getId());
		boolean needsort = r.compareTo(treeNode) != 0
				|| !r.getFullId().equals(treeNode.getFullId());
		if (!r.getFullId().equals(treeNode.getFullId())) {
			r.getParent().getChildren().remove(r);
			String str = treeNode.getFullId();
			long newParentId = 0;
			if (str.indexOf('.') > 0) {
				str = str.substring(0, str.lastIndexOf('.'));
				if (str.indexOf('.') > 0)
					str = str.substring(str.lastIndexOf('.') + 1);
				newParentId = Long.valueOf(str);
			}
			TreeNode newParent;
			if (newParentId == 0)
				newParent = tree;
			else
				newParent = tree.getDescendantOrSelfById(newParentId);
			r.setParent(newParent);
			newParent.getChildren().add(r);
			resetChildren(r);
		}
		BeanUtils.copyProperties(treeNode, r, new String[] { "parent",
				"children" });
		if (needsort && r.getParent().getChildren() instanceof List)
			Collections.sort((List) r.getParent().getChildren());
	}

	private void resetChildren(TreeNode treeNode) {
		if (treeNode.isHasChildren())
			for (TreeNode r : treeNode.getChildren()) {
				String fullId = (r.getParent()).getFullId() + "."
						+ String.valueOf(r.getId());
				r.setFullId(fullId);
				r.setLevel(fullId.split("\\.").length);
				resetChildren(r);
			}
	}

	private synchronized void delete(TreeNode treeNode) {
		TreeNode r = tree.getDescendantOrSelfById(treeNode.getId());
		r.getParent().getChildren().remove(r);
	}

	@Override
	public void onApplicationEvent(EntityOperationEvent event) {
		if (tree == null)
			return;
		if (event.getEntity() instanceof TreeNode) {
			TreeNode treeNode = (TreeNode) event.getEntity();
			if (event.getType() == EntityOperationType.CREATE)
				create(treeNode);
			else if (event.getType() == EntityOperationType.UPDATE)
				update(treeNode);
			else if (event.getType() == EntityOperationType.DELETE)
				delete(treeNode);
		}
	}
}
