package org.ironrhino.ums.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig
public class UserAction extends BaseAction {

	private static final long serialVersionUID = -302766917154104461L;

	private User user;

	private ResultPage<User> resultPage;

	private String rolesAsString;

	private String password;

	private String confirmPassword;

	@Autowired
	private transient UserManager userManager;

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

	@Override
	public String execute() {
		DetachedCriteria dc = userManager.detachedCriteria();
		if (resultPage == null)
			resultPage = new ResultPage<User>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("username"));
		resultPage = userManager.getResultPage(resultPage);
		return LIST;
	}

	@Override
	public String input() {
		user = userManager.get(getUid());
		if (user == null)
			user = new User();
		return INPUT;
	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.username", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.name", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "validation.required") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "user.username", expression = "^[\\w-]{3,20}$", key = "validation.invalid") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "validation.invalid") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error") })
	public String save2() {
		if (user.isNew()) {
			user.setUsername(user.getUsername().toLowerCase());
			if (userManager.getByNaturalId(user.getUsername()) != null) {
				addFieldError("user.username",
						getText("validation.already.exists"));
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

	@Override
	public String save() {
		if (user != null && user.getId() != null) {
			user = userManager.get(user.getId());
			if (user != null) {
				if (StringUtils.isNotBlank(password)
						&& !password.equals("********"))
					user.setLegiblePassword(password);
				if (rolesAsString != null)
					user.setRolesAsString(rolesAsString);
				userManager.save(user);
				addActionMessage(getText("save.success"));
			}
		}
		return SUCCESS;
	}

	@Override
	public String view() {
		if (user != null && user.getId() != null)
			user = userManager.get(user.getId());
		return VIEW;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = userManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<User> list = userManager.getListByCriteria(dc);
			if (list.size() > 0) {
				for (User user : list)
					userManager.delete(user);
				addActionMessage(getText("delete.success"));
			}
		}
		return SUCCESS;
	}

}
