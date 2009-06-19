package org.ironrhino.common.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.hibernate.CustomizableEntityChanger;
import org.ironrhino.core.ext.hibernate.PropertyChange;
import org.ironrhino.core.ext.struts.BaseAction;

import com.opensymphony.xwork2.util.CreateIfNull;

@AutoConfig
public class CustomizeEntityAction extends BaseAction {

	private transient CustomizableEntityChanger customizableEntityChanger;

	private String entityClassName;

	private List<PropertyChange> changes;

	@CreateIfNull
	public List<PropertyChange> getChanges() {
		return changes;
	}

	public void setChanges(List<PropertyChange> changes) {
		this.changes = changes;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	public void setEntityClassName(String entityClassName) {
		this.entityClassName = entityClassName;
	}

	public void setCustomizableEntityChanger(
			CustomizableEntityChanger customizableEntityChanger) {
		this.customizableEntityChanger = customizableEntityChanger;
	}

	public CustomizableEntityChanger getCustomizableEntityChanger() {
		return customizableEntityChanger;
	}

	public String execute() {
		try {
			if (StringUtils.isNotBlank(entityClassName)) {
				if (changes != null)
					for (PropertyChange change : changes)
						if (change != null
								&& StringUtils.isNotBlank(change.getName()))
							customizableEntityChanger.prepareChange(
									entityClassName, change);
				if (getId() != null)
					for (String name : getId())
						customizableEntityChanger
								.prepareChange(entityClassName,
										new PropertyChange(name, true));
			}
		} catch (Exception e) {
			addActionError(e.getMessage());
		}
		return SUCCESS;
	}

}
