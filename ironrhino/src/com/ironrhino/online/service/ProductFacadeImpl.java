package com.ironrhino.online.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.NativeSql;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Component;

import com.ironrhino.online.action.ProductAction;
import com.ironrhino.online.model.ProductScore;
import com.ironrhino.pms.model.Category;
import com.ironrhino.pms.model.Product;
import com.ironrhino.pms.service.ProductManager;
import com.ironrhino.pms.support.CategoryTreeControl;

@Component("productFacade")
public class ProductFacadeImpl implements ProductFacade {

	protected final Log log = LogFactory.getLog(ProductFacadeImpl.class);

	@Autowired
	private ProductManager productManager;

	@Autowired
	private BaseManager baseManager;

	@Autowired
	private SettingControl settingControl;

	@Autowired(required = false)
	private CategoryTreeControl categoryTreeControl;

	private Random random;

	@PostConstruct
	public void afterPropertiesSet() {
		random = new Random();
	}

	@Override
	public List<Product> getNewArrivalProducts(int... parameters) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.addOrder(Order.asc("displayOrder"));
		dc.addOrder(Order.desc("modifyDate"));
		Integer maxResults = null;
		Integer beforeDays = null;
		if (parameters.length >= 1)
			maxResults = parameters[0];
		if (parameters.length >= 2)
			beforeDays = parameters[1];
		if (maxResults == null)
			maxResults = Integer.MAX_VALUE;
		if (beforeDays != null) {
			Date date = new Date();
			dc.add(Restrictions.between("modifyDate", DateUtils.addDays(date,
					-beforeDays), date));
		}
		return productManager.getListByCriteria(dc, 1, maxResults);
	}

	@Override
	public List<Product> getProducts(int max) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.addOrder(Order.asc("displayOrder"));
		dc.addOrder(Order.desc("modifyDate"));
		return productManager.getListByCriteria(dc, 1, max);
	}

	@Override
	public List<Product> getProducts(String[] codeArray) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		dc.setFetchMode("category", FetchMode.JOIN);
		dc.setFetchMode("attributes", FetchMode.JOIN);
		dc.setFetchMode("roles", FetchMode.JOIN);
		dc.setFetchMode("relatedProducts", FetchMode.JOIN);
		dc.add(Restrictions.in("code", codeArray));
		List<Product> result = productManager.getListByCriteria(dc);
		List<Product> list = new ArrayList<Product>(result.size());
		// sort by array
		for (String code : codeArray)
			for (Product p : result)
				if (p.getCode().equals(code)) {
					list.add(p);
					break;
				}
		return list;
	}

	@Override
	public List<Product> getRecommendedProducts() {
		List<Product> list;
		String[] codeArray = settingControl
				.getStringArray(SETTING_KEY_RECOMMENDED_PRODUCT);
		if (codeArray.length > 0) {
			list = getProducts(codeArray);
		} else {
			list = Collections.EMPTY_LIST;
		}
		return list;
	}

	/**
	 * categoryId 0->all,-1->no category
	 */
	@Override
	public ResultPage getResultPageByCategoryId(ResultPage<Product> resultPage,
			Integer categoryId) {
		DetachedCriteria dc = prepareDetachedCriteria();
		resultPage.setDetachedCriteria(dc);
		if (categoryId != null)
			if (categoryId > 0)
				dc.createAlias("category", "c").add(
						Restrictions.disjunction().add(
								Restrictions.eq("c.id", categoryId)).add(
								Restrictions
										.like("c.fullId", categoryId + ".%")));
			else
				dc.add(Restrictions.isNull("category"));
		resultPage.addOrder(Order.asc("displayOrder"));
		resultPage.addOrder(Order.desc("modifyDate"));
		resultPage = productManager.getResultPage(resultPage);
		return resultPage;
	}

	@Override
	public ResultPage getResultPageByCategoryCode(
			ResultPage<Product> resultPage, String categoryCode) {
		Integer id = null;
		if (StringUtils.isNotBlank(categoryCode)) {
			if (categoryCode
					.equalsIgnoreCase(ProductAction.PSEUDO_CATEGORY_CODE_NULL))
				id = -1;
			else {
				Category cate = categoryTreeControl.getCategoryTree()
						.getDescendantOrSelfByCode(categoryCode);
				if (cate != null)
					id = cate.getId();
			}
		}
		return getResultPageByCategoryId(resultPage, id);
	}

	@Override
	public ResultPage getResultPageByTag(ResultPage<Product> resultPage,
			String tag) {
		DetachedCriteria dc = prepareDetachedCriteria();
		resultPage.setDetachedCriteria(dc);
		// dc.createAlias("tags", "tag").add(Restrictions.eq("tag", tag));//not
		// works
		@NativeSql
		String sql = "{alias}.id in (select p.id from pms_product p left join pms_productTag pt on p.id = pt.productId where pt.value =?)";
		dc.add(Restrictions.sqlRestriction(sql, tag, Hibernate.STRING));
		resultPage.addOrder(Order.asc("displayOrder"));
		resultPage.addOrder(Order.desc("modifyDate"));
		resultPage = productManager.getResultPage(resultPage);
		return resultPage;
	}

	@Override
	@CheckCache(key = "${args[0]}", namespace = "product", onHit = "${org.ironrhino.core.stat.StatLog.add({'cache','product','hit'})}", onMiss = "${org.ironrhino.core.stat.StatLog.add({'cache','product','miss'})}")
	public Product getProductByCode(String code) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.add(Restrictions.naturalId().set("code", code));
		Product product = productManager.getByCriteria(dc);
		return product;
	}

	@Override
	public Product getRandomProduct() {
		int count = productManager.countByCriteria(prepareDetachedCriteria());
		if (count == 0)
			return new Product();
		int rand = random.nextInt(count);
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.setFetchMode("attributes", FetchMode.JOIN);
		dc.setFetchMode("category", FetchMode.JOIN);
		List<Product> list = productManager.getBetweenListByCriteria(dc, rand,
				rand + 1);
		Product product = list.get(0);
		return product;
	}

	@Override
	@CheckCache(key = "score_${args[0]}", namespace = "product")
	public AggregateResult getScoreResult(final String productCode) {
		baseManager.setEntityClass(ProductScore.class);
		final DetachedCriteria dc = baseManager.detachedCriteria();
		dc.setProjection(Projections.projectionList().add(
				Projections.count("score")).add(Projections.avg("score")));
		dc.add(Restrictions.eq("productCode", productCode));
		Object[] array = (Object[]) baseManager
				.executeQuery(new HibernateCallback() {
					@Override
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						return dc.getExecutableCriteria(session).uniqueResult();
					}
				});
		AggregateResult sr = new AggregateResult();
		if (array != null) {
			sr.setPrincipal(productCode);
			sr.setCount((Number) array[0]);
			if (array[1] != null)
				sr.setAverage(NumberUtils.round(((Number) array[1])
						.doubleValue(), 1));
			else
				sr.setAverage(new Double(0.0d));
		}
		if (sr.getCount().intValue() > 0) {
			sr.setDetails((Map<Number, Number>) baseManager
					.executeQuery(new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Query q = session
									.createQuery("select ps.score,count(ps.score) from ProductScore ps where ps.productCode=? group by ps.score order by ps.score desc");
							q.setString(0, productCode);
							Map<Number, Number> details = new LinkedHashMap<Number, Number>();
							List<Object[]> result = q.list();
							for (Object[] array : result)
								details.put((Number) array[0],
										(Number) array[1]);
							return details;
						}
					}));
		}
		return sr;
	}

	@Override
	public List<AggregateResult> getTopScoreProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.executeQuery(new HibernateCallback() {
					@Override
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select ps.productCode,count(ps.score),avg(ps.score) from ProductScore ps,Product p where ps.productCode=p.code group by ps.productCode order by avg(ps.score) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal(array[0]);
							sr.setCount(((Number) array[1]).intValue());
							if (array[2] != null)
								sr.setAverage(NumberUtils.round(
										((Number) array[2]).doubleValue(), 1));
							else
								sr.setAverage(new Double(0.0d));
							list.add(sr);
						}
						return list;
					}
				});
		for (AggregateResult sr : list)
			sr.setPrincipal(getProductByCode((String) sr.getPrincipal()));
		return list;
	}

	@Override
	public List<AggregateResult> getTopFavoriteProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.executeQuery(new HibernateCallback() {
					@Override
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select pf.productCode,count(pf.username) from ProductFavorite pf,Product p where pf.productCode=p.code group by pf.productCode order by count(pf.username) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal(array[0]);
							sr.setCount(((Number) array[1]).intValue());
							list.add(sr);
						}
						return list;
					}
				});
		for (AggregateResult sr : list)
			sr.setPrincipal(getProductByCode((String) sr.getPrincipal()));
		return list;
	}

	@Override
	public List<AggregateResult> getTopSaleProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.executeQuery(new HibernateCallback() {
					@Override
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select oi.productCode,sum(oi.quantity) from Product p,Order o right join o.items oi where o.status in('PAID','SHIPPED','COMPLETED') and oi.productCode=p.code  group by oi.productCode order by sum(oi.quantity) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal(array[0]);
							sr.setCount(((Number) array[1]).intValue());
							list.add(sr);
						}
						return list;
					}
				});
		for (AggregateResult sr : list)
			sr.setPrincipal(getProductByCode((String) sr.getPrincipal()));
		return list;
	}

	@Override
	public List<AggregateResult> getTags(final String... prefix) {
		final List<AggregateResult> tags = new ArrayList<AggregateResult>();
		baseManager.executeQuery(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				SQLQuery q;
				if (prefix.length == 0 || StringUtils.isEmpty(prefix[0])) {
					@NativeSql
					String sql = "select count(value) as count,value from pms_productTag group by value order by count desc";
					q = session.createSQLQuery(sql);
				} else {
					@NativeSql
					String sql = "select count(value) as count,value from pms_productTag where value like ?  group by value order by count desc";
					q = session.createSQLQuery(sql);
					q.setString(0, prefix[0] + "%");
				}
				List<Object[]> list = q.list();
				for (Object[] array : list) {
					AggregateResult ar = new AggregateResult();
					ar.setCount(((Number) array[0]).intValue());
					ar.setPrincipal(array[1]);
					tags.add(ar);
				}
				return null;
			}
		});
		return tags;
	}

	// for auth
	private DetachedCriteria prepareDetachedCriteria() {
		DetachedCriteria dc = productManager.detachedCriteria();
		dc.setFetchMode("category", FetchMode.JOIN);
		dc.setFetchMode("attributes", FetchMode.JOIN);
		dc.setFetchMode("tags", FetchMode.JOIN);
		return dc;
	}

	@Override
	public List<Product> getRelatedProducts(final Product product) {
		int count = 5;
		List<Product> list = new ArrayList<Product>(count);
		final int maxResults = count - list.size();
		if (product.getTags().size() > 0) {
			list.addAll((List<Product>) productManager
					.executeQuery(new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							List<SimpleElement> tags = product.getTags();
							String[] ar = new String[tags.size()];
							for (int i = 0; i < ar.length; i++)
								ar[i] = "?";
							String hql = "from Product p join p.tags tag where tag.value in ("
									+ StringUtils.join(ar, ",")
									+ ") and p.code<>? order by p.displayOrder,p.modifyDate desc";

							Query q = session.createQuery(hql.toString());
							q.setParameter(0, Arrays.asList(product
									.getTagsAsString().split(",")));
							for (int i = 0; i < tags.size(); i++)
								q.setString(i, tags.get(i).getValue());
							q.setParameter(tags.size() + 0, true);
							q.setParameter(tags.size() + 1, product.getCode());
							q.setMaxResults(maxResults);
							return q.list();

						}
					}));
		}
		return list;
	}
}
