package com.ironrhino.online.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.ironrhino.common.model.ResultPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ironrhino.online.service.ProductFacade;
import com.ironrhino.pms.model.Category;
import com.ironrhino.pms.model.Product;
import com.ironrhino.pms.service.CategoryManager;
import com.ironrhino.pms.service.ProductManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"resources/spring/applicationContext-common.xml",
		"resources/spring/applicationContext-datasource.xml",
		"resources/spring/applicationContext-hibernate.xml",
		"resources/spring/applicationContext-online.xml",
		"resources/spring/applicationContext-cache.xml" })
public class ProductFacadeTest {

	Product[] products = new Product[10];

	Category category;

	@Autowired
	CategoryManager categoryManager;

	@Autowired
	ProductManager productManager;

	@Autowired
	ProductFacade productFacade;

	@Before
	public void doSetUp() {
		category = new Category();
		category.setCode("test");
		category.setName("test");
		categoryManager.save(category);
		for (int i = 0; i < products.length; i++) {
			products[i] = new Product();
			products[i].setCode("productCode" + i);
			products[i].setName("productName" + i);
			products[i].setDisplayOrder(i - 100);
			products[i].setCategory(category);
			productManager.save(products[i]);
		}
	}

	@After
	public void doTearDown() {
		for (int i = 0; i < products.length; i++)
			productManager.delete(products[i]);
		categoryManager.delete(category);
	}

	@Test
	public void testGetNewArrivalProducts() {
		assertEquals(products[4], productFacade.getNewArrivalProducts(1).get(0));
		SecurityContext sc = new SecurityContextImpl();
		Authentication auth = new UsernamePasswordAuthenticationToken(
				new Object(), new Object(),
				new GrantedAuthorityImpl[] { new GrantedAuthorityImpl(
						"ROLE_TEST") });
		sc.setAuthentication(auth);
		SecurityContextHolder.setContext(sc);
		assertEquals(products[0], productFacade.getNewArrivalProducts(1).get(0));
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void testGetResultPageByCategoryId() {
		ResultPage<Product> rp = new ResultPage<Product>();
		assertEquals(3, productFacade.getResultPageByCategoryId(rp,
				category.getId()).getResult().size());
		SecurityContext sc = new SecurityContextImpl();
		Authentication auth = new UsernamePasswordAuthenticationToken(
				new Object(), new Object(),
				new GrantedAuthorityImpl[] { new GrantedAuthorityImpl(
						"ROLE_TEST") });
		sc.setAuthentication(auth);
		SecurityContextHolder.setContext(sc);
		assertEquals(5, productFacade.getResultPageByCategoryId(rp,
				category.getId()).getResult().size());
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void testGetProductByCode() {
		assertNotNull(productFacade.getProductByCode(products[0].getCode()));
		assertEquals(products[2], productFacade.getProductByCode(products[2]
				.getCode()));
		SecurityContext sc = new SecurityContextImpl();
		Authentication auth = new UsernamePasswordAuthenticationToken(
				new Object(), new Object(),
				new GrantedAuthorityImpl[] { new GrantedAuthorityImpl(
						"ROLE_TEST") });
		sc.setAuthentication(auth);
		SecurityContextHolder.setContext(sc);
		assertEquals(products[0], productFacade.getProductByCode(products[0]
				.getCode()));
		SecurityContextHolder.getContext().setAuthentication(null);
	}

}