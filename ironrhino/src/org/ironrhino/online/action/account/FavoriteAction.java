package org.ironrhino.online.action.account;

import java.util.List;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.ProductFavorite;

@AutoConfig(namespace = "/account")
public class FavoriteAction extends BaseAction {

	private ResultPage<ProductFavorite> resultPage;

	private BaseManager<ProductFavorite> baseManager;

	public ResultPage<ProductFavorite> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<ProductFavorite> resultPage) {
		this.resultPage = resultPage;
	}

	public void setBaseManager(BaseManager<ProductFavorite> baseManager) {
		this.baseManager = baseManager;
		this.baseManager.setEntityClass(ProductFavorite.class);
	}

	@SkipValidation
	public String execute() {
		if (resultPage == null)
			resultPage = new ResultPage<ProductFavorite>();
		DetachedCriteria dc = baseManager.detachedCriteria();
		dc.add(Restrictions.eq("username", AuthzUtils.getUsername()));
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.desc("addDate"));
		resultPage = baseManager.getResultPage(resultPage);
		return SUCCESS;
	}

	@SkipValidation
	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.eq("username", AuthzUtils.getUsername()));
			dc.add(Restrictions.in("id", id));
			List<ProductFavorite> list = baseManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (ProductFavorite pf : list) {
					baseManager.delete(pf);
					sb.append(pf.getProductName() + ",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				addActionMessage(getText("delete.success",
						"delete {0} successfully",
						new String[] { sb.toString() }));
			}
		}
		return SUCCESS;
	}

}
