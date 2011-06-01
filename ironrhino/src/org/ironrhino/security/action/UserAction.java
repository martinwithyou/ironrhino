package org.ironrhino.security.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.compass.core.CompassHit;
import org.compass.core.support.search.CompassSearchResults;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.CurrentPassword;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.CompassCriteria;
import org.ironrhino.core.search.CompassSearchService;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;
import org.ironrhino.security.model.UserRole.UserRoleHelper;
import org.ironrhino.security.service.UserManager;
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

	@Autowired(required = false)
	private transient CompassSearchService compassSearchService;

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
		if (compassSearchService == null || StringUtils.isBlank(keyword)) {
			DetachedCriteria dc = userManager.detachedCriteria();
			dc.addOrder(Order.asc("username"));
			if (resultPage == null)
				resultPage = new ResultPage<User>();
			resultPage.setDetachedCriteria(dc);
			resultPage = userManager.findByResultPage(resultPage);
		} else {
			String query = keyword.trim();
			CompassCriteria cc = new CompassCriteria();
			cc.setQuery(query);
			cc.setAliases(new String[] { "user" });
			if (resultPage == null)
				resultPage = new ResultPage<User>();
			cc.setPageNo(resultPage.getPageNo());
			cc.setPageSize(resultPage.getPageSize());
			CompassSearchResults searchResults = compassSearchService
					.search(cc);
			resultPage.setTotalRecord(searchResults.getTotalHits());
			CompassHit[] hits = searchResults.getHits();
			if (hits != null) {
				List<User> list = new ArrayList<User>(hits.length);
				for (CompassHit ch : searchResults.getHits()) {
					User u = (User) ch.getData();
					list.add(u);
				}
				resultPage.setResult(list);
			} else {
				resultPage.setResult(Collections.EMPTY_LIST);
			}
		}
		return LIST;
	}

	@Override
	public String input() {
		user = userManager.get(getUid());
		if (user == null) {
			user = new User();
		} else {
			roleId = new String[user.getRoles().size()];
			Iterator<String> it = user.getRoles().iterator();
			int i = 0;
			while (it.hasNext())
				roleId[i++] = it.next();
		}
		roles = new LinkedHashMap<String, String>();
		Set<String> set = UserRoleHelper.getAllRoles();
		for (String name : set)
			roles.put(name, getText(name));
		return INPUT;
	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.username", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.name", trim = true, key = "validation.required") }, emails = { @EmailValidator(fieldName = "user.email", key = "validation.invalid") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "user.username", expression = User.USERNAME_REGEX, key = "validation.invalid") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error") })
	public String save() {
		if (StringUtils.isBlank(user.getEmail()))
			user.setEmail(null);
		if (user.isNew()) {
			user.setUsername(user.getUsername().toLowerCase());
			if (userManager.findByNaturalId(user.getUsername()) != null) {
				addFieldError("user.username",
						getText("validation.already.exists"));
				return INPUT;
			}
			if (StringUtils.isNotBlank(user.getEmail())
					&& userManager.findByNaturalId("email", user.getEmail()) != null) {
				addFieldError("user.email",
						getText("validation.already.exists"));
				return INPUT;
			}
			user.setLegiblePassword(password);
		} else {

			User temp = user;
			user = userManager.get(temp.getId());
			if (StringUtils.isNotBlank(temp.getEmail())
					&& !temp.getEmail().equals(user.getEmail())
					&& userManager.findByNaturalId("email", temp.getEmail()) != null) {
				addFieldError("user.email",
						getText("validation.already.exists"));
				return INPUT;
			}
			BeanUtils.copyProperties(temp, user);
			if (StringUtils.isNotBlank(password))
				user.setLegiblePassword(password);
		}
		user.getRoles().clear();
		if (roleId != null) {
			for (String role : roleId)
				user.getRoles().add(role);
		}
		userManager.save(user);
		addActionMessage(getText("save.success"));
		return SUCCESS;
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
		user.setLegiblePassword(password);
		userManager.save(user);
		addActionMessage(getText("save.success"));
		return "password";
	}

	@Authorize(ifAnyGranted = UserRole.ROLE_BUILTIN_USER)
	@InputConfig(methodName = "inputprofile")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.name", trim = true, key = "validation.required") }, emails = { @EmailValidator(fieldName = "user.email", key = "validation.invalid") })
	public String profile() {
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
		return JSON;
	}

}
