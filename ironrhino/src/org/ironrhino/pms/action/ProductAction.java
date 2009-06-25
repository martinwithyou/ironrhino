package org.ironrhino.pms.action;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Attribute;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.common.util.BeanUtils;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.common.util.Thumbnail;
import org.ironrhino.common.util.WaterMark;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.model.Product;
import org.ironrhino.pms.service.CategoryManager;
import org.ironrhino.pms.service.ProductManager;

import com.opensymphony.xwork2.util.CreateIfNull;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ProductAction extends BaseAction {

	private static Log log = LogFactory.getLog(ProductAction.class);

	public static final String SETTING_NAME_DEFAULT_NEW_ARRIVAL_DAYS = "product.defaultNewArrivalDays";

	private Product product;

	private ResultPage<Product> resultPage;

	private Integer categoryId;

	private List<Attribute> attributes;

	private String relatedProductsAsString;

	private File picture;

	private String pictureContentType;

	private boolean useWaterMark = true;

	private boolean overrideDefault;

	private String pictureName;

	private String actionType;

	private String rolesAsString;

	private String tagsAsString;

	private transient WaterMark waterMark;

	private transient Thumbnail smallThumbnail;

	private transient Thumbnail mediumThumbnail;

	private transient CategoryManager categoryManager;

	private transient ProductManager productManager;

	private transient SettingControl settingControl;

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

	public void setRolesAsString(String rolesAsString) {
		this.rolesAsString = rolesAsString;
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

	public boolean isUseWaterMark() {
		return useWaterMark;
	}

	public void setUseWaterMark(boolean useWaterMark) {
		this.useWaterMark = useWaterMark;
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

	public void setRelatedProductsAsString(String relatedProductCodes) {
		this.relatedProductsAsString = relatedProductCodes;
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

	public void setMediumThumbnail(Thumbnail mediumThumbnail) {
		this.mediumThumbnail = mediumThumbnail;
	}

	public void setSmallThumbnail(Thumbnail smallThumbnail) {
		this.smallThumbnail = smallThumbnail;
	}

	public void setCategoryManager(CategoryManager categoryManager) {
		this.categoryManager = categoryManager;
	}

	public void setProductManager(ProductManager productManager) {
		this.productManager = productManager;
	}

	public void setSettingControl(SettingControl settingControl) {
		this.settingControl = settingControl;
	}

	public void setWaterMark(WaterMark waterMark) {
		this.waterMark = waterMark;
	}

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

	public String input() {
		product = productManager.get(getUid());
		if (product == null) {
			product = new Product();
			product.setReleased(true);
			product.setNewArrival(true);
		}
		return INPUT;
	}

	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "product.code", trim = true, key = "product.code.required", message = "请输入编号"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "product.name", trim = true, key = "product.name.required", message = "请输入名字"), })
	public String save2() {
		if (product == null)
			return INPUT;
		if (product.isNew()) {
			if (productManager.getByNaturalId("code", product.getCode()) != null) {
				addFieldError("product.code", getText("product.code.exists"));
				return INPUT;
			}
			if (categoryId != null) {
				Category category = categoryManager.get(categoryId);
				product.setCategory(category);
			}
			if (product.isReleased())
				product.setReleaseDate(new Date());
			if (product.isNewArrival()
					&& product.getNewArrivalTimeLimit() == null) {
				int defaultNewArrivalDays = 14;
				if (settingControl != null)
					defaultNewArrivalDays = settingControl.getIntValue(
							SETTING_NAME_DEFAULT_NEW_ARRIVAL_DAYS, 14);
				product.setNewArrivalTimeLimit(DateUtils.addDays(new Date(),
						defaultNewArrivalDays));
			}
		} else {
			Product temp = product;
			product = productManager.get(temp.getId());
			if (product.isReleased() && !temp.isReleased())
				product.setReleaseDate(null);
			// release
			if (!product.isReleased() && temp.isReleased())
				product.setReleaseDate(new Date());
			BeanUtils.copyProperties(temp, product, "releaseDate");
			if (!product.isNewArrival())
				product.setNewArrivalTimeLimit(null);
		}
		productManager.save(product);
		addActionMessage(getText("save.success", "save {0} successfully",
				new String[] { product.getCode() }));
		return SUCCESS;
	}

	// save related products and roles
	public String save() {
		if (product != null && product.getId() != null) {
			product = productManager.get(product.getId());
			if (product != null) {
				if (relatedProductsAsString != null) {
					product.getRelatedProducts().clear();
					productManager.save(product);
					Set<String> set = new HashSet<String>();
					String[] array = StringUtils.split(relatedProductsAsString,
							",");
					for (String name : array) {
						name = name.trim();
						if (!"".equals(name))
							set.add(name);
					}
					for (String code : set) {
						Product p = productManager.getByNaturalId("code", code);
						if (p != null)
							product.getRelatedProducts().add(p);
					}
				}
				if (rolesAsString != null)
					product.setRolesAsString(rolesAsString);
				if (tagsAsString != null)
					product.setTagsAsString(tagsAsString);
				productManager.save(product);
				addActionMessage(getText("save.success",
						"save {0} successfully", new String[] { product
								.getCode() }));
			}
		}
		return SUCCESS;
	}

	public String view() {
		product = productManager.get(getUid());
		return VIEW;
	}

	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = productManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Product> list = productManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Product product : list) {
					productManager.delete(product);
					sb.append(product.getCode() + ",");
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

	public String attribute() {
		product = productManager.get(getUid());
		if (product != null) {
			if (attributes != null) {
				product.getAttributes().clear();
				for (Attribute attr : attributes)
					if (attr != null && StringUtils.isNotBlank(attr.getName()))
						product.getAttributes().add(attr);
				productManager.save(product);
				addActionMessage(getText("save.attributes.success",
						"save attributes successfully"));
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
			addActionMessage(getText("change.category.success",
					"change category to {0} successfully",
					new String[] { product.getCategory().getName() }));
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
				product = productManager.getByNaturalId("code", code);
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
		Image pictureImage = ImageIO.read(picture);
		BufferedImage bufferedImage;
		FileOutputStream fos;
		JPEGImageEncoder encoder;
		if (useWaterMark) {
			bufferedImage = waterMark.mark(pictureImage);
			fos = new FileOutputStream(target);
			encoder = JPEGCodec.createJPEGEncoder(fos);
			encoder.encode(bufferedImage);
			fos.flush();
			fos.close();
		} else {
			if (!picture.renameTo(target))
				log.error("failed rename " + picture.getAbsolutePath() + " to "
						+ target.getAbsolutePath());
		}
		bufferedImage = smallThumbnail.resizeFix(pictureImage);
		fos = new FileOutputStream(path.replace(".jpg", ".small.jpg"));
		encoder = JPEGCodec.createJPEGEncoder(fos);
		encoder.encode(bufferedImage);
		fos.flush();
		fos.close();
		bufferedImage = mediumThumbnail.resizeFix(pictureImage);
		fos = new FileOutputStream(path.replace(".jpg", ".medium.jpg"));
		encoder = JPEGCodec.createJPEGEncoder(fos);
		encoder.encode(bufferedImage);
		fos.flush();
		fos.close();
	}

	private void deletePictureFile(String filename) {
		String path = getPicturePath(filename);
		File f = new File(path);
		if (!f.delete())
			log.error("failed to delete " + f.getAbsolutePath());
		f = new File(path.replace(".jpg", ".small.jpg"));
		if (!f.delete())
			log.error("failed to delete " + f.getAbsolutePath());
		f = new File(path.replace(".jpg", ".medium.jpg"));
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
		f1 = new File(path1.replace(".jpg", ".small.jpg"));
		f2 = new File(path2.replace(".jpg", ".small.jpg"));
		if (!f1.renameTo(f2))
			log.error("failed rename " + f1.getAbsolutePath() + " to "
					+ f2.getAbsolutePath());
		f1 = new File(path1.replace(".jpg", ".medium.jpg"));
		f2 = new File(path2.replace(".jpg", ".medium.jpg"));
		if (!f1.renameTo(f2))
			log.error("failed rename " + f1.getAbsolutePath() + " to "
					+ f2.getAbsolutePath());
	}

	private String getPicturePath(String filename) {
		return ServletActionContext.getServletContext().getRealPath(
				"/pic/" + filename + ".jpg");

	}
}
