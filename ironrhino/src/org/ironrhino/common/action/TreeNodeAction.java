package org.ironrhino.common.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.TreeNode;
import org.ironrhino.common.support.TreeNodeControl;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.security.role.UserRole;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.struts.BaseAction;

public class TreeNodeAction extends BaseAction {

	private static final long serialVersionUID = -4641355307938016102L;

	private TreeNode treeNode;

	private Long parentId;

	private long root;

	@Autowired
	private transient TreeNodeControl treeNodeControl;

	private transient EntityManager<TreeNode> entityManager;

	private Collection<TreeNode> list;

	public Collection<TreeNode> getList() {
		return list;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public long getRoot() {
		return root;
	}

	public void setRoot(long root) {
		this.root = root;
	}

	public TreeNode getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(TreeNode treeNode) {
		this.treeNode = treeNode;
	}

	public void setEntityManager(EntityManager<TreeNode> entityManager) {
		entityManager.setEntityClass(TreeNode.class);
		this.entityManager = entityManager;
	}

	@Override
	public String execute() {
		if (parentId != null && parentId > 0) {
			treeNode = entityManager.get(parentId);
		} else {
			treeNode = new TreeNode();
			DetachedCriteria dc = entityManager.detachedCriteria();
			dc.add(Restrictions.isNull("parent"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("name"));
			treeNode.setChildren(entityManager.findListByCriteria(dc));
		}
		list = treeNode != null ? treeNode.getChildren()
				: new ArrayList<TreeNode>(0);
		return LIST;
	}

	@Override
	public String input() {
		if (getUid() != null)
			treeNode = entityManager.get(Long.valueOf(getUid()));
		if (treeNode == null)
			treeNode = new TreeNode();
		return INPUT;
	}

	@Override
	public String save() {
		Collection<TreeNode> siblings = null;
		if (treeNode.isNew()) {
			if (parentId != null) {
				TreeNode parent = entityManager.get(parentId);
				treeNode.setParent(parent);
				siblings = parent != null ? parent.getChildren()
						: new ArrayList<TreeNode>(0);
			} else {
				DetachedCriteria dc = entityManager.detachedCriteria();
				dc.add(Restrictions.isNull("parent"));
				dc.addOrder(Order.asc("displayOrder"));
				dc.addOrder(Order.asc("name"));
				siblings = entityManager.findListByCriteria(dc);
			}
			for (TreeNode sibling : siblings)
				if (sibling.getName().equals(treeNode.getName())) {
					addFieldError("treeNode.name",
							getText("validation.already.exists"));
					return INPUT;
				}
		} else {
			TreeNode temp = treeNode;
			treeNode = entityManager.get(temp.getId());
			if (!treeNode.getName().equals(temp.getName())) {
				if (treeNode.getParent() == null) {
					DetachedCriteria dc = entityManager.detachedCriteria();
					dc.add(Restrictions.isNull("parent"));
					dc.addOrder(Order.asc("displayOrder"));
					dc.addOrder(Order.asc("name"));
					siblings = entityManager.findListByCriteria(dc);
				} else {
					siblings = treeNode.getParent().getChildren();
				}
				for (TreeNode sibling : siblings)
					if (sibling.getName().equals(temp.getName())
							&& !sibling.getId().equals(treeNode.getId())) {
						addFieldError("treeNode.name",
								getText("validation.already.exists"));
						return INPUT;
					}
			}
			treeNode.setName(temp.getName());
			treeNode.setDescription(temp.getDescription());
			treeNode.setDisplayOrder(temp.getDisplayOrder());
			treeNode.setAttributes(temp.getAttributes());
		}
		entityManager.save(treeNode);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			entityManager.setEntityClass(TreeNode.class);
			entityManager.delete((Serializable[]) id);
			addActionMessage(getText("delete.success"));
		}
		return SUCCESS;
	}

	@JsonConfig(root = "list")
	@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_USER)
	public String children() {
		TreeNode treeNode;
		if (root < 1)
			treeNode = treeNodeControl.getTree();
		else
			treeNode = treeNodeControl.getTree().getDescendantOrSelfById(root);
		if (treeNode != null)
			list = treeNode.getChildren();
		return JSON;
	}

}
