package org.ironrhino.pms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.Store;
import org.ironrhino.common.model.Attribute;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Customizable;
import org.ironrhino.core.model.Ordered;
import org.ironrhino.core.model.Recordable;

import com.opensymphony.xwork2.util.CreateIfNull;

@RecordAware
@PublishAware
@Searchable(alias = "product")
@AutoConfig(fileupload = "image/pjpeg,image/jpeg")
public class Product extends BaseEntity implements Ordered, Recordable,
		Customizable {

	private static final long serialVersionUID = 6787762149959503752L;

	private static final int SHORT_DESC_LENGTH = 200;

	@NaturalId
	@SearchableProperty(boost = 3, index = Index.NOT_ANALYZED)
	private String code;

	@SearchableProperty(boost = 3)
	private String name;

	@SearchableProperty
	private String description;

	private int inventory;

	private BigDecimal price;

	private ProductStatus status;

	@SearchableProperty(index = Index.NO, store = Store.YES)
	private int displayOrder;

	@NotInCopy
	private int pictureQuantity;

	@NotInCopy
	private Date createDate;

	@NotInCopy
	private Date modifyDate;

	@NotInCopy
	@SearchableComponent
	private Category category;

	@NotInCopy
	@CreateIfNull
	@SearchableComponent
	private List<Attribute> attributes = new ArrayList<Attribute>(0);

	@NotInCopy
	@SearchableComponent
	private List<SimpleElement> tags = new ArrayList<SimpleElement>(0);

	public Product() {
		createDate = new Date();
		displayOrder = 100;
	}

	public int getInventory() {
		return inventory;
	}

	public void setInventory(int inventory) {
		this.inventory = inventory;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public List<SimpleElement> getTags() {
		return tags;
	}

	public void setTags(List<SimpleElement> tags) {
		this.tags = tags;
	}

	@NotInCopy
	public String getTagsAsString() {
		return StringUtils.join(tags.iterator(), ',');
	}

	public void setTagsAsString(String tagsAsString) {
		SimpleElement.fillCollectionWithString(tags, tagsAsString);
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus(ProductStatus status) {
		this.status = status;
	}

	public int getPictureQuantity() {
		return pictureQuantity;
	}

	public void setPictureQuantity(int pictureQuantity) {
		this.pictureQuantity = pictureQuantity;
	}

	public boolean isPictured() {
		return pictureQuantity > 0;
	}

	public String getShortDescription() {
		return StringUtils.abbreviate(this.description, SHORT_DESC_LENGTH);
	}

	public int compareTo(Object object) {
		if (!(object instanceof Product))
			return 0;
		Product product = (Product) object;
		if (this.getDisplayOrder() != product.getDisplayOrder())
			return this.getDisplayOrder() - product.getDisplayOrder();
		return product.getCreateDate().compareTo(this.getCreateDate());
	}

	@Override
	public String toString() {
		return this.code;
	}

	private Map<String, Serializable> customProperties;

	public Map<String, Serializable> getCustomProperties() {
		if (customProperties == null)
			customProperties = new HashMap<String, Serializable>();
		return customProperties;
	}

	public void setCustomProperties(Map<String, Serializable> customProperties) {
		this.customProperties = customProperties;
	}

}
