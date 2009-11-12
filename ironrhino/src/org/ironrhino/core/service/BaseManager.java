package org.ironrhino.core.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.model.Treeable;
import org.springframework.orm.hibernate3.HibernateCallback;

public interface BaseManager<T extends Persistable> {

	public void setSessionFactory(SessionFactory sessionFactory);

	public Class<T> getEntityClass();

	public void setEntityClass(Class<T> clazz);

	public void save(T obj);

	public T get(Serializable id);

	public void delete(T obj);

	public void lock(T obj, LockMode mode);

	public void evict(T obj);

	public void clear();

	public DetachedCriteria detachedCriteria();

	public int countByCriteria(DetachedCriteria dc);

	public T getByCriteria(DetachedCriteria dc);

	public List<T> getListByCriteria(DetachedCriteria dc);

	public List<T> getBetweenListByCriteria(final DetachedCriteria dc,
			int from, int end);

	public List<T> getListByCriteria(DetachedCriteria dc, int pageNo,
			int pageSize);

	public int countResultPage(ResultPage<T> resultPage);

	public ResultPage<T> getResultPage(ResultPage<T> resultPage);

	public int countAll();

	public List<T> getAll(int pageNo, int pageSize, Order... orders);

	public T getByNaturalId(Object... objects);

	public T getByNaturalId(boolean caseInsensitive, Object... objects);

	public List<T> getAll(Order... orders);

	public List<T> query(String hql, Object... args);

	public void initialize(Treeable<? extends Treeable> entity);

	public <TE extends BaseTreeableEntity<TE>> TE loadTree(Object... args);

	public int bulkUpdate(String hql, Object... args);

	public Object execute(HibernateCallback callback);

	public Object executeQuery(HibernateCallback callback);

}
