package com.ironrhino.online.payment;

import java.util.Collections;
import java.util.List;

public class PaymentManager {

	private List<Payment> payments = Collections.EMPTY_LIST;

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	public Payment getPayment(String code) {
		for (Payment pm : payments)
			if (pm.getCode().equalsIgnoreCase(code))
				return pm;
		return null;
	}
}
