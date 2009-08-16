package org.ironrhino.online.action.backend;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.service.AccountManager;

@AutoConfig
public class AccountAction extends BaseAction {

	private Account account;

	private ResultPage<Account> resultPage;

	private String rolesAsString;

	private String groupsAsString;

	private String password;

	private AccountManager accountManager;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGroupsAsString() {
		return groupsAsString;
	}

	public void setGroupsAsString(String groupsAsString) {
		this.groupsAsString = groupsAsString;
	}

	public String getRolesAsString() {
		return rolesAsString;
	}

	public void setRolesAsString(String rolesAsString) {
		this.rolesAsString = rolesAsString;
	}

	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public ResultPage<Account> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Account> resultPage) {
		this.resultPage = resultPage;
	}

	public String execute() {
		DetachedCriteria dc = accountManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage<Account>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("username"));
		resultPage = accountManager.getResultPage(resultPage);
		return LIST;
	}

	public String view() {
		account = accountManager.get(getUid());
		return VIEW;
	}

	public String save() {
		if (account != null && account.getId() != null) {
			Account temp = account;
			account = accountManager.get(temp.getId());
			if (account != null) {
				account.setEnabled(temp.isEnabled());
				account.setLocked(temp.isLocked());
				if (StringUtils.isNotBlank(password))
					account.setLegiblePassword(password);
				if (rolesAsString != null)
					account.setRolesAsString(rolesAsString);
				if (groupsAsString != null)
					account.setGroupsAsString(groupsAsString);
				accountManager.save(account);
				addActionMessage(getText("save.success",
						"save {0} successfully", new String[] { account
								.getUsername() }));
			}
		}
		return SUCCESS;
	}

	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = accountManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Account> list = accountManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Account ac : list) {
					accountManager.delete(ac);
					sb.append(ac.getUsername() + ",");
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
