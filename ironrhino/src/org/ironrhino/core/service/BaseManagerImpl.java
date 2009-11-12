package org.ironrhino.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.hibernate.CustomizableEntityChanger;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.model.AbstractTreeableEntity;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.model.Customizable;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.model.Recordable;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.model.Treeable;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.core.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

public class BaseManagerImpl<T extends Persistable> implements BaseManager<T> {

	protected Log log = LogFactory.getLog(BaseManagerImpl.class);

	protected Class<T> entityClass;

	private ThreadLocal<Class<T>> entityClassHolder = new ThreadLocal<Class<T>>();

	protected SessionFactory sessionFactory;

	@Required
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public BaseManagerImpl() {
		Class clazz = ReflectionUtils.getGenericClass(getClass());
		if (clazz != null)
			entityClass = clazz;
	}

	public BaseManagerImpl(Class<T> clazz) {
		entityClass = clazz;
	}

	public void setEntityClass(Class<T> clazz) {
		entityClassHolder.set(clazz);
	}

	public Class<T> getEntityClass() {
		if (entityClass != null)
			return entityClass;
		return entityClassHolder.get();
	}

	@Transactional
	public void save(T obj) {
		Session session = sessionFactory.getCurrentSession();
		if (obj instanceof Recordable) {
			Recordable r = (Recordable) obj;
			Date date = new Date();
			r.setModifyDate(date);
			if (obj.isNew())
				r.setCreateDate(date);
		}
		if (obj instanceof AbstractTreeableEntity) {
			final AbstractTreeableEntity entity = (AbstractTreeableEntity) obj;
			if (entity.isNew()) {
				FlushMode mode = session.getFlushMode();
				session.setFlushMode(FlushMode.MANUAL);
				entity.setFullId("");
				session.save(entity);
				session.flush();
				session.setFlushMode(mode);
			}
			String fullId;
			if (entity.getParent() == null)
				fullId = String.valueOf(entity.getId());
			else
				fullId = ((AbstractTreeableEntity) entity.getParent())
						.getFullId()
						+ "." + String.valueOf(entity.getId());
			entity.setFullId(fullId);
			entity.setLevel(fullId.split("\\.").length);
		}
		if (obj instanceof Customizable) {
			CustomizableEntityChanger
					.convertCustomPropertiesType((Customizable) obj);
		}
		session.saveOrUpdate(obj);
	}

	@Transactional
	public void delete(T obj) {
		sessionFactory.getCurrentSession().delete(obj);
	}

	@Transactional(readOnly = true)
	public T get(Serializable id) {
		if (id == null)
			return null;
		return (T) sessionFactory.getCurrentSession().get(getEntityClass(), id);
	}

	@Transactional(readOnly = true)
	public void lock(T obj, LockMode mode) {
		sessionFactory.getCurrentSession().lock(obj, mode);
	}

	@Transactional(readOnly = true)
	public void evict(T obj) {
		sessionFactory.getCurrentSession().evict(obj);
	}

	@Transactional(readOnly = true)
	public void clear() {
		sessionFactory.getCurrentSession().clear();
	}

	public DetachedCriteria detachedCriteria() {
		return DetachedCriteria.forClass(getEntityClass());
	}

	@Transactional(readOnly = true)
	public int countByCriteria(DetachedCriteria dc) {
		Criteria c = dc.getExecutableCriteria(sessionFactory
				.getCurrentSession());
		c.setProjection(Projections.projectionList()
				.add(Projections.rowCount()));
		return (Integer) c.uniqueResult();

	}

	@Transactional(readOnly = true)
	public T getByCriteria(DetachedCriteria dc) {
		Criteria c = dc.getExecutableCriteria(sessionFactory
				.getCurrentSession());
		c.setMaxResults(1);
		c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		return (T) c.uniqueResult();
	}

	@Transactional(readOnly = true)
	public List<T> getListByCriteria(DetachedCriteria dc) {
		try {
			Criteria c = dc.getExecutableCriteria(sessionFactory
					.getCurrentSession());
			c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			return c.list();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ArrayList<T>();
		}
	}

	@Transactional(readOnly = true)
	public List<T> getBetweenListByCriteria(DetachedCriteria dc, int start,
			int end) {
		try {
			Criteria c = dc.getExecutableCriteria(sessionFactory
					.getCurrentSession());
			int firstResult = start;
			if (firstResult < 0)
				firstResult = 0;
			c.setFirstResult(firstResult);
			int maxResults = end - firstResult;
			if (maxResults > 0)
				c.setMaxResults(maxResults);
			return c.list();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ArrayList<T>();
		}
	}

	@Transactional(readOnly = true)
	public List<T> getListByCriteria(DetachedCriteria dc, int pageNo,
			int pageSize) {
		return getBetweenListByCriteria(dc, (pageNo - 1) * pageSize, pageNo
				* pageSize);
	}

	@Transactional(readOnly = true)
	public int countResultPage(ResultPage<T> resultPage) {
		if (resultPage.getTotalRecord() < 0) {
			int totalRecord = countByCriteria(resultPage.getDetachedCriteria());
			resultPage.setTotalRecord(totalRecord);
		}
		return resultPage.getTotalRecord();
	}

	@Transactional(readOnly = true)
	public ResultPage<T> getResultPage(ResultPage<T> resultPage) {
		countResultPage(resultPage);
		int totalRecord = resultPage.getTotalRecord();
		if (resultPage.getPageSize() < 1)
			resultPage.setPageSize(ResultPage.DEFAULT_PAGE_SIZE);
		else if (resultPage.getPageSize() > ResultPage.MAX_RECORDS_PER_PAGE)
			resultPage.setPageSize(ResultPage.MAX_RECORDS_PER_PAGE);
		int totalPage = totalRecord % resultPage.getPageSize() == 0 ? totalRecord
				/ resultPage.getPageSize()
				: totalRecord / resultPage.getPageSize() + 1;
		resultPage.setTotalPage(totalPage);
		if (resultPage.getPageNo() < 1)
			resultPage.setPageNo(1);
		else if (resultPage.getPageNo() > resultPage.getTotalPage())
			resultPage.setPageNo(resultPage.getTotalPage());
		resultPage.getDetachedCriteria().setProjection(null);
		resultPage.getDetachedCriteria().setResultTransformer(
				CriteriaSpecification.ROOT_ENTITY);
		for (Order order : resultPage.getOrders())
			resultPage.getDetachedCriteria().addOrder(order);
		int start, end;
		if (!resultPage.isReverse()) {
			start = (resultPage.getPageNo() - 1) * resultPage.getPageSize();
			end = resultPage.getPageNo() * resultPage.getPageSize();
		} else {
			start = resultPage.getTotalRecord() - resultPage.getPageNo()
					* resultPage.getPageSize();
			end = resultPage.getTotalRecord() - (resultPage.getPageNo() - 1)
					* resultPage.getPageSize();
		}
		if (totalRecord > 0)
			resultPage.setResult(getBetweenListByCriteria(resultPage
					.getDetachedCriteria(), start, end));
		else
			resultPage.setResult(Collections.EMPTY_LIST);
		resultPage.setStart(start);
		return resultPage;
	}

	@Transactional(readOnly = true)
	public int countAll() {
		return countByCriteria(detachedCriteria());
	}

	@Transactional(readOnly = true)
	public List<T> getAll(int pageNo, int pageSize, Order... orders) {

		Criteria c = sessionFactory.getCurrentSession().createCriteria(
				getEntityClass());
		if (pageNo != 1 || (pageSize > 0 && pageSize < Integer.MAX_VALUE))
			c.setFirstResult((pageNo - 1) * pageSize).setMaxResults(pageSize);
		for (Order order : orders)
			c.addOrder(order);
		return c.list();
	}

	@Transactional(readOnly = true)
	public List<T> getAll(Order... orders) {
		Criteria c = sessionFactory.getCurrentSession().createCriteria(
				getEntityClass());
		c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		for (Order order : orders)
			c.addOrder(order);
		return c.list();
	}

	@Transactional(readOnly = true)
	public T getByNaturalId(Object... objects) {
		if (objects.length == 1) {
			Criteria c = sessionFactory.getCurrentSession().createCriteria(
					getEntityClass());
			NaturalIdentifier ni = Restrictions.naturalId();
			Set<String> naturalIds = AnnotationUtils.getAnnotatedPropertyNames(
					getEntityClass(), NaturalId.class);
			if (naturalIds.size() != 1)
				throw new IllegalArgumentException(
						"@NaturalId must and only be one");
			ni.set(naturalIds.iterator().next(), objects[0]);
			c.add(ni);
			c.setMaxResults(1);
			return (T) c.uniqueResult();
		}
		if (objects.length == 0 || objects.length % 2 != 0)
			throw new IllegalArgumentException("parameter size must be even");
		Criteria c = sessionFactory.getCurrentSession().createCriteria(
				getEntityClass());
		NaturalIdentifier ni = Restrictions.naturalId();
		int doubles = objects.length / 2;
		for (int i = 0; i < doubles; i++)
			ni.set(String.valueOf(objects[2 * i]), objects[2 * i + 1]);
		c.add(ni);
		c.setMaxResults(1);
		return (T) c.uniqueResult();
	}

	@Transactional(readOnly = true)
	public T getByNaturalId(boolean caseInsensitive, Object... objects) {
		if (!caseInsensitive)
			return getByNaturalId(objects);

		String hql = "select entity from " + getEntityClass().getName()
				+ " entity where ";
		int doubles = objects.length / 2;
		if (doubles == 1) {
			hql += "lower(entity." + String.valueOf(objects[0]) + ")=lower(?)";
		} else {
			List<String> list = new ArrayList<String>(doubles);
			for (int i = 0; i < doubles; i++)
				list.add("lower(entity." + String.valueOf(objects[2 * i])
						+ ")=lower(?)");
			hql += StringUtils.join(list, " and ");
		}
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		for (int i = 0; i < doubles; i++)
			query.setParameter(i, objects[2 * i + 1]);
		query.setMaxResults(1);
		return (T) query.uniqueResult();
	}

	@Transactional(readOnly = true)
	public List<T> query(final String hql, final Object... args) {
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		for (int i = 0; i < args.length; i++)
			query.setParameter(i, args[i]);
		return query.list();
	}

	@Transactional(readOnly = true)
	public void initialize(final Treeable<? extends Treeable> entity) {
		sessionFactory.getCurrentSession().lock(entity, LockMode.NONE);
		if (!Hibernate.isInitialized(entity.getChildren()))
			Hibernate.initialize(entity.getChildren());
		for (Treeable child : entity.getChildren())
			initialize(child);
	}

	@Transactional(readOnly = true)
	public <TE extends BaseTreeableEntity<TE>> TE loadTree(Object... args) {
		if (getEntityClass() == null
				|| !(BaseTreeableEntity.class
						.isAssignableFrom(getEntityClass())))
			throw new IllegalArgumentException(
					"entityClass mustn't be null,and must extends class 'BaseTreeableEntity'");
		try {
			TE root = (TE) getEntityClass().newInstance();
			root.setId(0L);
			root.setName("");
			if (args.length > 0) {
				loadTree1(root);
			} else {
				loadTree2(root);
			}
			return root;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private <TE extends BaseTreeableEntity<TE>> void loadTree1(TE root) {
		DetachedCriteria dc = detachedCriteria();
		dc.add(Restrictions.isNull("parent"));
		dc.addOrder(Order.asc("displayOrder"));
		dc.addOrder(Order.asc("name"));
		List<TE> list = (List<TE>) getListByCriteria(dc);
		List<TE> children = new ArrayList<TE>();
		for (TE var : list) {
			initialize(var);
			var = BeanUtils.deepClone(var);
			var.setParent(root);
			children.add(var);
		}
		root.setChildren(list);
	}

	private <TE extends BaseTreeableEntity<TE>> void loadTree2(TE root) {
		try {
			assemble(root, (List<TE>) getAll());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private <TE extends BaseTreeableEntity<TE>> void assemble(TE te,
			List<TE> list) throws Exception {
		List<TE> children = new ArrayList<TE>();
		for (TE r : list) {
			boolean isChild = false;
			if (te.getId() == 0) {
				if (r.getFullId().indexOf('.') < 0)
					isChild = true;
			} else {
				if (r.getFullId().indexOf('.') > 0
						&& te.getFullId().equals(
								r.getFullId().substring(0,
										r.getFullId().lastIndexOf('.'))))
					isChild = true;
			}
			if (isChild) {
				TE rr = (TE) te.getClass().newInstance();
				BeanUtils.copyProperties(r, rr);
				children.add(rr);
				rr.setParent(te);
			}
		}
		Collections.sort(children);
		te.setChildren(children);
		for (TE r : children)
			assemble(r, list);
	}

	@Transactional
	public int bulkUpdate(String queryString, Object... values) {
		Query queryObject = sessionFactory.getCurrentSession().createQuery(
				queryString);
		SessionFactoryUtils
				.applyTransactionTimeout(queryObject, sessionFactory);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
			}
		}
		return queryObject.executeUpdate();
	}

	@Transactional
	public Object execute(HibernateCallback callback) {
		try {
			return callback.doInHibernate(sessionFactory.getCurrentSession());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	@Transactional(readOnly = true)
	public Object executeQuery(HibernateCallback callback) {
		return execute(callback);
	}

}
