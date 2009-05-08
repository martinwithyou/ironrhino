package org.ironrhino.ums.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.util.BeanUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class UserAction extends BaseAction {

	private User user;

	private ResultPage<User> resultPage;

	private String rolesAsString;

	private String groupsAsString;

	private String password;

	private String confirmPassword;

	private UserManager userManager;

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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ResultPage<User> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<User> resultPage) {
		this.resultPage = resultPage;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public String execute() {
		DetachedCriteria dc = userManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage<User>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("username"));
		resultPage = userManager.getResultPage(resultPage);
		return LIST;
	}

	public String input() {
		user = userManager.get(getUid());
		if (user == null)
			user = new User();
		return INPUT;
	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.username", trim = true, key = "user.username.required", message = "请输入用户名"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.name", trim = true, key = "user.name.required", message = "请输入姓名"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "user.email.required", message = "请输入email") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "user.username", expression = "^\\w{3,20}$", key = "user.username.invalid", message = "username不合法") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "user.email.invalid", message = "请输入正确的email") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error", message = "两次输入密码不一致") })
	public String save2() {
		if (user.isNew()) {
			user.setUsername(user.getUsername().toLowerCase());
			if (userManager.getByNaturalId("username", user.getUsername()) != null) {
				addFieldError("user.username", getText("user.username.exists"));
				return INPUT;
			}
			user.setLegiblePassword(password);
		} else {
			User temp = user;
			user = userManager.get(temp.getId());
			BeanUtils.copyProperties(temp, user);
		}
		userManager.save(user);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	public String save() {
		if (user != null && user.getId() != null) {
			user = userManager.get(user.getId());
			if (user != null) {
				if (StringUtils.isNotBlank(password)
						&& !password.equals("********"))
					user.setLegiblePassword(password);
				if (rolesAsString != null)
					user.setRolesAsString(rolesAsString);
				if (groupsAsString != null)
					user.setGroupsAsString(groupsAsString);
				userManager.save(user);
				addActionMessage(getText("save.success",
						"save {0} successfully", new String[] { user
								.getUsername() }));
			}
		}
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	public String view() {
		if (user != null && user.getId() != null)
			user = userManager.get(user.getId());
		return VIEW;
	}

	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = userManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<User> list = userManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (User user : list) {
					userManager.delete(user);
					sb.append(user.getUsername() + ",");
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
