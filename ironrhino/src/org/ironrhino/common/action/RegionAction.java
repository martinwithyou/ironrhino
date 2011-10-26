package org.ironrhino.common.action;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Region;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.ClassScaner;
import org.ironrhino.core.util.HtmlUtils;
import org.springframework.beans.BeanUtils;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class RegionAction extends BaseAction {

	private static final long serialVersionUID = -4643055307938016102L;

	private Region region;

	private Long parentId;

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
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "region.name", trim = true, key = "validation.required") }, stringLengthFields = {
			@StringLengthFieldValidator(type = ValidatorType.FIELD, fieldName = "region.areacode", maxLength = "6", key = "validation.invalid"),
			@StringLengthFieldValidator(type = ValidatorType.FIELD, fieldName = "region.postcode", maxLength = "6", key = "validation.invalid") })
	public String save() {
		Collection<Region> siblings = null;
		if (region.isNew()) {
			if (parentId != null) {
				Region parent = baseManager.get(parentId);
				region.setParent(parent);
				siblings = parent.getChildren();
			} else {
				DetachedCriteria dc = baseManager.detachedCriteria();
				dc.add(Restrictions.isNull("parent"));
				dc.addOrder(Order.asc("displayOrder"));
				dc.addOrder(Order.asc("name"));
				siblings = baseManager.findListByCriteria(dc);
			}
			for (Region sibling : siblings)
				if (sibling.getName().equals(region.getName())) {
					addFieldError("region.name",
							getText("validation.already.exists"));
					return INPUT;
				}
		} else {
			Region temp = region;
			region = baseManager.get(temp.getId());
			if (temp.getCoordinate() != null
					&& temp.getCoordinate().getLatitude() != null) {
				region.setCoordinate(temp.getCoordinate());
			} else {
				if (!region.getName().equals(temp.getName())) {
					if (region.getParent() == null) {
						DetachedCriteria dc = baseManager.detachedCriteria();
						dc.add(Restrictions.isNull("parent"));
						dc.addOrder(Order.asc("displayOrder"));
						dc.addOrder(Order.asc("name"));
						siblings = baseManager.findListByCriteria(dc);
					} else {
						siblings = region.getParent().getChildren();
					}
					for (Region sibling : siblings)
						if (sibling.getName().equals(temp.getName())) {
							addFieldError("region.name",
									getText("validation.already.exists"));
							return INPUT;
						}
				}
				region.setName(temp.getName());
				region.setAreacode(temp.getAreacode());
				region.setPostcode(temp.getPostcode());
				region.setDisplayOrder(temp.getDisplayOrder());
			}
		}

		baseManager.save(region);
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
	public String markers() {
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
		dc.add(Restrictions.and(
				Restrictions.between("coordinate.latitude", bottom, top),
				Restrictions.between("coordinate.longitude", left, right)));
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

	public String move() {
		String[] id = getId();
		if (id != null && id.length == 2) {
			Region source = null;
			Region target = null;
			try {
				source = baseManager.get(Long.valueOf(id[0]));
				if (Long.valueOf(id[1]) > 0)
					target = baseManager.get(Long.valueOf(id[1]));
			} catch (Exception e) {

			}
			if (source == null) {
				addActionError(getText("validation.required"));
				return SUCCESS;
			}
			if (!(source.getParent() == null && target == null || source
					.getParent() != null
					&& target != null
					&& source.getParent().getId() == target.getId())) {
				source.setParent(target);
				baseManager.save(source);
				addActionMessage(getText("operate.success"));
			}
		}
		return SUCCESS;
	}

	public String merge() {
		String[] id = getId();
		if (id != null && id.length == 2) {
			Region source = null;
			Region target = null;
			try {
				source = baseManager.get(Long.valueOf(id[0]));
				target = baseManager.get(Long.valueOf(id[1]));
			} catch (Exception e) {

			}
			if (source == null || target == null || !source.isLeaf()
					|| !target.isLeaf()) {
				addActionError(getText("validation.required"));
				return SUCCESS;
			}
			Set<Class> set = ClassScaner.scanAssignable(
					ClassScaner.getAppPackages(), Persistable.class);
			for (Class clz : set) {
				if (clz.equals(Region.class))
					continue;
				PropertyDescriptor[] pds = BeanUtils
						.getPropertyDescriptors(clz);
				for (PropertyDescriptor pd : pds) {
					if (pd.getReadMethod() != null
							&& pd.getReadMethod().getReturnType()
									.equals(Region.class)
							&& pd.getWriteMethod() != null) {
						String name = pd.getName();
						String hql = new StringBuilder("update ")
								.append(clz.getName()).append(" t set t.")
								.append(name).append(".id=? where t.")
								.append(name).append(".id=?").toString();
						baseManager.executeUpdate(hql, target.getId(),
								source.getId());
					}
				}
			}
			baseManager.delete(source);
			addActionMessage(getText("operate.success"));
		}
		return SUCCESS;
	}
}
