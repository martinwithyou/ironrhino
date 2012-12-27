package com.ironrhino.online.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.common.model.Addressee;
import org.ironrhino.core.session.SessionCompressor;
import org.ironrhino.core.util.JsonUtils;

import com.ironrhino.online.model.OrderItem;
import com.ironrhino.online.service.ProductFacade;

@Singleton
@Named
public class CartSessionCompressor implements SessionCompressor<Cart> {

	private static final String SEPARATOR = "|**|";

	private Log log = LogFactory.getLog(getClass());

	@Inject
	private ProductFacade productFacade;

	@Override
	public boolean supportsKey(String key) {
		return Cart.SESSION_KEY_CART.equals(key);
	}

	@Override
	public String compress(Cart cart) {
		if (cart == null)
			return null;
		List<OrderItem> items = cart.getOrder().getItems();
		if (items == null || items.isEmpty())
			return null;
		Map<String, Integer> map = new LinkedHashMap<String, Integer>(items.size());
		for (OrderItem oi : items)
			map.put(oi.getProductCode(), oi.getQuantity());
		Addressee add = cart.getOrder().getAddressee();
		if (add == null) {
			return JsonUtils.toJson(map);
		} else {
			return new StringBuilder().append(JsonUtils.toJson(map)).append(
					SEPARATOR).append(JsonUtils.toJson(add)).toString();
		}

	}

	@Override
	public Cart uncompress(String str) {
		Cart cart = new Cart();
		if (StringUtils.isNotBlank(str)) {
			String add = null;
			int index = str.indexOf(SEPARATOR);
			if (index > 0) {
				add = str.substring(index + SEPARATOR.length());
				str = str.substring(0, index);
			}
			try {
				Map<String, Integer> map = JsonUtils.fromJson(str,
						new TypeReference<Map<String, Integer>>() {
						});
				for (Map.Entry<String, Integer> entry : map.entrySet())
					cart.put(productFacade.getProductByCode(entry.getKey()),
							entry.getValue());
				if (add != null)
					cart.getOrder().setAddressee(
							JsonUtils.fromJson(add, Addressee.class));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return cart;
	}
}