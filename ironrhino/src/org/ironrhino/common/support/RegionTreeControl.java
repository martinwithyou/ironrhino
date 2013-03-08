package org.ironrhino.common.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.common.model.Region;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.util.BeanUtils;
import org.springframework.context.ApplicationListener;

@Singleton
@Named("regionTreeControl")
public class RegionTreeControl implements
		ApplicationListener<EntityOperationEvent> {

	private volatile Region regionTree;

	@Inject
	private EntityManager<Region> entityManager;

	public void buildRegionTree() {
		entityManager.setEntityClass(Region.class);
		regionTree = entityManager.loadTree();
	}

	public Region getRegionTree() {
		if (regionTree == null)
			synchronized (this) {
				if (regionTree == null)
					buildRegionTree();
			}
		return regionTree;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void create(Region region) {
		Region parent;
		String fullId = region.getFullId();
		if (region.getId().toString().equals(fullId)) {
			parent = regionTree;
		} else {
			String parentId = fullId.substring(0, fullId.lastIndexOf('.'));
			if (parentId.indexOf('.') > -1)
				parentId = parentId.substring(parentId.lastIndexOf('.') + 1);
			parent = regionTree.getDescendantOrSelfById(Long.valueOf(parentId));
		}
		Region r = new Region();
		r.setChildren(new ArrayList<Region>());
		BeanUtils.copyProperties(region, r,
				new String[] { "parent", "children" });
		r.setParent(parent);
		parent.getChildren().add(r);
		if (parent.getChildren() instanceof List)
			Collections.sort((List) parent.getChildren());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void update(Region region) {
		Region r = regionTree.getDescendantOrSelfById(region.getId());
		boolean needsort = r.compareTo(region) != 0
				|| !r.getFullId().equals(region.getFullId());
		if (!r.getFullId().equals(region.getFullId())) {
			r.getParent().getChildren().remove(r);
			String str = region.getFullId();
			long newParentId = 0;
			if (str.indexOf('.') > 0) {
				str = str.substring(0, str.lastIndexOf('.'));
				if (str.indexOf('.') > 0)
					str = str.substring(str.lastIndexOf('.') + 1);
				newParentId = Long.valueOf(str);
			}
			Region newParent;
			if (newParentId == 0)
				newParent = regionTree;
			else
				newParent = regionTree.getDescendantOrSelfById(newParentId);
			r.setParent(newParent);
			newParent.getChildren().add(r);
			resetChildren(r);
		}
		BeanUtils.copyProperties(region, r,
				new String[] { "parent", "children" });
		if (needsort && r.getParent().getChildren() instanceof List)
			Collections.sort((List) r.getParent().getChildren());
	}

	private void resetChildren(Region region) {
		if (region.isHasChildren())
			for (Region r : region.getChildren()) {
				String fullId = (r.getParent()).getFullId() + "."
						+ String.valueOf(r.getId());
				r.setFullId(fullId);
				r.setLevel(fullId.split("\\.").length);
				resetChildren(r);
			}
	}

	private synchronized void delete(Region region) {
		Region r = regionTree.getDescendantOrSelfById(region.getId());
		r.getParent().getChildren().remove(r);
	}

	public void onApplicationEvent(EntityOperationEvent event) {
		if (regionTree == null)
			return;
		if (event.getEntity() instanceof Region) {
			Region region = (Region) event.getEntity();
			if (event.getType() == EntityOperationType.CREATE)
				create(region);
			else if (event.getType() == EntityOperationType.UPDATE)
				update(region);
			else if (event.getType() == EntityOperationType.DELETE)
				delete(region);
		}
	}
}
