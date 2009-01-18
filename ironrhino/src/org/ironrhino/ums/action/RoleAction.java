package org.ironrhino.ums.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.ecside.common.util.RequestUtil;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.util.BeanUtils;
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

	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		DetachedCriteria dc = roleManager.detachedCriteria();
		if (role != null) {
			if (StringUtils.isNotBlank(role.getName()))
				dc.add(Restrictions.ilike("name", role.getName(),
						MatchMode.ANYWHERE));
			if (StringUtils.isNotBlank(role.getDescription()))
				dc.add(Restrictions.ilike("description", role.getDescription(),
						MatchMode.ANYWHERE));
			String value = request.getParameter("role.enabled");
			if ("true".equals(value))
				dc.add(Restrictions.eq("enabled", true));
			else if ("false".equals(value))
				dc.add(Restrictions.eq("enabled", false));
		}
		if (resultPage == null)
			resultPage = new ResultPage<Role>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.desc("enabled"));
		resultPage.addOrder(Order.asc("name"));
		int totalRows = roleManager.countResultPage(resultPage);
		String pageSize = request.getParameter("ec_rd");
		if (StringUtils.isNumeric(pageSize))
			resultPage.setPageSize(Integer.parseInt(pageSize));
		int[] rowStartEnd = RequestUtil.getRowStartEnd(request, totalRows,
				resultPage.getPageSize());
		resultPage.setStart(rowStartEnd[0]);
		resultPage = roleManager.getResultPage(resultPage);
		request.setAttribute("recordList", resultPage.getResult());
		request.setAttribute("totalRows", resultPage.getTotalRecord());
		return "list";
	}

	public String input() {
		role = roleManager.get(getUid());
		if (role == null)
			role = new Role();
		return INPUT;
	}

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
