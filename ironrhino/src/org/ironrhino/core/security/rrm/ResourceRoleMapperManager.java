package org.ironrhino.core.security.rrm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;

@Singleton
@Named("resourceRoleMapperManager")
public class ResourceRoleMapperManager {

	private Map<String, ResourceRoleMapper> mappers;

	@Inject
	private ApplicationContext ctx;

	@PostConstruct
	public void init() {
		Collection<ResourceRoleMapper> beans = ctx.getBeansOfType(
				ResourceRoleMapper.class).values();
		if (beans.size() > 0){
			mappers = new HashMap<String, ResourceRoleMapper>(beans.size());
			for (ResourceRoleMapper c : beans)
				mappers.put(c.getClass().getName(), c);
		}
	}

	public String map(String mapperName, String resource, UserDetails user) {
		if (mappers != null) {
			ResourceRoleMapper mapper = mappers.get(mapperName);
			if (mapper != null) {
				String roles = mapper.map(resource, user);
				if (roles != null)
					return roles;
			}
		}
		return mapperName;
	}

}
