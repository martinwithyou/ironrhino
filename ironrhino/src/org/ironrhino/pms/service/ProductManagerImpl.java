package org.ironrhino.pms.service;

import java.util.Date;

import org.ironrhino.core.annotation.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.pms.model.Product;
import org.springframework.transaction.annotation.Transactional;

public class ProductManagerImpl extends BaseManagerImpl<Product> implements
		ProductManager {

	@Transactional(readOnly = false)
	@FlushCache("product_${args[0].code}")
	public void save(Product product) {
		product.setOpen(null);// rejudge if it's open
		super.save(product);
	}

	@Transactional(readOnly = false)
	public void updateNewArrival() {
		String hql = "update Product p set p.newArrival = ? where p.newArrival = ? and p.newArrivalTimeLimit <= ?";
		bulkUpdate(hql, new Object[] { false, true, new Date() });
	}

}
