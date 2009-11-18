package com.ironrhino.pms.action;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.HtmlUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ironrhino.pms.model.Category;
import com.ironrhino.pms.service.CategoryManager;
import com.ironrhino.pms.support.CategoryTreeControl;

public class CategoryAction extends BaseAction {

	private static final long serialVersionUID = 8576058148393322253L;

	private Category category;

	private Long parentId;

	private String rolesAsString;

	private Collection<Category> list;

	@Autowired
	private transient CategoryManager categoryManager;

	@Autowired
	private transient CategoryTreeControl categoryTreeControl;

	private boolean async;

	private long root;

	public long getRoot() {
		return root;
	}

	public void setRoot(long root) {
		this.root = root;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public Collection<Category> getList() {
		return list;
	}

	public String getRolesAsString() {
		return rolesAsString;
	}

	public void setRolesAsString(String rolesAsString) {
		this.rolesAsString = rolesAsString;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public String execute() {
		if (parentId != null && parentId > 0) {
			category = categoryManager.get(parentId);
		} else {
			category = new Category();
			DetachedCriteria dc = categoryManager.detachedCriteria();
			dc.add(Restrictions.isNull("parent"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("name"));
			category.setChildren(categoryManager.findListByCriteria(dc));
		}
		list = category.getChildren();
		return LIST;
	}

	@Override
	public String input() {
		if (getUid() != null)
			category = categoryManager.get(new Integer(getUid()));
		if (category == null)
			category = new Category();
		return INPUT;
	}

	@Override
	public String save() {
		if (category.isNew()) {
			if (categoryManager.findByNaturalId(category.getCode()) != null) {
				addFieldError("category.code",
						getText("validation.already.exists"));
				return INPUT;
			}
			if (parentId != null) {
				Category parent = categoryManager.get(parentId);
				category.setParent(parent);
			}
		} else {
			Category temp = category;
			category = categoryManager.get(temp.getId());
			category.setName(temp.getName());
			category.setDescription(temp.getDescription());
			category.setDisplayOrder(temp.getDisplayOrder());
			if (rolesAsString != null)
				category.setRolesAsString(rolesAsString);
		}
		categoryManager.save(category);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	public String move() {
		String id = getUid();
		if (StringUtils.isNotBlank(id)) {
			category = categoryManager.get(new Integer(getUid()));
			if (parentId != null && parentId > 0
					&& !id.equals(parentId.toString()))
				category.setParent(categoryManager.get(parentId));
			else
				category.setParent(null);
			categoryManager.save(category);
		}
		return "tree";
	}

	@Override
	public String delete() {
		String[] arr = getId();
		Integer[] id = new Integer[arr.length];
		for (int i = 0; i < id.length; i++)
			id[i] = new Integer(arr[i]);
		if (id != null) {
			DetachedCriteria dc = categoryManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Category> list = categoryManager.findListByCriteria(dc);
			if (list.size() > 0) {
				for (Category category : list)
					categoryManager.delete(category);
				addActionMessage(getText("delete.success"));
			}
		}
		return SUCCESS;
	}

	public String tree() {
		if (!async) {
			if (root < 1)
				category = categoryTreeControl.getCategoryTree();
			else
				category = categoryTreeControl.getCategoryTree()
						.getDescendantOrSelfById(root);
			list = category.getChildren();
		}
		return "tree";
	}

	@JsonConfig(root = "list")
	public String children() {
		Category category;
		if (root < 1)
			category = categoryTreeControl.getCategoryTree();
		else
			category = categoryTreeControl.getCategoryTree()
					.getDescendantOrSelfById(root);
		list = category.getChildren();
		return JSON;
	}

	public String getTreeViewHtml() {
		return HtmlUtils.getTreeViewHtml(list, async);
	}
}
