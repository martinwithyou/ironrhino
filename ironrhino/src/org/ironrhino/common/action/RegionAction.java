package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Region;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.HtmlUtils;

public class RegionAction extends BaseAction {

	private static final long serialVersionUID = -4643055307938016102L;

	private Region region;

	private Long parentId;

	private String rolesAsString;

	private transient BaseManager<Region> baseManager;

	private Collection<Region> list;

	private String southWest;

	private String northEast;

	private int zoom;

	private boolean async;

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

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

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
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

	@Override
	public String execute() {
		if (parentId != null && parentId > 0) {
			region = baseManager.get(parentId);
		} else {
			region = new Region();
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.isNull("parent"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("name"));
			region.setChildren(baseManager.findListByCriteria(dc));
		}
		list = region.getChildren();
		return LIST;
	}

	@Override
	public String input() {
		if (getUid() != null)
			region = baseManager.get(Long.valueOf(getUid()));
		if (region == null)
			region = new Region();
		return INPUT;
	}

	@Override
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
			}
		}
		baseManager.save(region);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			List<Region> list;
			if (id.length == 1) {
				list = new ArrayList<Region>(1);
				list.add(baseManager.get(id[0]));
			} else {
				DetachedCriteria dc = baseManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = baseManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (Region temp : list) {
					if (!baseManager.canDelete(temp)) {
						addActionError(temp.getName()
								+ getText("delete.forbidden",
										new String[] { temp.getName() }));
						deletable = false;
						break;
					}
				}
				if (deletable) {
					for (Region temp : list)
						baseManager.delete(temp);
					addActionMessage(getText("delete.success"));
				}
			}
		}
		return SUCCESS;
	}

	public String getTreeViewHtml() {
		return HtmlUtils.getTreeViewHtml(list, async);
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
		list = baseManager.findListByCriteria(dc);
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
