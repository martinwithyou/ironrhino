package org.ironrhino.pms.action;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Attribute;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.model.Product;
import org.ironrhino.pms.service.CategoryManager;
import org.ironrhino.pms.service.ProductManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.util.CreateIfNull;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class ProductAction extends BaseAction {

	private static final long serialVersionUID = -5904974740762964365L;

	private static Log log = LogFactory.getLog(ProductAction.class);

	private Product product;

	private ResultPage<Product> resultPage;

	private Integer categoryId;

	private List<Attribute> attributes;

	private File picture;

	private String pictureContentType;

	private boolean overrideDefault;

	private String pictureName;

	private String actionType;

	private String tagsAsString;

	@Autowired
	private transient CategoryManager categoryManager;

	@Autowired
	private transient ProductManager productManager;

	public ResultPage<Product> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Product> resultPage) {
		this.resultPage = resultPage;
	}

	@CreateIfNull
	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public void setTagsAsString(String tagsAsString) {
		this.tagsAsString = tagsAsString;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}

	public void setOverrideDefault(boolean overrideDefault) {
		this.overrideDefault = overrideDefault;
	}

	public String getPictureContentType() {
		return pictureContentType;
	}

	public void setPictureContentType(String pictureContentType) {
		this.pictureContentType = pictureContentType;
	}

	public File getPicture() {
		return picture;
	}

	public void setPicture(File picture) {
		this.picture = picture;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	@Override
	public String execute() {
		DetachedCriteria dc = productManager.detachedCriteria();
		Category category = null;
		if (categoryId != null) {
			category = categoryManager.get(categoryId);
			if (category != null)
				dc.add(Restrictions.eq("category", category));
		}
		if (resultPage == null)
			resultPage = new ResultPage<Product>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("displayOrder"));
		resultPage.addOrder(Order.desc("createDate"));
		resultPage = productManager.getResultPage(resultPage);
		return LIST;
	}

	@Override
	public String input() {
		product = productManager.get(getUid());
		if (product == null) {
			product = new Product();
		}
		return INPUT;
	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "product.code", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "product.name", trim = true, key = "validation.required") })
	public String save2() {
		if (product == null)
			return INPUT;
		if (product.isNew()) {
			if (productManager.getByNaturalId(product.getCode()) != null) {
				addFieldError("product.code",
						getText("validation.already.exists"));
				return INPUT;
			}
			if (categoryId != null) {
				Category category = categoryManager.get(categoryId);
				product.setCategory(category);
			}
		} else {
			Product temp = product;
			product = productManager.get(temp.getId());
			BeanUtils.copyProperties(temp, product);
		}
		productManager.save(product);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	// save related products and roles
	@Override
	public String save() {
		if (product != null && product.getId() != null) {
			product = productManager.get(product.getId());
			if (product != null) {
				if (tagsAsString != null)
					product.setTagsAsString(tagsAsString);
				productManager.save(product);
				addActionMessage(getText("save.success"));
			}
		}
		return SUCCESS;
	}

	@Override
	public String view() {
		product = productManager.get(getUid());
		return VIEW;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = productManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Product> list = productManager.getListByCriteria(dc);
			if (list.size() > 0) {
				for (Product product : list)
					productManager.delete(product);
				addActionMessage(getText("delete.success"));
			}
		}
		return SUCCESS;
	}

	public String attribute() {
		product = productManager.get(getUid());
		if (product != null) {
			if (attributes != null) {
				product.getAttributes().clear();
				for (Attribute attr : attributes)
					if (attr != null && StringUtils.isNotBlank(attr.getName()))
						product.getAttributes().add(attr);
				productManager.save(product);
				addActionMessage(getText("save.success"));
			}
			attributes = product.getAttributes();
		}
		return "attribute";
	}

	public String category() {
		product = productManager.get(getUid());
		if (product != null && categoryId != null) {
			product.setCategory(categoryManager.get(categoryId));
			productManager.save(product);
			addActionMessage(getText("operation.success"));
		}
		return "category";
	}

	public String picture() throws Exception {
		if ("delete".equals(actionType)) {
			if (StringUtils.isNotBlank(pictureName)) {
				String code;
				int index;
				int temp;
				if ((temp = pictureName.lastIndexOf('_')) > 0) {
					code = pictureName.substring(0, temp);
					index = Integer.parseInt(pictureName.substring(temp + 1));
				} else {
					code = pictureName;
					index = 0;
				}
				deletePictureFile(pictureName);
				product = productManager.getByNaturalId(code);
				product.setPictureQuantity(product.getPictureQuantity() - 1);
				productManager.save(product);
				int lastIndex = product.getPictureQuantity();
				// move last to deleted
				if (index == 0 && lastIndex > 0)
					renamePictureFile(code + "_" + lastIndex, code);
				else if (index > 0 && index < lastIndex)
					renamePictureFile(code + "_" + lastIndex, code + "_"
							+ index);
			}
		} else {
			product = productManager.get(getUid());
			if ("save".equals(actionType)) {
				if (picture == null) {
					addFieldError("picture", "must upload a image file");
					return "viewPicture";
				}
				if (overrideDefault || !product.isPictured())
					savePictureFile(product.getCode());
				else
					savePictureFile(product.getCode() + "_"
							+ product.getPictureQuantity());
				// not override
				if (!product.isPictured() || !overrideDefault)
					product
							.setPictureQuantity(product.getPictureQuantity() + 1);
				productManager.save(product);
			}
		}
		return "picture";
	}

	private void savePictureFile(String filename) throws Exception {
		String path = getPicturePath(filename);
		File target = new File(path);
		if (!picture.renameTo(target))
			log.error("failed rename " + picture.getAbsolutePath() + " to "
					+ target.getAbsolutePath());
	}

	private void deletePictureFile(String filename) {
		String path = getPicturePath(filename);
		File f = new File(path);
		if (!f.delete())
			log.error("failed to delete " + f.getAbsolutePath());
	}

	private void renamePictureFile(String filename, String newfilename) {
		String path1 = getPicturePath(filename);
		String path2 = getPicturePath(newfilename);
		File f1 = new File(path1);
		File f2 = new File(path2);
		if (!f1.renameTo(f2))
			log.error("failed rename " + f1.getAbsolutePath() + " to "
					+ f2.getAbsolutePath());
	}

	private String getPicturePath(String filename) {
		return ServletActionContext.getServletContext().getRealPath(
				"/pic/" + filename + ".jpg");

	}
}
