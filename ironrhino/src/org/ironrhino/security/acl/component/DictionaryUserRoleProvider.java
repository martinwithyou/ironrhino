package org.ironrhino.security.acl.component;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.common.support.DictionaryControl;
import org.ironrhino.security.service.UserRoleProvider;

@Singleton
@Named
public class DictionaryUserRoleProvider implements UserRoleProvider {

	public static final String DICTIONARY_NAME = "CustomRole";

	@Inject
	private DictionaryControl dictionaryControl;

	@Override
	public Map<String, String> getRoles() {
		return dictionaryControl.getItemsAsMap(DICTIONARY_NAME);
	}

}
