package org.ironrhino.core.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.model.ResultPage;
import org.springframework.orm.hibernate3.HibernateCallback;

public interface BaseManager<T extends Persistable> {

	public Class<T> getEntityClass();

	public void setEntityClass(Class<T> clazz);

	public void save(T obj);

	public T get(Serializable id);

	public void delete(T obj);

	public void clear();

	public DetachedCriteria detachedCriteria();

	public int countByCriteria(DetachedCriteria dc);

	public T findByCriteria(DetachedCriteria dc);

	public List<T> findListByCriteria(DetachedCriteria dc);

	public List<T> findBetweenListByCriteria(final DetachedCriteria dc,
			int from, int end);

	public List<T> findListByCriteria(DetachedCriteria dc, int pageNo,
			int pageSize);

	public ResultPage<T> findByResultPage(ResultPage<T> resultPage);

	public int countAll();

	public T findByNaturalId(Object... objects);

	public T findByNaturalId(boolean caseInsensitive, Object... objects);

	public List<T> findAll(Order... orders);

	public List<T> find(String queryString, Object... args);

	public <TE extends BaseTreeableEntity<TE>> TE loadTree();

	public int bulkUpdate(String queryString, Object... args);

	public Object execute(HibernateCallback callback);

	public Object executeFind(HibernateCallback callback);

}
