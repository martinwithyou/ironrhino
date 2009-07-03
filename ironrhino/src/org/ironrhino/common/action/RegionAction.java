package org.ironrhino.common.action;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Region;
import org.ironrhino.core.annotation.JsonConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.service.BaseManager;

public class RegionAction extends BaseAction {

	private Region region;

	private Integer parentId;

	private String rolesAsString;

	private BaseManager<Region> baseManager;

	private Collection<Region> list;

	private String southWest;

	private String northEast;

	private int zoom;

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public void setSouthWest(String southWest) {
		this.southWest = southWest;
	}

	public void setNorthEast(String northEast) {
		this.northEast = northEast;
	}

	public Collection<Region> getList() {
		return list;
	}

	public String getRolesAsString() {
		return rolesAsString;
	}

	public void setRolesAsString(String rolesAsString) {
		this.rolesAsString = rolesAsString;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public void setBaseManager(BaseManager baseManager) {
		baseManager.setEntityClass(Region.class);
		this.baseManager = baseManager;
	}

	public String execute() {
		if (parentId != null && parentId > 0) {
			region = baseManager.get(parentId);
		} else {
			region = new Region();
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.isNull("parent"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("name"));
			region.setChildren(baseManager.getListByCriteria(dc));
		}
		list = region.getChildren();
		return LIST;
	}

	public String input() {
		if (getUid() != null)
			region = baseManager.get(new Integer(getUid()));
		if (region == null)
			region = new Region();
		return INPUT;
	}

	public String save() {
		if (region.isNew()) {
			if (parentId != null) {
				Region parent = baseManager.get(parentId);
				region.setParent(parent);
			}
		} else {
			Region temp = region;
			region = baseManager.get(temp.getId());
			if (temp.getLatitude() != null) {
				region.setLatitude(temp.getLatitude());
				region.setLongitude(temp.getLongitude());
			} else {
				region.setName(temp.getName());
				region.setDisplayOrder(temp.getDisplayOrder());
				if (rolesAsString != null)
					region.setRolesAsString(rolesAsString);
			}
		}
		baseManager.save(region);
		addActionMessage(getText("save.success", "save {0} successfully",
				new String[] { region.getName() }));
		return SUCCESS;
	}

	public String delete() {
		String[] arr = getId();
		Integer[] id = new Integer[arr.length];
		for (int i = 0; i < id.length; i++)
			id[i] = new Integer(arr[i]);
		if (id != null) {
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Region> list = baseManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Region region : list) {
					baseManager.delete(region);
					sb.append(region.getName() + ",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				addActionMessage(getText("delete.success",
						"delete {0} successfully",
						new String[] { sb.toString() }));
			}
		}
		return SUCCESS;
	}

	public String map() {
		return "map";
	}

	@JsonConfig(root = "list")
	public String mark() {
		String[] array = southWest.split(",");
		Double bottom = new Double(array[0]);
		Double left = new Double(array[1]);
		array = northEast.split(",");
		Double top = new Double(array[0]);
		Double right = new Double(array[1]);
		Integer[] levels = zoom2level(zoom);
		DetachedCriteria dc = baseManager.detachedCriteria();
		if (levels != null)
			dc.add(Restrictions.in("level", levels));
		dc.add(Restrictions.and(Restrictions.between("latitude", bottom, top),
				Restrictions.between("longitude", left, right)));
		list = baseManager.getListByCriteria(dc);
		return JSON;
	}

	private Integer[] zoom2level(int z) {
		if (z <= 7) {
			return new Integer[] { 1 };
		} else if (z <= 7) {
			return new Integer[] { 1, 2 };
		} else if (z <= 9) {
			return new Integer[] { 1, 2, 3 };
		} else {
			return null;
		}
	}
}
