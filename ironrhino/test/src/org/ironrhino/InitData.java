package org.ironrhino;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.model.Setting;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.service.AccountManager;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.online.servlet.StaticResourceFilter;
import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.model.Product;
import org.ironrhino.pms.model.ProductStatus;
import org.ironrhino.pms.service.CategoryManager;
import org.ironrhino.pms.service.ProductManager;
import org.ironrhino.ums.model.Role;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.RoleManager;
import org.ironrhino.ums.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitData {

	public static void main(String... strings) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {
						"resources/spring/applicationContext-base.xml",
						"resources/spring/applicationContext-hibernate.xml",
						"resources/spring/applicationContext-service-ums.xml",
						"resources/spring/applicationContext-service-pms.xml",
						"resources/spring/applicationContext-service-online.xml",
						"resources/spring/applicationContext-aop.xml" });

		UserManager userManager = (UserManager) ctx.getBean("userManager");
		RoleManager roleManager = (RoleManager) ctx.getBean("roleManager");
		Role supervisorRole = new Role("ROLE_SUPERVISOR");
		Role advancedRole = new Role("ROLE_ADVANCED");
		roleManager.save(supervisorRole);
		roleManager.save(advancedRole);
		User test = new User();
		test.setUsername("test");
		test.setLegiblePassword("test");
		userManager.save(test);
		User admin = new User();
		admin.setUsername("admin");
		admin.setLegiblePassword("password");
		admin.getRoles().add(new SimpleElement("ROLE_SUPERVISOR"));
		userManager.save(admin);

		AccountManager accountManager = (AccountManager) ctx
				.getBean("accountManager");
		Account acc = new Account();
		acc.setUsername("test");
		acc.setLegiblePassword("password");
		acc.setEmail("www@google.com");
		acc.setEnabled(true);
		acc.getRoles().add(new SimpleElement("ROLE_ADVANCED"));
		accountManager.save(acc);
		acc = new Account();
		acc.setUsername("test2");
		acc.setLegiblePassword("password");
		acc.setEmail("www2@google.com");
		acc.setEnabled(true);
		accountManager.save(acc);

		CategoryManager categoryManager = (CategoryManager) ctx
				.getBean("categoryManager");
		ProductManager productManager = (ProductManager) ctx
				.getBean("productManager");
		Category[] categories = new Category[10];
		List<String> recommendedProducts = new ArrayList<String>();
		for (int i = 0; i < categories.length; i++) {
			categories[i] = new Category();
			categories[i].setName("category" + i);
			if ((i + 1) % 2 == 0)
				categories[i].setParent(categories[(i - 1) / 2]);
			if (i == 0) {
				SimpleElement cr = new SimpleElement("ROLE_ADVANCED");
				categories[i].getRoles().add(cr);
			}
			categoryManager.save(categories[i]);
			for (int j = 0; j < 10; j++) {
				Product p = new Product();
				int id = (i + 100) * (j + 1);
				p.setCode("productCode" + id);
				p.setName("productName" + id);
				if (j % 2 == 0)
					p.setPrice(new BigDecimal(11.00));
				else
					p.setPrice(new BigDecimal(0.01));
				p.setStatus(ProductStatus.ACTIVE);
				p.setCategory(categories[i]);
				if ((j & 1) == 1) {
					p.setReleased(true);
					p.setReleaseDate(new Date());
					if (j % 4 == 1) {
						p.setNewArrival(true);
						p.setNewArrivalTimeLimit(DateUtils.addDays(new Date(),
								14));
					}
					if (j == 1) {
						SimpleElement pr = new SimpleElement("ROLE_ADVANCED");
						p.getRoles().add(pr);
					}
					if (recommendedProducts.size() < 4
							&& p.getRoles().size() == 0)
						recommendedProducts.add(p.getCode());
				}
				productManager.save(p);
			}
		}

		BaseManager baseManager = (BaseManager) ctx.getBean("baseManager");
		baseManager.save(new Setting(
				ProductFacade.SETTING_KEY_RECOMMENDED_PRODUCT, StringUtils
						.join(recommendedProducts, ",")));
		baseManager.save(new Setting("product.defaultNewArrivalDays", "14"));
		baseManager.save(new Setting(
				StaticResourceFilter.SETTING_KEY_USESTATICPAGE, "true"));

		Region[] regions = new Region[10];
		for (int i = 0; i < regions.length; i++) {
			regions[i] = new Region();
			regions[i].setName("region" + i);
			if ((i + 1) % 2 == 0)
				regions[i].setParent(regions[(i - 1) / 2]);
			baseManager.save(regions[i]);
		}

	}
}
