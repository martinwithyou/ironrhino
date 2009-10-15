package org.ironrhino;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ironrhino.common.model.Region;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ironrhino.pms.model.Category;
import com.ironrhino.pms.model.Product;
import com.ironrhino.pms.model.ProductStatus;
import com.ironrhino.pms.service.CategoryManager;
import com.ironrhino.pms.service.ProductManager;

public class InitData {

	public static void main(String... strings) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "applicationContext-common.xml",
						"resources/spring/applicationContext-datasource.xml",
						"resources/spring/applicationContext-hibernate.xml" });
		UserManager userManager = (UserManager) ctx.getBean("userManager");
		User test = new User();
		test.setUsername("test");
		test.setLegiblePassword("test");
		test.setEmail("test@test.com");
		test.setEnabled(true);
		userManager.save(test);
		User admin = new User();
		admin.setUsername("admin");
		admin.setLegiblePassword("password");
		admin.setEmail("admin@test.com");
		admin.setEnabled(true);
		admin.getRoles().add(new SimpleElement("ROLE_SUPERVISOR"));
		userManager.save(admin);

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
					if (recommendedProducts.size() < 4)
						recommendedProducts.add(p.getCode());
				}
				productManager.save(p);
			}
		}

		BaseManager baseManager = (BaseManager) ctx.getBean("baseManager");

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
