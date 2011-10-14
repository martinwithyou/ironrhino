package org.ironrhino.common.support;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.security.rrm.ResourceRoleMapper;
import org.springframework.security.core.userdetails.UserDetails;

@Singleton
@Named
public class SettingResourceRoleMapper implements ResourceRoleMapper {

	@Inject
	protected SettingControl settingControl;

	public String map(String resource, UserDetails user) {
		return settingControl.getStringValue("resource:" + resource);
	}

}
