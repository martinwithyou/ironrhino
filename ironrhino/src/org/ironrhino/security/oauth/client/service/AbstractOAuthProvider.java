package org.ironrhino.security.oauth.client.service;

import javax.inject.Inject;

import org.ironrhino.common.support.SettingControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOAuthProvider implements OAuthProvider {

	protected static Logger logger = LoggerFactory
			.getLogger(OAuthProvider.class);

	@Inject
	protected SettingControl settingControl;

	protected boolean forceDisabled;

	public void setForceDisabled(boolean forceDisabled) {
		this.forceDisabled = forceDisabled;
	}

	public String getName() {
		return getClass().getSimpleName().toLowerCase();
	}

	public boolean isEnabled() {
		return !forceDisabled
				&& settingControl.getBooleanValue("oauth." + getName()
						+ ".enabled", true);
	}

	public int getDisplayOrder() {
		return settingControl.getIntValue("oauth." + getName()
				+ ".displayOrder", 0);
	}

	protected boolean isUseAuthorizationHeader() {
		return true;
	}

	protected String generateId(String uid) {
		return "(" + getName() + ")" + uid;
	}

	public String toString() {
		return getName();
	}

	public int compareTo(OAuthProvider object) {
		if (!(object instanceof AbstractOAuthProvider))
			return 0;
		AbstractOAuthProvider ap = (AbstractOAuthProvider) object;
		if (this.getDisplayOrder() != ap.getDisplayOrder())
			return this.getDisplayOrder() - ap.getDisplayOrder();
		return this.toString().compareTo(ap.toString());
	}

}
