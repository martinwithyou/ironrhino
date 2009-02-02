package org.ironrhino.online.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.annotation.Captcha;
import org.ironrhino.core.annotation.JsonConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.mail.MailService;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.model.ProductComment;
import org.ironrhino.online.model.ProductFavorite;
import org.ironrhino.online.model.ProductScore;
import org.ironrhino.online.model.ProductSend;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.online.support.ProductHitsControl;
import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.model.Product;
import org.ironrhino.pms.support.CategoryTreeControl;
import org.springframework.mail.SimpleMailMessage;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class ProductAction extends BaseAction {

	public static final String PSEUDO_CATEGORY_CODE_HISTORY = "history";

	public static final String PSEUDO_CATEGORY_CODE_NULL = "null";

	private ProductFacade productFacade;

	private CategoryTreeControl categoryTreeControl;

	private Product product;

	private ResultPage<Product> resultPage;

	private Category category;

	private SyndFeed feed;

	private String newArrival;

	private int max = 100;

	private int score;

	private List<AggregateResult> suggestions;

	private AggregateResult scoreResult;

	private ProductComment comment;

	private ProductSend send;

	private BaseManager baseManager;

	private ProductHitsControl productHitsControl;

	private MailService mailService;

	public Product getProduct() {
		return this.product;
	}

	public ProductSend getSend() {
		return send;
	}

	public void setSend(ProductSend send) {
		this.send = send;
	}

	public void setComment(ProductComment comment) {
		this.comment = comment;
	}

	public ProductComment getComment() {
		return comment;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public AggregateResult getScoreResult() {
		return scoreResult;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void setNewArrival(String newArrival) {
		this.newArrival = newArrival;
	}

	public SyndFeed getFeed() {
		return feed;
	}

	public Category getCategory() {
		return category;
	}

	public ResultPage<Product> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Product> resultPage) {
		this.resultPage = resultPage;
	}

	public List<AggregateResult> getSuggestions() {
		return suggestions;
	}

	public void setProductFacade(ProductFacade productFacade) {
		this.productFacade = productFacade;
	}

	public void setCategoryTreeControl(CategoryTreeControl categoryTreeControl) {
		this.categoryTreeControl = categoryTreeControl;
	}

	public CategoryTreeControl getCategoryTreeControl() {
		return categoryTreeControl;
	}

	public void setProductHitsControl(ProductHitsControl productHitsControl) {
		this.productHitsControl = productHitsControl;
	}

	public void setBaseManager(BaseManager baseManager) {
		this.baseManager = baseManager;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public String input() {
		return INPUT;
	}

	@SkipValidation
	public String execute() {
		return list();
	}

	@SkipValidation
	public String list() {
		if (resultPage == null)
			resultPage = new ResultPage<Product>();
		String cateCode = getUid();
		if (PSEUDO_CATEGORY_CODE_HISTORY.equalsIgnoreCase(cateCode))
			return history();
		resultPage = productFacade.getResultPageByCategoryCode(resultPage,
				cateCode);
		if (StringUtils.isNotBlank(cateCode))
			category = categoryTreeControl.getCategoryTree()
					.getDescendantOrSelfByCode(cateCode);
		return LIST;
	}

	@SkipValidation
	public String history() {
		List<Product> list;
		String history = RequestUtils.getCookieValue(ServletActionContext
				.getRequest(), "HISTORY");
		if (StringUtils.isNotBlank(history)) {
			String[] array = history.split(",");
			list = productFacade.getProducts(array);
		} else {
			list = Collections.EMPTY_LIST;
		}
		resultPage = new ResultPage<Product>();
		resultPage.setResult(list);
		resultPage.setTotalPage(1);
		resultPage.setTotalRecord(list.size());
		setUid(PSEUDO_CATEGORY_CODE_HISTORY);
		return LIST;
	}

	@SkipValidation
	public String tag() {
		if (resultPage == null)
			resultPage = new ResultPage<Product>();
		String tag = getUid();
		resultPage = productFacade.getResultPageByTag(resultPage, tag);
		return LIST;
	}

	@SkipValidation
	public String suggest() {
		String prefix = getUid();
		suggestions = productFacade.getTags(prefix);
		return SUGGEST;
	}

	@SkipValidation
	public String view() {
		String code = getUid();
		if (StringUtils.isNotBlank(code)) {
			product = productFacade.getProductByCode(code);
			if (product != null && !AuthzUtils.hasPermission(product))
				return ACCESSDENIED;
			scoreResult = productFacade.getScoreResult(code);
			if (resultPage == null)
				resultPage = new ResultPage();
			baseManager.setEntityClass(ProductComment.class);
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.eq("productCode", code));
			resultPage.setDetachedCriteria(dc);
			resultPage.addOrder(Order.desc("commentDate"));
			resultPage = baseManager.getResultPage(resultPage);

			Account account = AuthzUtils.getUserDetails(Account.class);
			if (account != null) {
				if (send == null)
					send = new ProductSend();
				send.setName(account.getName());
				send.setEmail(account.getEmail());
				if (comment == null)
					comment = new ProductComment();
				comment.setDisplayName(account.getNickname());
				comment.setEmail(account.getEmail());
			}
		}
		return VIEW;
	}

	@SkipValidation
	public String random() {
		setUid(productFacade.getRandomProduct().getCode());
		return "success";
	}

	@SkipValidation
	public String hit() {
		String code = getUid();
		if (productHitsControl != null && StringUtils.isNotBlank(code))
			productHitsControl.put(code);
		return NONE;
	}

	@SkipValidation
	public String feed() {
		String siteUrl = RequestUtils.getBaseUrl(ServletActionContext
				.getRequest());
		feed = new SyndFeedImpl();
		feed.setTitle("products");
		feed.setDescription("products");
		feed.setLink(siteUrl);
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		feed.setEntries(entries);
		List<Product> productList = newArrival != null ? productFacade
				.getNewArrivalProducts(max) : productFacade.getProducts(max);
		for (Product product : productList) {
			SyndEntry entry = new SyndEntryImpl();
			entry.setTitle(product.getName());
			entry.setLink(siteUrl + "/product/" + product.getCode() + ".html");
			entry.setPublishedDate(product.getReleaseDate());
			SyndCategory cat = new SyndCategoryImpl();
			cat.setName(product.getCategory().getName());
			List<SyndCategory> cats = new ArrayList<SyndCategory>();
			cats.add(cat);
			entry.setCategories(cats);
			SyndContent content = new SyndContentImpl();
			content.setValue(product.getDescription());
			List<SyndContent> contents = new ArrayList<SyndContent>();
			contents.add(content);
			entry.setContents(contents);
			entry.setDescription(content);
			entries.add(entry);
		}
		return FEED;
	}

	@InputConfig(methodName="input")
	@Captcha
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "comment.content", trim = true, key = "comment.content.required", message = "请输入评论内容") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "comment.email", key = "comment.email.invalid", message = "请输入正确的email") })
	public String comment() {
		String code = getUid();
		if (StringUtils.isNotBlank(code) && comment != null) {
			comment.setProductCode(code);
			comment.setUsername(AuthzUtils.getUsername());
			baseManager.save(comment);
		}
		return REFERER;
	}

	@InputConfig(methodName="input")
	@Captcha
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "send.name", trim = true, key = "send.name.required", message = "请输入您的名字"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "send.email", trim = true, key = "send.email.required", message = "请输入您的email"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "send.destination", trim = true, key = "send.destination.required", message = "请输入对方的email") }, emails = {
			@EmailValidator(type = ValidatorType.FIELD, fieldName = "send.email", key = "send.email.invalid", message = "请输入正确的email"),
			@EmailValidator(type = ValidatorType.FIELD, fieldName = "send.destination", key = "send.destination.invalid", message = "请输入正确的email") })
	public String send() {
		String code = getUid();
		send.setProductCode(code);
		Account account = AuthzUtils.getUserDetails(Account.class);
		baseManager.setEntityClass(ProductSend.class);
		if (account != null) {
			send.setUsername(account.getUsername());
		}
		SimpleMailMessage smm = new SimpleMailMessage();
		baseManager.save(send);
		if (StringUtils.isBlank(send.getName())) {
			send.setName("");
		}
		if (StringUtils.isNotBlank(send.getEmail()))
			smm.setFrom(send.getName() + "<" + send.getEmail() + ">");
		smm.setTo(send.getDestination());
		smm.setSubject(getText("send.subject", "your friend "
				+ account.getFriendlyName() + " recommend our product to you",
				new String[] { account.getFriendlyName() }));
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("message", send.getMessage());
		model.put("product", productFacade.getProductByCode(code));
		mailService.send(smm, "product_send.ftl", model);
		addActionMessage(getText("send.successfully"));
		return REFERER;
	}

	@SkipValidation
	public String favorite() {
		String code = getUid();
		Account account = AuthzUtils.getUserDetails(Account.class);
		if (account == null) {
			addActionError("not.login");
		} else if (StringUtils.isNotBlank(code)) {
			baseManager.setEntityClass(ProductFavorite.class);
			ProductFavorite favor = (ProductFavorite) baseManager
					.getByNaturalId("username", account.getUsername(),
							"productCode", code);
			if (favor == null) {
				Product product = productFacade.getProductByCode(code);
				if (product != null) {
					ProductFavorite pf = new ProductFavorite();
					pf.setUsername(account.getUsername());
					pf.setProductCode(product.getCode());
					pf.setProductName(product.getName());
					baseManager.save(pf);
					addActionMessage("favorite.succussfully");
				}
			} else {
				addActionMessage("favor.exist");
			}
		}
		return REFERER;
	}

	@SkipValidation
	@JsonConfig(propertyName = "scoreResult")
	public String score() {
		String code = getUid();
		Account account = AuthzUtils.getUserDetails(Account.class);
		if (account == null) {
			addActionError("not.login");
		} else if (StringUtils.isNotBlank(code) && (score >= 1 && score <= 5)) {
			String username = AuthzUtils.getUsername();
			baseManager.setEntityClass(ProductScore.class);
			ProductScore ps = (ProductScore) baseManager.getByNaturalId(
					"username", username, "productCode", code);
			if (ps == null) {
				ps = new ProductScore();
				ps.setUsername(username);
				ps.setProductCode(code);
				ps.setScore(score);
			} else {
				ps.setScore(score);
				ps.setScoreDate(new Date());
			}
			baseManager.save(ps);
			scoreResult = productFacade.getScoreResult(ps.getProductCode());
		}
		return REFERER;
	}

}
