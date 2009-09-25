package org.ironrhino.online.action;

import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.online.model.OrderItem;
import org.ironrhino.online.support.Cart;

import com.opensymphony.xwork2.util.CreateIfNull;

@AutoConfig(namespace = "/")
public class CartAction extends BaseAction {

	private static final long serialVersionUID = 2090481459995236536L;

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

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public Cart getCart() {
		return cart;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	public String facade() {
		return "facade";
	}

	@SkipValidation
	public String add() {
		String code = getUid();
		if (!cart.contains(code) && cart.getOrder().getItems().size() >= 10) {
			addActionError(getText("add.cart.full",
					"your cart is full,max items is {0}", new String[] { String
							.valueOf(cart.getOrder().getItems().size()) }));
		} else {
			cart.put(code, quantity);
		}
		return "facade";
	}

	@SkipValidation
	public String subtract() {
		String code = getUid();
		if (cart.contains(code)) {
			cart.update(code, cart.getQuantity(code) - 1);
		}
		return "facade";
	}

	@SkipValidation
	public String remove() {
		String[] codeArray = getId();
		if (codeArray != null && codeArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (String c : codeArray) {
				sb.append(cart.getProductName(c));
				cart.remove(c);
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			addActionMessage(getText("operation.success"));

		}
		return SUCCESS;
	}

	@Override
	@SkipValidation
	public String update() {
		if (items != null && items.size() != 0) {
			for (OrderItem item : items)
				cart.update(item.getProductCode(), item.getQuantity());
		}
		addActionMessage(getText("operation.success"));
		return SUCCESS;
	}

	@SkipValidation
	public String clear() {
		cart.clear();
		addActionMessage(getText("operation.success"));
		return SUCCESS;
	}

}
