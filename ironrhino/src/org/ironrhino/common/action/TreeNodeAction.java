package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.TreeNode;
import org.ironrhino.common.support.TreeNodeControl;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.security.model.UserRole;

public class TreeNodeAction extends BaseAction {

	private static final long serialVersionUID = -4641355307938016102L;

	private TreeNode treeNode;

	private Long parentId;

	private long root;

	@Inject
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
		list = treeNode.getChildren();
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
				siblings = parent.getChildren();
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
		String[] arr = getId();
		Long[] id = (arr != null) ? new Long[arr.length] : new Long[0];
		for (int i = 0; i < id.length; i++)
			id[i] = Long.valueOf(arr[i]);
		if (id.length > 0) {
			List<TreeNode> list;
			if (id.length == 1) {
				list = new ArrayList<TreeNode>(1);
				list.add(entityManager.get(id[0]));
			} else {
				DetachedCriteria dc = entityManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = entityManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (TreeNode temp : list) {
					if (!entityManager.canDelete(temp)) {
						addActionError(temp.getName()
								+ getText("delete.forbidden",
										new String[] { temp.getName() }));
						deletable = false;
						break;
					}
				}
				if (deletable) {
					for (TreeNode temp : list)
						entityManager.delete(temp);
					addActionMessage(getText("delete.success"));
				}
			}
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
