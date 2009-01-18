package org.ironrhino.core.ext.struts;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.model.Entity;
import org.ironrhino.core.util.ClassScaner;
import org.springframework.util.ClassUtils;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.config.entities.ResultTypeConfig;
import com.opensymphony.xwork2.config.providers.InterceptorBuilder;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigPackageProvider implements PackageProvider {

	private static final Log log = LogFactory
			.getLog(AutoConfigPackageProvider.class);

	private String parentPackage = "ironrhino-default";

	private String defaultActionClass = EntityAction.class.getName();

	private Map<String, Set<String>> packages = new HashMap<String, Set<String>>();

	private Configuration configuration;

	private boolean initialized = false;

	private PackageLoader packageLoader;

	private ObjectFactory objectFactory;

	@Inject("ironrhino.autoconfig.packages")
	public void setPackages(String val) {
		if (StringUtils.isNotBlank(val)) {
			String[] array = val.split(";");
			for (String s : array) {
				String[] arr = s.split(":");
				String defaultNamespace = "";
				String packs;
				if (arr.length == 1) {
					packs = arr[0];
				} else {
					defaultNamespace = arr[0];
					packs = arr[1];
				}
				Set<String> set = packages.get(defaultNamespace);
				if (set == null) {
					set = new HashSet<String>();
					packages.put(defaultNamespace, set);
				}
				set.addAll(Arrays.asList(packs.split(",")));
			}
		}
	}

	@Inject(value = "ironrhino.autoconfig.parent.package", required = false)
	public void setParentPackage(String val) {
		this.parentPackage = val;
	}

	@Inject
	public void setObjectFactory(ObjectFactory of) {
		this.objectFactory = of;
	}

	public void init(Configuration configuration) throws ConfigurationException {
		this.configuration = configuration;
	}

	public void loadPackages() throws ConfigurationException {
		if (packages.size() == 0)
			return;
		for (String defaultNamespace : packages.keySet()) {
			Set<Class> entityClasses = ClassScaner.scan(packages.get(
					defaultNamespace).toArray(new String[0]), AutoConfig.class);
			if (entityClasses.size() == 0)
				return;
			packageLoader = new PackageLoader();
			for (Class clazz : entityClasses) {
				processEntityClass(clazz, defaultNamespace);
			}
			for (PackageConfig config : packageLoader.createPackageConfigs()) {
				PackageConfig pc = configuration.getPackageConfig(config
						.getName());
				if (pc == null) {
					configuration.addPackageConfig(config.getName(), config);
				} else {
					Map<String, ActionConfig> actionConfigs = new LinkedHashMap<String, ActionConfig>(
							pc.getActionConfigs());
					for (String key : config.getActionConfigs().keySet()) {
						if (actionConfigs.containsKey(key)) {
							ActionConfig ac = actionConfigs.get(key);
							Map<String, ResultConfig> results = new HashMap<String, ResultConfig>();
							results.putAll(config.getActionConfigs().get(key)
									.getResults());
							results.putAll(ac.getResults());
							// this is a trick
							try {
								Field field = ActionConfig.class
										.getDeclaredField("results");
								field.setAccessible(true);
								field.set(ac, results);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
							continue;
						}
						actionConfigs.put(key, config.getActionConfigs().get(
								key));
					}
					// this is a trick
					try {
						Field field = PackageConfig.class
								.getDeclaredField("actionConfigs");
						field.setAccessible(true);
						field.set(pc, actionConfigs);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

			}
		}
		initialized = true;
	}

	protected void processEntityClass(Class cls, String defaultNamespace) {
		AutoConfig ac = (AutoConfig) cls.getAnnotation(AutoConfig.class);
		String[] arr = getNamespaceAndActionName(cls, defaultNamespace);
		String namespace = arr[0];
		String actionName = arr[1];
		String actionClass = arr[2];
		String packageName;
		if (!"".equals(namespace)) {
			packageName = namespace.substring(1);
			packageName = packageName.replace('/', '_');
		} else {
			packageName = "default";
		}
		PackageConfig.Builder pkgConfig = loadPackageConfig(packageName);

		ResultConfig autoCofigResult = new ResultConfig.Builder("*",
				AutoConfigResult.class.getName()).build();
		ActionConfig.Builder builder = new ActionConfig.Builder(packageName,
				actionName, actionClass).addResultConfig(autoCofigResult);
		if (StringUtils.isNotBlank(ac.fileupload())) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("allowedTypes", ac.fileupload());
			List<InterceptorMapping> interceptors = InterceptorBuilder
					.constructInterceptorReference(pkgConfig, "fileUpload",
							params, null, objectFactory);
			interceptors
					.addAll(InterceptorBuilder.constructInterceptorReference(
							pkgConfig, "annotationDefaultStack", null, null,
							objectFactory));
			builder.interceptors(interceptors);
		}
		ActionConfig actionConfig = builder.build();
		pkgConfig.addActionConfig(actionName, actionConfig);

	}

	protected PackageConfig.Builder loadPackageConfig(String packageName) {
		String namespace;
		if (packageName.equals("default"))
			namespace = "/";
		else
			namespace = "/" + packageName.replace('_', '/');
		PackageConfig.Builder pkgConfig = packageLoader.getPackage(packageName);
		if (pkgConfig == null) {
			pkgConfig = new PackageConfig.Builder(packageName);
			pkgConfig.namespace(namespace);
			PackageConfig parent = configuration
					.getPackageConfig(parentPackage);
			if (parent != null) {
				pkgConfig.addParent(parent);
			} else {
				log.error("Unable to locate parent package: " + parentPackage);
			}
			packageLoader.registerPackage(pkgConfig);
		} else if (pkgConfig.getNamespace() == null) {
			pkgConfig.namespace(namespace);
		}
		return pkgConfig;
	}

	public boolean needsReload() {
		return !initialized;
	}

	private static class PackageLoader {

		private Map<String, PackageConfig.Builder> packageConfigBuilders = new HashMap<String, PackageConfig.Builder>();

		private Map<PackageConfig.Builder, PackageConfig.Builder> childToParent = new HashMap<PackageConfig.Builder, PackageConfig.Builder>();

		public PackageConfig.Builder getPackage(String name) {
			return packageConfigBuilders.get(name);
		}

		public void registerChildToParent(PackageConfig.Builder child,
				PackageConfig.Builder parent) {
			childToParent.put(child, parent);
		}

		public void registerPackage(PackageConfig.Builder builder) {
			packageConfigBuilders.put(builder.getName(), builder);
		}

		public Collection<PackageConfig> createPackageConfigs() {
			Map<String, PackageConfig> configs = new HashMap<String, PackageConfig>();

			Set<PackageConfig.Builder> builders;
			while ((builders = findPackagesWithNoParents()).size() > 0) {
				for (PackageConfig.Builder parent : builders) {
					PackageConfig config = parent.build();
					configs.put(config.getName(), config);
					packageConfigBuilders.remove(config.getName());

					for (Iterator<Map.Entry<PackageConfig.Builder, PackageConfig.Builder>> i = childToParent
							.entrySet().iterator(); i.hasNext();) {
						Map.Entry<PackageConfig.Builder, PackageConfig.Builder> entry = i
								.next();
						if (entry.getValue() == parent) {
							entry.getKey().addParent(config);
							i.remove();
						}
					}
				}
			}
			return configs.values();
		}

		Set<PackageConfig.Builder> findPackagesWithNoParents() {
			Set<PackageConfig.Builder> builders = new HashSet<PackageConfig.Builder>();
			for (PackageConfig.Builder child : packageConfigBuilders.values()) {
				if (!childToParent.containsKey(child)) {
					builders.add(child);
				}
			}
			return builders;
		}

		public ResultTypeConfig getDefaultResultType(
				PackageConfig.Builder pkgConfig) {
			PackageConfig.Builder parent;
			PackageConfig.Builder current = pkgConfig;

			while ((parent = childToParent.get(current)) != null) {
				current = parent;
			}
			return current.getResultType(current.getFullDefaultResultType());
		}
	}

	private Map<String, Class> entityClassURLMapping = new ConcurrentHashMap<String, Class>();

	public String[] getNamespaceAndActionName(Class cls, String defaultNamespace) {
		String actionName = null;
		String namespace = null;
		String actionClass = null;
		AutoConfig ac = (AutoConfig) cls.getAnnotation(AutoConfig.class);
		if (Entity.class.isAssignableFrom(cls)) {
			actionName = StringUtils.uncapitalize(cls.getSimpleName());
			namespace = ac.namespace();
			if (StringUtils.isBlank(namespace))
				namespace = defaultNamespace;
			Class action = ac.action();
			if (action.equals(Object.class)) {
				actionClass = cls.getName().replace("model", "action")
						+ "Action";
				if (!ClassUtils.isPresent(actionClass,getClass().getClassLoader()))
					actionClass = defaultActionClass;
			} else {
				actionClass = action.getName();
			}

			entityClassURLMapping.put(namespace + "/" + actionName, cls);
		} else if (Action.class.isAssignableFrom(cls)) {
			actionName = StringUtils.uncapitalize(cls.getSimpleName());
			if (actionName.endsWith("Action"))
				actionName = actionName.substring(0, actionName.length() - 6);
			actionClass = cls.getName();
			namespace = ac.namespace();
			if (StringUtils.isBlank(namespace))
				namespace = defaultNamespace;
		}
		return new String[] { namespace, actionName, actionClass };
	}

	public Class getEntityClass(String namespace, String actionName) {
		if (namespace.endsWith("/"))
			namespace = "";
		return entityClassURLMapping.get(namespace + "/" + actionName);
	}

}
