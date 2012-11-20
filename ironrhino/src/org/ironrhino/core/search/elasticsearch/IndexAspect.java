package org.ironrhino.core.search.elasticsearch;

import java.util.List;

import javax.inject.Inject;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.aop.AopContext;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.springframework.core.Ordered;

@Aspect
@SuppressWarnings("rawtypes")
public class IndexAspect implements Ordered {

	@Inject
	private IndexManager indexManager;

	private int order;

	public IndexAspect() {
		order = 1;
	}

	@AfterReturning(pointcut = "execution(java.util.List org.ironrhino.core.service.BaseManager.delete(*)) ", returning = "list")
	public void deleteBatch(List list) throws Throwable {
		if (!AopContext.isBypass(this.getClass()) && list != null)
			for (Object entity : list) {
				Searchable searchable = entity.getClass().getAnnotation(
						Searchable.class);
				if (searchable != null)
					indexManager.delete((Persistable) entity);
			}
	}

	@AfterReturning("execution(* org.ironrhino.core.service.BaseManager.delete(*)) and args(entity) and @args(searchable)")
	public void delete(Persistable entity, Searchable searchable) {
		if (!AopContext.isBypass(this.getClass()))
			indexManager.delete(entity);
	}

	@AfterReturning("execution(* org.ironrhino.core.service.BaseManager.save(*)) and args(entity) and @args(searchable)")
	public void save(Persistable entity, Searchable searchable)
			throws Throwable {
		if (!AopContext.isBypass(this.getClass()))
			indexManager.index(entity);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
