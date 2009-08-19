package org.ironrhino.pms.service;

import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.pms.model.Product;
import org.springframework.transaction.annotation.Transactional;

public class ProductManagerImpl extends BaseManagerImpl<Product> implements
		ProductManager {

	@Override
	@Transactional(readOnly = false)
	@FlushCache(key = "${args[0].code}", namespace = "product")
	public void save(Product product) {
		super.save(product);
	}

}
