package org.ironrhino.pms.service;

import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.pms.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("productManager")
public class ProductManagerImpl extends BaseManagerImpl<Product> implements
		ProductManager {

	@Override
	@Transactional
	@FlushCache(key = "${args[0].code}", namespace = "product")
	public void save(Product product) {
		super.save(product);
	}

	@Override
	@Transactional
	@FlushCache(key = "${args[0].code}", namespace = "product")
	public void delete(Product product) {
		super.delete(product);
	}

}
