package com.ironrhino.online.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;
import com.ironrhino.online.model.OrderItem;
import com.ironrhino.online.service.ProductFacade;
import com.ironrhino.online.support.Cart;
import com.opensymphony.xwork2.util.CreateIfNull;

@AutoConfig(namespace = "/")
public class CartAction extends BaseAction {

	private static final long serialVersionUID = 2090481459995236536L;

	private static final String SESSION_KEY_CART = "SESSION_KEY_CART";

	@Autowired
	private transient ProductFacade productFacade;

	// quantity
	private int quantity = 1;

	private List<OrderItem> items;

	private Cart cart;

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	@CreateIfNull
	public List<OrderItem> getItems() {
		return items;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Cart getCart() {
		if (cart == null)
			cart = uncompress((String) ServletActionContext.getRequest()
					.getSession().getAttribute(SESSION_KEY_CART));
		return cart;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	public String facade() {
		getCart();
		return "facade";
	}

	@SkipValidation
	public String add() {
		String code = getUid();
		if (!getCart().contains(code)
				&& getCart().getOrder().getItems().size() >= 10) {
			addActionError(getText("add.cart.full",
					"your cart is full,max items is {0}", new String[] { String
							.valueOf(getCart().getOrder().getItems().size()) }));
		} else {
			getCart().put(productFacade.getProductByCode(code), quantity);
		}
		markSessionDirty();
		return "facade";
	}

	@SkipValidation
	public String subtract() {
		String code = getUid();
		if (getCart().contains(code)) {
			getCart().update(code, getCart().getQuantity(code) - 1);
		}
		markSessionDirty();
		return "facade";
	}

	@SkipValidation
	public String remove() {
		String[] codeArray = getId();
		if (codeArray != null && codeArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (String c : codeArray) {
				sb.append(getCart().getProductName(c));
				getCart().remove(c);
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			markSessionDirty();
			addActionMessage(getText("operate.success"));
		}
		return SUCCESS;
	}

	@Override
	@SkipValidation
	public String update() {
		if (items != null && items.size() != 0) {
			for (OrderItem item : items)
				getCart().update(item.getProductCode(), item.getQuantity());
		}
		markSessionDirty();
		addActionMessage(getText("operate.success"));
		return SUCCESS;
	}

	@SkipValidation
	public String clear() {
		getCart().clear();
		markSessionDirty();
		addActionMessage(getText("operate.success"));
		return SUCCESS;
	}

	private void markSessionDirty() {
		if (cart != null)
			ServletActionContext.getRequest().getSession().setAttribute(
					SESSION_KEY_CART, compress(cart));
	}

	private String compress(Cart cart) {
		if (cart == null)
			return null;
		List<OrderItem> items = cart.getOrder().getItems();
		if (items == null || items.isEmpty())
			return null;
		Map<String, Integer> map = new HashMap<String, Integer>(items.size());
		for (OrderItem oi : items)
			map.put(oi.getProductCode(), oi.getQuantity());
		return JsonUtils.toJson(map);
	}

	private Cart uncompress(String str) {
		Cart cart = new Cart();
		if (StringUtils.isNotBlank(str)) {
			Map<String, Integer> map = JsonUtils.fromJson(str,
					new TypeToken<Map<String, Integer>>() {
					}.getType());
			for (Map.Entry<String, Integer> entry : map.entrySet())
				cart.put(productFacade.getProductByCode(entry.getKey()), entry
						.getValue());
		}
		return cart;
	}

}
