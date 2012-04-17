package org.ironrhino.security.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.hibernate.CriterionUtils;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.CurrentPassword;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.compass.CompassSearchCriteria;
import org.ironrhino.core.search.compass.CompassSearchService;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.ironrhino.security.service.UserManager;
import org.ironrhino.security.service.UserRoleManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.ExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
public class UserAction extends BaseAction {

	private static final long serialVersionUID = -79191921685741502L;

	private User user;

	private String[] roleId;

	private Map<String, String> roles;

	private ResultPage<User> resultPage;

	private String password;

	private String confirmPassword;

	@Inject
	private transient UserManager userManager;

	@Inject
	private transient UserRoleManager userRoleManager;

	@Autowired(required = false)
	private transient CompassSearchService<User> compassSearchService;

	public String[] getRoleId() {
		return roleId;
	}

	public void setRoleId(String[] roleId) {
		this.roleId = roleId;
	}

	public Map<String, String> getRoles() {
		return roles;
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

	@Override
	public String execute() {
		if (StringUtils.isBlank(keyword) || compassSearchService == null) {
			DetachedCriteria dc = userManager.detachedCriteria();
			Criterion filtering = CriterionUtils.filter(user, "id", "username",
					"name", "enabled");
			if (filtering != null)
				dc.add(filtering);
			if (StringUtils.isNotBlank(keyword))
				dc.add(CriterionUtils.like(keyword, MatchMode.ANYWHERE,
						"username", "name"));
			dc.addOrder(Order.asc("username"));
			if (resultPage == null)
				resultPage = new ResultPage<User>();
			resultPage.setCriteria(dc);
			resultPage = userManager.findByResultPage(resultPage);
		} else {
			String query = keyword.trim();
			CompassSearchCriteria criteria = new CompassSearchCriteria();
			criteria.setQuery(query);
			criteria.setAliases(new String[] { "user" });
			if (resultPage == null)
				resultPage = new ResultPage<User>();
			resultPage.setCriteria(criteria);
			resultPage = compassSearchService.search(resultPage);
		}
		return LIST;
	}

	@Override
	public String input() {
		String id = getUid();
		if (StringUtils.isNotBlank(id)) {
			user = userManager.get(id);
			if (user == null)
				user = userManager.findByNaturalId(id);
		}
		if (user == null) {
			user = new User();
		} else {
			roleId = new String[user.getRoles().size()];
			Iterator<String> it = user.getRoles().iterator();
			int i = 0;
			while (it.hasNext())
				roleId[i++] = it.next();
		}
		roles = userRoleManager.getAllRoles();
		for (Map.Entry<String, String> entry : roles.entrySet())
			if (StringUtils.isBlank(entry.getValue()))
				entry.setValue(getText(entry.getKey()));
		return INPUT;
	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.username", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.name", trim = true, key = "validation.required") }, emails = { @EmailValidator(fieldName = "user.email", key = "validation.invalid") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "user.username", expression = User.USERNAME_REGEX, key = "validation.invalid") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error") })
	public String save() {
		if (!makeEntityValid())
			return INPUT;
		if (StringUtils.isBlank(user.getEmail()))
			user.setEmail(null);
		user.getRoles().clear();
		if (roleId != null) {
			for (String role : roleId)
				user.getRoles().add(role);
		}
		userManager.save(user);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	public String checkavailable() {
		return makeEntityValid() ? NONE : INPUT;
	}

	private boolean makeEntityValid() {
		if (user == null) {
			addActionError(getText("access.denied"));
			return false;
		}
		if (StringUtils.isBlank(user.getEmail()))
			user.setEmail(null);
		if (user.isNew()) {
			if (StringUtils.isNotBlank(user.getUsername())) {
				user.setUsername(user.getUsername().toLowerCase());
				if (userManager.findByNaturalId(user.getUsername()) != null) {
					addFieldError("user.username",
							getText("validation.already.exists"));
					return false;
				}
			}
			if (StringUtils.isNotBlank(user.getEmail())
					&& userManager.findByNaturalId("email", user.getEmail()) != null) {
				addFieldError("user.email",
						getText("validation.already.exists"));
				return false;
			}
			user.setLegiblePassword(password);
		} else {
			User temp = user;
			user = userManager.get(temp.getId());
			userManager.evict(user);
			if (StringUtils.isNotBlank(temp.getEmail())
					&& !temp.getEmail().equals(user.getEmail())
					&& userManager.findByNaturalId("email", temp.getEmail()) != null) {
				addFieldError("user.email",
						getText("validation.already.exists"));
				return false;
			}
			BeanUtils.copyProperties(temp, user);
			if (StringUtils.isNotBlank(password))
				user.setLegiblePassword(password);
		}
		return true;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			List<User> list;
			if (id.length == 1) {
				list = new ArrayList<User>(1);
				list.add(userManager.get(id[0]));
			} else {
				DetachedCriteria dc = userManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = userManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (final User user : list) {
					if (!userManager.canDelete(user)) {
						deletable = false;
						addActionError(getText("delete.forbidden",
								new String[] { user.getUsername() }));
						break;
					}
				}
				if (deletable) {
					for (User user : list)
						userManager.delete(user);
					addActionMessage(getText("delete.success"));
				}
			}
		}
		return SUCCESS;
	}

	@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_USER)
	@InputConfig(resultName = "password")
	@CurrentPassword
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, trim = true, fieldName = "password", key = "validation.required") }, expressions = { @ExpressionValidator(expression = "password == confirmPassword", key = "confirmPassword.error") })
	public String password() {
		User user = AuthzUtils.getUserDetails(User.class);
		if (user != null) {
			user.setLegiblePassword(password);
			userManager.save(user);
			addActionMessage(getText("save.success"));
		}
		return "password";
	}

	@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_USER)
	@InputConfig(methodName = "inputprofile")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.name", trim = true, key = "validation.required") }, emails = { @EmailValidator(fieldName = "user.email", key = "validation.invalid") })
	public String profile() {
		if (!makeEntityValid())
			return INPUT;
		User userInSession = AuthzUtils.getUserDetails(User.class);
		if (userInSession == null || user == null) {
			return "profile";
		}
		userInSession.setName(user.getName());
		userInSession.setEmail(user.getEmail());
		userInSession.setPhone(user.getPhone());
		userManager.save(userInSession);
		addActionMessage(getText("save.success"));
		return "profile";
	}

	@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_USER)
	public String inputprofile() {
		user = AuthzUtils.getUserDetails(User.class);
		return "profile";
	}

	@JsonConfig(root = "user")
	@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_USER)
	public String self() {
		user = AuthzUtils.getUserDetails(User.class);
		if (user != null) {
			user.setCreateUser(null);
			user.setModifyUser(null);
		}
		return JSON;
	}

}
