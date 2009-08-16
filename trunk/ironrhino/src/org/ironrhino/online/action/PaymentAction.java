package org.ironrhino.online.action;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.online.model.Order;
import org.ironrhino.online.model.OrderStatus;
import org.ironrhino.online.payment.Paypal;
import org.ironrhino.online.service.OrderManager;

@AutoConfig(namespace = "/")
public class PaymentAction extends BaseAction {

	public static final Log log = LogFactory.getLog(PaymentAction.class);

	private transient OrderManager orderManager;

	private transient Paypal paypal;

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public void setPaypal(Paypal paypal) {
		this.paypal = paypal;
	}

	public String paypal() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		String orderCode = request.getParameter("item_name");

		Order order = orderManager.getByNaturalId("code", orderCode);
		if (order == null) {
			log.error("no order code:" + orderCode);
			return NONE;
		}

		Enumeration en = request.getParameterNames();
		String str = "cmd=_notify-validate";
		while (en.hasMoreElements()) {
			String paramName = (String) en.nextElement();
			String paramValue = request.getParameter(paramName);
			str = str + "&" + paramName + "="
					+ URLEncoder.encode(paramValue, "utf-8");
		}

		URL u = new URL("http://www.paypal.com/cgi-bin/webscr");
		URLConnection uc = u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		PrintWriter pw = new PrintWriter(uc.getOutputStream());
		pw.println(str);
		pw.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(uc
				.getInputStream()));
		String res = in.readLine();
		in.close();

		String paymentStatus = request.getParameter("payment_status");
		String paymentAmount = request.getParameter("mc_gross");
		String paymentCurrency = request.getParameter("mc_currency");
		String receiverEmail = request.getParameter("receiver_email");
		if ("VERIFIED".equals(res)) {
			if (!"Completed".equals(paymentStatus)) {
				log.error("payment not completed,orderCode=" + orderCode);
				return NONE;
			}
			if (!paypal.getBusiness().equals(receiverEmail)) {
				log.error("receiver email is not bussiness,orderCode="
						+ orderCode);
				return NONE;
			}
			if (!paypal.getCurrency().equals(paymentCurrency)
					|| !NumberUtils.format(order.getGrandtotal().doubleValue(),
							2).equals(paymentAmount)) {
				log.error("amount or currency is not correct,orderCode="
						+ orderCode);
				return NONE;
			}
			paid(order);
		} else if (res.equals("INVALID")) {
			log.error("IPN is invalid,orderCode=" + orderCode);
		} else {
			log.error("IPN check is error,orderCode=" + orderCode);
		}
		return NONE;
	}

	private void paid(Order order) {
		order.setStatus(OrderStatus.PAID);
		order.setPayDate(new Date());
		orderManager.save(order);
		// TODO send mail
	}
}
