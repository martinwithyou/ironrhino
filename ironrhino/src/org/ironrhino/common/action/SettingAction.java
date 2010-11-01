package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Setting;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.BaseAction;

public class SettingAction extends BaseAction {

	private static final long serialVersionUID = -7824355496392523420L;

	private Setting setting;

	private transient BaseManager<Setting> baseManager;

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

	@Override
	public String execute() {
		list = baseManager.findAll(Order.asc("key"));
		return LIST;
	}

	@Override
	public String input() {
		setting = baseManager.get(getUid());
		if (setting == null)
			setting = new Setting();
		return INPUT;
	}

	@Override
	public String save() {
		if (setting != null) {
			Setting temp = setting;
			setting = baseManager.get(setting.getId());
			if (setting != null) {
				setting.setKey(temp.getKey());
				setting.setValue(temp.getValue());
			} else {
				setting = temp;
			}
			baseManager.save(setting);
			addActionMessage(getText("save.success"));
		}
		return SUCCESS;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			List<Setting> list;
			if (id.length == 1) {
				list = new ArrayList<Setting>(1);
				list.add(baseManager.get(id[0]));
			} else {
				DetachedCriteria dc = baseManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = baseManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (Setting temp : list) {
					if (!baseManager.canDelete(temp)) {
						addActionError(temp.getKey()
								+ getText("delete.forbidden",
										new String[] { setting.getKey() }));
						deletable = false;
						break;
					}
				}
				if (deletable) {
					for (Setting temp : list)
						baseManager.delete(temp);
					addActionMessage(getText("delete.success"));
				}
			}
		}
		return SUCCESS;
	}
}
