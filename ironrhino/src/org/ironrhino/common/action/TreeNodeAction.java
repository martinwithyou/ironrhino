package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.TreeNode;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.BaseAction;

public class TreeNodeAction extends BaseAction {

	private static final long serialVersionUID = -4641355307938016102L;

	private TreeNode treeNode;

	private Long parentId;

	private transient BaseManager<TreeNode> baseManager;

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

	public TreeNode getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(TreeNode treeNode) {
		this.treeNode = treeNode;
	}

	public void setBaseManager(BaseManager baseManager) {
		baseManager.setEntityClass(TreeNode.class);
		this.baseManager = baseManager;
	}

	@Override
	public String execute() {
		if (parentId != null && parentId > 0) {
			treeNode = baseManager.get(parentId);
		} else {
			treeNode = new TreeNode();
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.isNull("parent"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("name"));
			treeNode.setChildren(baseManager.findListByCriteria(dc));
		}
		list = treeNode.getChildren();
		return LIST;
	}

	@Override
	public String input() {
		if (getUid() != null)
			treeNode = baseManager.get(Long.valueOf(getUid()));
		if (treeNode == null)
			treeNode = new TreeNode();
		return INPUT;
	}

	@Override
	public String save() {
		Collection<TreeNode> siblings = null;
		if (treeNode.isNew()) {
			if (parentId != null) {
				TreeNode parent = baseManager.get(parentId);
				treeNode.setParent(parent);
				siblings = parent.getChildren();
			} else {
				DetachedCriteria dc = baseManager.detachedCriteria();
				dc.add(Restrictions.isNull("parent"));
				dc.addOrder(Order.asc("displayOrder"));
				dc.addOrder(Order.asc("name"));
				siblings = baseManager.findListByCriteria(dc);
			}
			for (TreeNode sibling : siblings)
				if (sibling.getName().equals(treeNode.getName())) {
					addFieldError("treeNode.name",
							getText("validation.already.exists"));
					return INPUT;
				}
		} else {
			TreeNode temp = treeNode;
			treeNode = baseManager.get(temp.getId());
			if (!treeNode.getName().equals(temp.getName())) {
				if (treeNode.getParent() == null) {
					DetachedCriteria dc = baseManager.detachedCriteria();
					dc.add(Restrictions.isNull("parent"));
					dc.addOrder(Order.asc("displayOrder"));
					dc.addOrder(Order.asc("name"));
					siblings = baseManager.findListByCriteria(dc);
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
			if (temp.getAttributes() != null && temp.getAttributes().size() > 0)
				treeNode.setAttributes(temp.getAttributes());
		}
		baseManager.save(treeNode);
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
				list.add(baseManager.get(id[0]));
			} else {
				DetachedCriteria dc = baseManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = baseManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (TreeNode temp : list) {
					if (!baseManager.canDelete(temp)) {
						addActionError(temp.getName()
								+ getText("delete.forbidden",
										new String[] { temp.getName() }));
						deletable = false;
						break;
					}
				}
				if (deletable) {
					for (TreeNode temp : list)
						baseManager.delete(temp);
					addActionMessage(getText("delete.success"));
				}
			}
		}
		return SUCCESS;
	}

}
