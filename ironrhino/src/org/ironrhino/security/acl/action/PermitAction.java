package org.ironrhino.security.acl.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.Redirect;
import org.ironrhino.core.struts.AutoConfigPackageProvider;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.security.acl.component.AclResourceRoleMapper;
import org.ironrhino.security.acl.model.Acl;
import org.ironrhino.security.acl.service.AclManager;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.ironrhino.security.service.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig
@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
public class PermitAction extends BaseAction {

	private static final long serialVersionUID = 8175406812208878896L;

	protected static Logger log = LoggerFactory.getLogger(PermitAction.class);

	private static Map<String, String> resourcesCache;

	private String username;

	private Map<String, String> resources;

	@Inject
	private transient UserManager userManager;

	@Inject
	private transient AclManager aclManager;

	@com.opensymphony.xwork2.inject.Inject("ironrhino-autoconfig")
	private transient PackageProvider packageProvider;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Map<String, String> getResources() {
		return resources;
	}

	private void scanResources() {
		if (resourcesCache == null) {
			HashMap<String, String> temp = new LinkedHashMap<String, String>();
			Collection<PackageConfig> pcs = ((AutoConfigPackageProvider) packageProvider)
					.getAllPackageConfigs();
			for (PackageConfig pc : pcs) {
				Collection<ActionConfig> acs = pc.getActionConfigs().values();
				for (ActionConfig ac : acs) {
					try {
						Class c = Class.forName(ac.getClassName());
						if (!BaseAction.class.isAssignableFrom(c))
							continue;
						Set<Method> methods = AnnotationUtils
								.getAnnotatedMethods(c, Authorize.class);
						for (Method m : methods) {
							Authorize authorize = m
									.getAnnotation(Authorize.class);
							if (authorize.ifAnyGranted().equals(
									AclResourceRoleMapper.class.getName())
									|| authorize.ifAllGranted().equals(
											AclResourceRoleMapper.class
													.getName())) {
								StringBuilder sb = new StringBuilder();
								sb.append(pc.getNamespace())
										.append(pc.getNamespace().endsWith("/") ? ""
												: "/").append(ac.getName());
								if (!m.getName().equals("execute"))
									sb.append("/").append(m.getName());
								temp.put(
										sb.toString(),
										!authorize.resourceName().equals("") ? authorize
												.resourceName() : sb.toString());
							}
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			resourcesCache = temp;
		}
		resources = new LinkedHashMap<String, String>(resourcesCache.size());
		for (Map.Entry<String, String> entry : resourcesCache.entrySet())
			resources.put(entry.getKey(), getText(entry.getValue()));
	}

	@Override
	public String input() {
		scanResources();
		User user = null;
		try {
			user = (User) userManager.loadUserByUsername(username);
			if (user == null)
				return ACCESSDENIED;
		} catch (UsernameNotFoundException e) {
			return ACCESSDENIED;
		}
		List<Acl> acls = aclManager.findAclsByUsername(user.getUsername());
		List<String> permitted = new ArrayList<String>();
		for (Acl acl : acls) {
			if (acl.isPermitted())
				permitted.add(acl.getResource());
		}
		setId(permitted.toArray(new String[0]));
		return SUCCESS;
	}

	@Override
	@Redirect
	@InputConfig(methodName = "input")
	public String execute() {
		scanResources();
		User user = null;
		try {
			user = (User) userManager.loadUserByUsername(username);
			if (user == null)
				return ACCESSDENIED;
		} catch (UsernameNotFoundException e) {
			return ACCESSDENIED;
		}
		String[] ids = getId();
		List<String> permitted;
		if (ids == null || ids.length == 0)
			permitted = Collections.EMPTY_LIST;
		else
			permitted = Arrays.asList(ids);
		for (String resource : resources.keySet()) {
			Acl acl = aclManager.findAcl(user.getUsername(), resource);
			if (acl != null)
				acl.setPermitted(permitted.contains(resource));
			else
				acl = new Acl(user.getUsername(), resource,
						permitted.contains(resource));

			aclManager.save(acl);
		}
		return SUCCESS;
	}
}
