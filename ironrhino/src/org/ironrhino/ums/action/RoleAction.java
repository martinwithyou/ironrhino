package org.ironrhino.ums.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.util.BeanUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.ums.model.Role;
import org.ironrhino.ums.service.RoleManager;

import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class RoleAction extends BaseAction {

	protected static final Log log = LogFactory.getLog(RoleAction.class);

	private Role role;

	private ResultPage<Role> resultPage;

	private RoleManager roleManager;

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public ResultPage<Role> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Role> resultPage) {
		this.resultPage = resultPage;
	}

	public void setRoleManager(RoleManager roleManager) {
		this.roleManager = roleManager;
	}

	@Override
	public String execute() {
		DetachedCriteria dc = roleManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage<Role>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.desc("enabled"));
		resultPage.addOrder(Order.asc("name"));
		resultPage = roleManager.getResultPage(resultPage);
		return LIST;
	}

	@Override
	public String input() {
		role = roleManager.get(getUid());
		if (role == null)
			role = new Role();
		return INPUT;
	}

	@Override
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "role.name", trim = true, key = "role.name.required", message = "请输入名字") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "role.name", expression = "^\\w+$", key = "role.name.invalid", message = "必须为数字或者字母或者下划线") })
	public String save() {
		if (role.isNew()) {
			role.setName(role.getName().toUpperCase());
			if (role.getName().startsWith("ROLE_BUILTIN_")
					|| roleManager.getByNaturalId("name", role.getName()) != null) {
				addFieldError("role.name", getText("role.name.exists"));
				return INPUT;
			}
		} else {
			Role temp = role;
			role = roleManager.get(temp.getId());
			BeanUtils.copyProperties(temp, role);
		}
		roleManager.save(role);
		addActionMessage(getText("save.success", "save {0} successfully",
				new String[] { role.getName() }));
		return SUCCESS;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = roleManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Role> list = roleManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Role role : list) {
					roleManager.delete(role);
					sb.append(role.getName() + ",");
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
