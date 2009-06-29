package org.ironrhino.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

public class ResultPage<T> implements Serializable {

	public static final int MAX_RECORDS_PER_PAGE = 1000;

	public static final int DEFAULT_PAGE_SIZE = 10;

	public static final String PAGENO_PARAM_NAME = "pn";

	public static final String PAGESIZE_PARAM_NAME = "ps";

	private int pageNo = 1;

	private int pageSize = DEFAULT_PAGE_SIZE;

	private int totalPage = -1;

	private int totalRecord = -1;

	private Collection<T> result = new ArrayList<T>();

	private DetachedCriteria detachedCriteria;

	private Set<Order> orders = new LinkedHashSet<Order>();

	private boolean reverse;

	private int start = -1;

	public int getStart() {
		return this.start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public int getPageNo() {
		if (start >= 0)
			return start / pageSize + 1;
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Collection<T> getResult() {
		return result;
	}

	public void setResult(Collection<T> result) {
		this.result = result;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public DetachedCriteria getDetachedCriteria() {
		return detachedCriteria;
	}

	public void setDetachedCriteria(DetachedCriteria detachedCriteria) {
		this.detachedCriteria = detachedCriteria;
	}

	public Set<Order> getOrders() {
		return orders;
	}

	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}

	public void addOrder(Order order) {
		this.orders.add(order);
	}

	public void removeOrder(Order order) {
		this.orders.remove(order);
	}

	public boolean isFirst() {
		return this.pageNo <= 1;
	}

	public boolean isLast() {
		return this.pageNo >= this.totalPage;
	}

	public int getPreviousPage() {
		return this.pageNo > 1 ? this.pageNo - 1 : 1;
	}

	public int getNextPage() {
		return this.pageNo < this.totalPage ? this.pageNo + 1 : this.totalPage;
	}

	public boolean isDefaultPageSize() {
		return this.pageSize == DEFAULT_PAGE_SIZE;
	}

	public String renderUrl(int pn) {
		String requestURI = (String) ServletActionContext.getRequest()
				.getAttribute("struts.request_uri");
		if (requestURI == null) {
			requestURI = (String) ServletActionContext.getRequest()
					.getAttribute("javax.servlet.forward.request_uri");
		}
		if (requestURI == null) {
			requestURI = ServletActionContext.getRequest().getRequestURI();
		}
		if (isDefaultPageSize()) {
			if (pn <= 1)
				return requestURI;
			else
				return requestURI + "?" + PAGENO_PARAM_NAME + "=" + pn;
		} else {
			if (pn <= 1)
				return requestURI + "?" + PAGESIZE_PARAM_NAME + "=" + pageSize;
			else
				return requestURI + "?" + PAGENO_PARAM_NAME + "=" + pn + "&"
						+ PAGESIZE_PARAM_NAME + "=" + pageSize;
		}
	}

}
