package org.ironrhino.common.support;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.ironrhino.common.model.Region;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.common.util.BeanUtils;
import org.ironrhino.common.util.ObjectFilter;
import org.ironrhino.common.util.RegionUtils;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.service.BaseManager;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class RegionTreeControl implements ApplicationListener {

	private Region regionTree;

	private BaseManager<Region> baseManager;

	public void setBaseManager(BaseManager baseManager) {
		this.baseManager = baseManager;
	}

	public void buildRegionTree() {
		baseManager.setEntityClass(Region.class);
		regionTree = baseManager.loadTree();
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		buildRegionTree();
	}

	public Region getRegionTree() {
		return regionTree;
	}

	public Region getPrivateRegionTree() {
		return BeanUtils.deepClone(regionTree, new ObjectFilter() {
			public boolean accept(Object object) {
				Region region = (Region) object;
				List<String> roleNames = AuthzUtils.getRoleNames();
				for (SimpleElement n : region.getRoles()) {
					if (roleNames.contains(n.getValue()))
						return true;
				}
				return false;
			}
		});
	}

	public boolean checkPermission(Integer id) {
		return getPrivateRegionTree().getDescendantOrSelfById(id) != null;
	}

	public Region parseByHost(String host) {
		return RegionUtils.parseByHost(host, regionTree);
	}

	public Region parseByAddress(String address) {
		return RegionUtils.parseByAddress(address, regionTree);
	}

	private void create(Region region) {
		Region parent;
		if (region.getParent() == null)
			parent = regionTree;
		else
			parent = regionTree.getDescendantOrSelfById(region.getParent()
					.getId());
		Region r = new Region();
		BeanUtils.copyProperties(region, r,
				new String[] { "parent", "children" });
		r.setParent(parent);
		parent.getChildren().add(r);
		Collections.sort((List<Region>) parent.getChildren());
	}

	private void update(Region region) {
		Region r = regionTree.getDescendantOrSelfById(region.getId());
		if (!r.getFullId().equals(region.getFullId())) {
			r.getParent().getChildren().remove(r);
			Region newParent;
			if (region.getParent() == null)
				newParent = regionTree;
			else
				newParent = regionTree.getDescendantOrSelfById(region
						.getParent().getId());
			r.setParent(newParent);
			newParent.getChildren().add(r);
		}
		BeanUtils.copyProperties(region, r,
				new String[] { "parent", "children" });
		Collections.sort((List<Region>) r.getParent().getChildren());
	}

	private void delete(Region region) {
		Region r = regionTree.getDescendantOrSelfById(region.getId());
		r.getParent().getChildren().remove(r);
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (regionTree == null)
			return;
		if (event instanceof EntityOperationEvent) {
			EntityOperationEvent ev = (EntityOperationEvent) event;
			if (ev.getEntity() instanceof Region) {
				Region region = (Region) ev.getEntity();
				if (ev.getType() == EntityOperationType.CREATE)
					create(region);
				else if (ev.getType() == EntityOperationType.UPDATE)
					update(region);
				else if (ev.getType() == EntityOperationType.DELETE)
					delete(region);
			}
		}

	}
}
