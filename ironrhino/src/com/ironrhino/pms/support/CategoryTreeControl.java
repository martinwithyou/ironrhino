package com.ironrhino.pms.support;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.core.util.ObjectFilter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.ironrhino.pms.model.Category;
import com.ironrhino.pms.service.CategoryManager;

@Singleton@Named("categoryTreeControl")
public class CategoryTreeControl implements ApplicationListener {

	private Category categoryTree;

	private Category publicCategoryTree;

	@Inject
	private CategoryManager categoryManager;

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		buildCategoryTree();
	}

	public void buildCategoryTree() {
		categoryTree = categoryManager.loadTree();
		publicCategoryTree = null;
	}

	public Category getFullCategoryTree() {
		return this.categoryTree;
	}

	public Category getCategoryTree() {
		if (AuthzUtils.getRoleNames().size() == 1)
			return getPublicCategoryTree();
		else
			return getProtectedCategoryTree();
	}

	private Category getPublicCategoryTree() {
		if (publicCategoryTree == null)
			buildPublicCategoryTree();
		return publicCategoryTree;
	}

	private void buildPublicCategoryTree() {
		publicCategoryTree = BeanUtils.deepClone(categoryTree,
				new ObjectFilter() {
					public boolean accept(Object object) {
						Category cate = (Category) object;
						if (cate.getRoles() == null
								|| cate.getRoles().size() == 0)
							return true;
						else
							return false;
					}
				});
	}

	private Category getProtectedCategoryTree() {
		return BeanUtils.deepClone(categoryTree, new ObjectFilter() {
			public boolean accept(Object object) {
				Category cate = (Category) object;
				if (cate.getRoles() == null || cate.getRoles().size() == 0)
					return true;
				if (AuthzUtils.hasPermission(cate))
					return true;
				return false;
			}
		});
	}

	private void create(Category cate) {
		Category parent;
		if (cate.getParent() == null)
			parent = categoryTree;
		else
			parent = categoryTree.getDescendantOrSelfById(cate.getParent()
					.getId());
		Category c = new Category();
		BeanUtils
				.copyProperties(cate, c, new String[] { "parent", "children" });
		c.setParent(parent);
		parent.getChildren().add(c);
		if (parent.getChildren() instanceof List)
			Collections.sort((List<Category>) parent.getChildren());
	}

	private void update(Category cate) {
		Category c = categoryTree.getDescendantOrSelfById(cate.getId());
		if (!c.getFullId().equals(cate.getFullId())) {
			c.getParent().getChildren().remove(c);
			Category newParent;
			if (cate.getParent() == null)
				newParent = categoryTree;
			else
				newParent = categoryTree.getDescendantOrSelfById(cate
						.getParent().getId());
			c.setParent(newParent);
			newParent.getChildren().add(c);
		}
		BeanUtils
				.copyProperties(cate, c, new String[] { "parent", "children" });
		if (c.getParent().getChildren() instanceof List)
			Collections.sort((List<Category>) c.getParent().getChildren());
	}

	private void delete(Category cate) {
		Category c = categoryTree.getDescendantOrSelfById(cate.getId());
		c.getParent().getChildren().remove(c);
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (categoryTree == null)
			return;
		if (event instanceof EntityOperationEvent) {
			EntityOperationEvent ev = (EntityOperationEvent) event;
			if (ev.getEntity() instanceof Category) {
				Category cate = (Category) ev.getEntity();
				if (ev.getType() == EntityOperationType.CREATE)
					create(cate);
				else if (ev.getType() == EntityOperationType.UPDATE)
					update(cate);
				else if (ev.getType() == EntityOperationType.DELETE)
					delete(cate);
			}
		}

	}

}
