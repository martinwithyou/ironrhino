package org.ironrhino.pms.service;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.pms.model.Product;

public interface ProductManager extends BaseManager<Product> {

	public void save(Product product);

	public void delete(Product product);

}
