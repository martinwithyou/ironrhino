package org.ironrhino.common.action;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Setting;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.service.BaseManager;

public class SettingAction extends BaseAction {

	private Setting setting;

	private BaseManager<Setting> baseManager;

	private Collection<Setting> list;

	public Collection<Setting> getList() {
		return list;
	}

	public Setting getSetting() {
		return setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}

	public void setBaseManager(BaseManager<Setting> baseManager) {
		baseManager.setEntityClass(Setting.class);
		this.baseManager = baseManager;
	}

	public String execute() {
		list = baseManager.getAll(Order.asc("key"));
		return LIST;
	}

	public String input() {
		setting = baseManager.get(getUid());
		if (setting == null)
			setting = new Setting();
		return INPUT;
	}

	public String save() {
		if (setting != null) {
			baseManager.save(setting);
			addActionMessage(getText("save.success", "save {0} successfully",
					new String[] { setting.getKey() }));
		}
		return SUCCESS;
	}

	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Setting> list = baseManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Setting setting : list) {
					baseManager.delete(setting);
					sb.append(setting.getKey() + ",");
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
}
