package org.ironrhino.security.acl.action;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.Redirect;
import org.ironrhino.core.security.dynauth.DynamicAuthorizer;
import org.ironrhino.core.struts.AutoConfigPackageProvider;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.struts.EntityAction;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.security.acl.model.Acl;
import org.ironrhino.security.acl.service.AclManager;
import org.ironrhino.security.model.UserRole;
import org.ironrhino.security.service.UsernameRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private String role;

	private Map<String, String> resources;

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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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
						if (EntityAction.class.equals(c)) {
							Class entityClass = ((AutoConfigPackageProvider) packageProvider)
									.getEntityClass(pc.getNamespace(),
											ac.getName());
							Authorize authorize = (Authorize) entityClass
									.getAnnotation(Authorize.class);
							if (authorize != null
									&& !authorize.authorizer().equals(
											DynamicAuthorizer.class)) {
								StringBuilder sb = new StringBuilder();
								sb.append(pc.getNamespace())
										.append(pc.getNamespace().endsWith("/") ? ""
												: "/").append(ac.getName());
								String s = sb.toString();
								temp.put(s, null);
								temp.put(s + "/input", null);
								temp.put(s + "/save", null);
								temp.put(s + "/delete", null);
							}
							continue;
						}
						Authorize authorizeOnClass = (Authorize) c
								.getAnnotation(Authorize.class);
						if (authorizeOnClass == null
								|| authorizeOnClass.authorizer().equals(
										DynamicAuthorizer.class)) {
							Set<Method> methods = AnnotationUtils
									.getAnnotatedMethods(c, Authorize.class);
							for (Method m : methods) {
								Authorize authorize = m
										.getAnnotation(Authorize.class);
								if (!authorize.authorizer().equals(
										DynamicAuthorizer.class)) {
									StringBuilder sb = new StringBuilder();
									sb.append(pc.getNamespace())
											.append(pc.getNamespace().endsWith(
													"/") ? "" : "/")
											.append(ac.getName());
									if (!m.getName().equals("execute"))
										sb.append("/").append(m.getName());
									temp.put(sb.toString(), null);
								}
							}
						} else if (authorizeOnClass != null
								&& !authorizeOnClass.authorizer().equals(
										DynamicAuthorizer.class)) {
							for (Method m : c.getMethods()) {
								int mod = m.getModifiers();
								if (!Modifier.isPublic(mod)
										|| Modifier.isStatic(mod)
										|| !m.getReturnType().equals(
												String.class)
										|| m.getParameterTypes().length != 0)
									continue;
								Authorize authorize = m
										.getAnnotation(Authorize.class);
								if (authorize != null
										&& authorize.authorizer().equals(
												DynamicAuthorizer.class))
									continue;
								StringBuilder sb = new StringBuilder();
								sb.append(pc.getNamespace())
										.append(pc.getNamespace().endsWith("/") ? ""
												: "/").append(ac.getName());
								if (!m.getName().equals("execute"))
									sb.append("/").append(m.getName());
								temp.put(sb.toString(), null);
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
			resources.put(entry.getKey(), getText(StringUtils.isBlank(entry
					.getValue()) ? entry.getKey() : entry.getValue()));
	}

	@Override
	public String input() {
		scanResources();
		if (StringUtils.isBlank(role) && StringUtils.isNotBlank(username))
			role = UsernameRoleMapper.map(username);
		if (StringUtils.isBlank(role))
			return ACCESSDENIED;
		List<Acl> acls = aclManager.findAclsByRole(role);
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
		if (StringUtils.isBlank(role) && StringUtils.isNotBlank(username))
			role = UsernameRoleMapper.map(username);
		if (StringUtils.isBlank(role))
			return ACCESSDENIED;
		String[] ids = getId();
		List<String> permitted;
		if (ids == null || ids.length == 0)
			permitted = Collections.EMPTY_LIST;
		else
			permitted = Arrays.asList(ids);
		for (String resource : resources.keySet()) {
			Acl acl = aclManager.findAcl(role, resource);
			if (acl != null)
				acl.setPermitted(permitted.contains(resource));
			else
				acl = new Acl(role, resource, permitted.contains(resource));
			aclManager.save(acl);
		}
		return SUCCESS;
	}

}
