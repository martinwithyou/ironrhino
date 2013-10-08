package org.ironrhino.core.service;

import org.ironrhino.core.model.Persistable;
import org.springframework.stereotype.Component;

@Component
public class EntityManagerImpl<T extends Persistable<?>> extends
		BaseManagerImpl<T> implements EntityManager<T> {

	private ThreadLocal<Class<T>> entityClassHolder = new ThreadLocal<Class<T>>();

	@Override
	public void setEntityClass(Class<T> clazz) {
		entityClassHolder.set(clazz);
	}

	@Override
	public Class<? extends Persistable<?>> getEntityClass() {
		return entityClassHolder.get();
	}

}
