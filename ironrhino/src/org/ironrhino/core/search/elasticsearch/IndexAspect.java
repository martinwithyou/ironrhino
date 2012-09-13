package org.ironrhino.core.search.elasticsearch;

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

	@AfterReturning("execution(* org.ironrhino..service.*Manager.save*(*)) and args(entity) and @args(searchable)")
	public void save(Persistable entity, Searchable searchable)
			throws Throwable {
		if (AopContext.isBypass(this.getClass()))
			return;
		indexManager.index(entity);
	}

	@AfterReturning("execution(* org.ironrhino..service.*Manager.delete*(*)) and args(entity) and @args(searchable)")
	public void delete(Persistable entity, Searchable searchable) {
		if (AopContext.isBypass(this.getClass()))
			return;
		indexManager.delete(entity);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
