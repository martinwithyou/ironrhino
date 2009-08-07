package org.ironrhino.online.service;

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
import org.hibernate.type.Type;
import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.annotation.NativeSql;
import org.ironrhino.core.cache.CheckCache;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.action.ProductAction;
import org.ironrhino.online.model.ProductScore;
import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.model.Product;
import org.ironrhino.pms.service.ProductManager;
import org.ironrhino.pms.support.CategoryTreeControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

public class ProductFacadeImpl implements ProductFacade {

	protected final Log log = LogFactory.getLog(ProductFacadeImpl.class);

	@Autowired
	private ProductManager productManager;

	@Autowired
	private BaseManager baseManager;

	@Autowired
	private SettingControl settingControl;

	@Autowired
	private CategoryTreeControl categoryTreeControl;

	private Random random;

	@PostConstruct
	public void afterPropertiesSet() {
		random = new Random();
	}

	public List<Product> getNewArrivalProducts(int... parameters) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.add(Restrictions.eq("newArrival", true));
		dc.addOrder(Order.asc("displayOrder"));
		dc.addOrder(Order.desc("releaseDate"));
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
			dc.add(Restrictions.between("releaseDate", DateUtils.addDays(date,
					-beforeDays), date));
		}
		return productManager.getListByCriteria(dc, 1, maxResults);
	}

	public List<Product> getProducts(int max) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.addOrder(Order.asc("displayOrder"));
		dc.addOrder(Order.desc("releaseDate"));
		return productManager.getListByCriteria(dc, 1, max);
	}

	public List<Product> getProducts(String[] codeArray) {
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		dc.setFetchMode("category", FetchMode.JOIN);
		dc.setFetchMode("attributes", FetchMode.JOIN);
		dc.setFetchMode("roles", FetchMode.JOIN);
		dc.setFetchMode("relatedProducts", FetchMode.JOIN);
		dc.add(Restrictions.in("code", codeArray));
		List<String> roleNames = AuthzUtils.getRoleNames();
		if (roleNames.size() > 0) {
			dc.add(Restrictions.eq("released", true));
			Type[] typeArray = new Type[roleNames.size()];
			String[] array = new String[roleNames.size()];
			for (int i = 0; i < roleNames.size(); i++) {
				array[i] = "?";
				typeArray[i] = Hibernate.STRING;
			}
			String sql = "{alias}.id in (select p.id from pms_product p left join pms_productRole pr on p.id = pr.productId where pr.value is null or pr.value in ("
					+ StringUtils.join(array, ',') + "))";
			dc.add(Restrictions.sqlRestriction(sql, roleNames.toArray(),
					typeArray));
		} else {
			dc.add(Restrictions.eq("open", true));
		}
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
		resultPage.addOrder(Order.desc("releaseDate"));
		resultPage = productManager.getResultPage(resultPage);
		return resultPage;
	}

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
		resultPage.addOrder(Order.desc("releaseDate"));
		resultPage = productManager.getResultPage(resultPage);
		return resultPage;
	}

	@CheckCache(key = "product_${args[0]}")
	public Product getProductByCode(String code) {

		DetachedCriteria dc = productManager.detachedCriteria();
		dc.setFetchMode("attributes", FetchMode.JOIN);
		dc.setFetchMode("roles", FetchMode.JOIN);
		dc.setFetchMode("relatedProducts", FetchMode.JOIN);
		dc.setFetchMode("category", FetchMode.JOIN);
		dc.add(Restrictions.naturalId().set("code", code));
		dc.add(Restrictions.eq("released", true));
		Product product = productManager.getByCriteria(dc);
		if (product == null)
			return null;
		evaluateRelatedProducts(product);
		return product;
	}

	private void evaluateRelatedProducts(final Product product) {
		int count = 5;
		List<Product> list = new ArrayList<Product>(count);
		list.addAll(product.getRelatedProducts());
		final int maxResults = count - list.size();
		if (product.getTags().size() > 0) {
			list.addAll((List<Product>) productManager
					.execute(new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							List<SimpleElement> tags = product.getTags();
							String[] ar = new String[tags.size()];
							for (int i = 0; i < ar.length; i++)
								ar[i] = "?";
							String hql = "from Product p join p.tags tag where tag.value in ("
									+ StringUtils.join(ar, ",")
									+ ") and p.open=? and p.code<>? order by p.displayOrder,p.releaseDate desc";

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
		product.setRelatedProducts(list);
	}

	public Product getRandomProduct() {
		int count = productManager.countByCriteria(prepareDetachedCriteria());
		if (count == 0)
			return new Product();
		int rand = random.nextInt(count);
		DetachedCriteria dc = prepareDetachedCriteria();
		dc.setFetchMode("attributes", FetchMode.JOIN);
		dc.setFetchMode("roles", FetchMode.JOIN);
		dc.setFetchMode("relatedProducts", FetchMode.JOIN);
		dc.setFetchMode("category", FetchMode.JOIN);
		List<Product> list = productManager.getBetweenListByCriteria(dc, rand,
				rand + 1);
		Product product = list.get(0);
		return product;
	}

	public AggregateResult getScoreResult(final String productCode) {
		baseManager.setEntityClass(ProductScore.class);
		final DetachedCriteria dc = baseManager.detachedCriteria();
		dc.setProjection(Projections.projectionList().add(
				Projections.count("score")).add(Projections.avg("score")));
		dc.add(Restrictions.eq("productCode", productCode));
		Object[] array = (Object[]) baseManager
				.execute(new HibernateCallback() {
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
					.execute(new HibernateCallback() {
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

	public List<AggregateResult> getTopScoreProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select ps.productCode,count(ps.score),avg(ps.score) from ProductScore ps,Product p where ps.productCode=p.code and p.open=true group by ps.productCode order by avg(ps.score) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal((String) array[0]);
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

	public List<AggregateResult> getTopFavoriteProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select pf.productCode,count(pf.username) from ProductFavorite pf,Product p where pf.productCode=p.code and p.open=true group by pf.productCode order by count(pf.username) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal((String) array[0]);
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

	public List<AggregateResult> getTopSendProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select ps.productCode,count(ps.destination) from ProductSend ps,Product p where ps.productCode=p.code and p.open=true group by ps.productCode order by count(ps.destination) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal((String) array[0]);
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

	public List<AggregateResult> getTopSaleProducts(final int maxResults) {
		List<AggregateResult> list;
		list = (List<AggregateResult>) baseManager
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query q = session
								.createQuery("select oi.productCode,sum(oi.quantity) from Product p,Order o right join o.items oi where o.status in('PAID','SHIPPED','COMPLETED') and oi.productCode=p.code and p.open=true  group by oi.productCode order by sum(oi.quantity) desc");
						q.setMaxResults(maxResults);
						List<AggregateResult> list = new ArrayList<AggregateResult>();
						List<Object[]> result = q.list();
						for (Object[] array : result) {
							AggregateResult sr = new AggregateResult();
							sr.setPrincipal((String) array[0]);
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

	public List<AggregateResult> getTags(final String... prefix) {
		final List<AggregateResult> tags = new ArrayList<AggregateResult>();
		baseManager.execute(new HibernateCallback() {
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
					ar.setPrincipal((String) array[1]);
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
		List<String> roleNames = AuthzUtils.getRoleNames();
		if (roleNames.size() > 0) {
			dc.add(Restrictions.eq("released", true));
			// this cannot query by Criteria directly,this is hibernate bug,we
			// could use hql,but we need ResultPage so use Criteria and sql
			Type[] typeArray = new Type[roleNames.size()];
			String[] array = new String[roleNames.size()];
			for (int i = 0; i < roleNames.size(); i++) {
				array[i] = "?";
				typeArray[i] = Hibernate.STRING;
			}
			@NativeSql
			String sql = "{alias}.id in (select p.id from pms_product p left join pms_productRole pr on p.id = pr.productId where pr.value is null or pr.value in ("
					+ StringUtils.join(array, ',') + "))";
			dc.add(Restrictions.sqlRestriction(sql, roleNames.toArray(),
					typeArray));
		} else {
			dc.add(Restrictions.eq("open", true));
		}
		return dc;
	}

}
