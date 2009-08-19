package org.ironrhino.online.action;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.pms.model.Product;

@AutoConfig(namespace = "/")
public class RightAction extends BaseAction {

	private static final long serialVersionUID = -6441264436887308502L;

	private transient ProductFacade productFacade;

	public Product getRelatedProduct() {
		String history = RequestUtils.getCookieValue(ServletActionContext
				.getRequest(), "HISTORY");
		Product relatedProduct = null;
		if (StringUtils.isNotBlank(history)) {
			String[] array = history.split(",");
			Random random = new Random();
			int index = random.nextInt(array.length);
			String code = array[index];
			relatedProduct = productFacade.getProductByCode(code);
			if (relatedProduct != null) {
				List<Product> relatedProducts = productFacade
						.getRelatedProducts(relatedProduct);
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
		return relatedProduct;
	}

	public ProductFacade getProductFacade() {
		return productFacade;
	}

	public void setProductFacade(ProductFacade productFacade) {
		this.productFacade = productFacade;
	}

	@Override
	public String execute() {

		return SUCCESS;
	}
}
