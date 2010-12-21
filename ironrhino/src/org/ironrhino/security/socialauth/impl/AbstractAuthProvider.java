package org.ironrhino.security.socialauth.impl;

import javax.inject.Inject;

import org.ironrhino.common.support.SettingControl;
import org.ironrhino.security.socialauth.AuthProvider;

public abstract class AbstractAuthProvider implements AuthProvider {

	@Inject
	protected SettingControl settingControl;

	public String getName() {
		return getClass().getSimpleName().toLowerCase();
	}

	public boolean isEnabled() {
		return settingControl.getBooleanValue("socialauth." + getName()
				+ ".enabled", true);
	}

	public int getDisplayOrder() {
		return settingControl.getIntValue("socialauth." + getName()
				+ ".displayOrder", 0);
	}

	public String toString() {
		return getName();
	}

	public int compareTo(AuthProvider object) {
		if (!(object instanceof AbstractAuthProvider))
			return 0;
		AbstractAuthProvider ap = (AbstractAuthProvider) object;
		if (this.getDisplayOrder() != ap.getDisplayOrder())
			return this.getDisplayOrder() - ap.getDisplayOrder();
		return this.toString().compareTo(ap.toString());
	}
}
