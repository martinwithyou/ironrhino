package org.ironrhino.security.socialauth.impl;

import javax.inject.Inject;

import org.ironrhino.common.support.SettingControl;
import org.ironrhino.security.socialauth.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAuthProvider implements AuthProvider {

	protected static Logger logger = LoggerFactory.getLogger(AuthProvider.class);
	
	@Inject
	protected SettingControl settingControl;

	protected boolean forceDisabled;
	
	public boolean isDiscoverable() {
		return false;
	}

	public void setForceDisabled(boolean forceDisabled) {
		this.forceDisabled = forceDisabled;
	}

	public String getName() {
		return getClass().getSimpleName().toLowerCase();
	}

	public boolean isEnabled() {
		return !forceDisabled
				&& settingControl.getBooleanValue("socialauth." + getName()
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
