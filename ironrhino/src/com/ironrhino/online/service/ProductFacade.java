package com.ironrhino.online.service;

import java.util.List;

import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.model.ResultPage;

import com.ironrhino.pms.model.Product;

public interface ProductFacade {

	public static final String SETTING_KEY_RECOMMENDED_PRODUCT = "product.recommanded";

	public List<Product> getNewArrivalProducts(int... parameters);

	public List<Product> getProducts(int max);

	public List<Product> getProducts(String[] codeArray);

	public List<Product> getRecommendedProducts();

	public ResultPage getResultPageByCategoryId(ResultPage<Product> resultPage,
			Long categoryId);

	public ResultPage getResultPageByCategoryCode(
			ResultPage<Product> resultPage, String categoryCode);

	public ResultPage getResultPageByTag(ResultPage<Product> resultPage,
			String tag);

	public Product getRandomProduct();

	public Product getProductByCode(String code);

	public AggregateResult getScoreResult(String productCode);

	public List<AggregateResult> getTopScoreProducts(int maxResults);

	public List<AggregateResult> getTopFavoriteProducts(int maxResults);

	public List<AggregateResult> getTopSaleProducts(int maxResults);

	public List<AggregateResult> getTags(String... prefix);

	public List<Product> getRelatedProducts(Product product);

}