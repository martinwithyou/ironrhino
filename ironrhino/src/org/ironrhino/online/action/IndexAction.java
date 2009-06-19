package org.ironrhino.online.action;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.pms.model.Product;
import org.ironrhino.pms.support.CategoryTreeControl;

public class IndexAction extends BaseAction {

	private ProductFacade productFacade;

	private transient CategoryTreeControl categoryTreeControl;

	private Product relatedProduct;

	public Product getRelatedProduct() {
		return relatedProduct;
	}

	public CategoryTreeControl getCategoryTreeControl() {
		return categoryTreeControl;
	}

	public void setCategoryTreeControl(CategoryTreeControl categoryTreeControl) {
		this.categoryTreeControl = categoryTreeControl;
	}

	public ProductFacade getProductFacade() {
		return productFacade;
	}

	public void setProductFacade(ProductFacade productFacade) {
		this.productFacade = productFacade;
	}

	public String execute() {
		return SUCCESS;
	}

	public String left() {
		return SUCCESS;
	}

	public String right() {
		String history = RequestUtils.getCookieValue(ServletActionContext
				.getRequest(), "HISTORY");
		if (StringUtils.isNotBlank(history)) {
			String[] array = history.split(",");
			Random random = new Random();
			int index = random.nextInt(array.length);
			String code = array[index];
			relatedProduct = productFacade.getProductByCode(code);
			if (relatedProduct != null) {
				Collection<Product> relatedProducts = relatedProduct
						.getRelatedProducts();
				if (relatedProducts.size() > 0) {
					index = random.nextInt(relatedProducts.size());
					Iterator<Product> it = relatedProducts.iterator();
					for (int i = 0; i <= index; i++)
						relatedProduct = it.next();
				}
			}
		}
		if (relatedProduct == null)
			relatedProduct = productFacade.getRandomProduct();
		return SUCCESS;
	}
}
