package org.ironrhino.pms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.ironrhino.core.model.Secured;

import com.opensymphony.xwork2.util.CreateIfNull;

@RecordAware
@PublishAware
@Searchable(alias = "product")
@AutoConfig(fileupload = "image/pjpeg,image/jpeg")
public class Product extends BaseEntity implements Ordered, Secured,
		Recordable, Customizable {

	private static final int SHORT_DESC_LENGTH = 200;

	@NaturalId
	@SearchableProperty(boost = 3)
	private String code;

	@SearchableProperty(boost = 3)
	private String name;

	@SearchableProperty
	private String spec;

	@SearchableProperty
	private String material;

	private Color color;

	private String size;

	@SearchableProperty
	private String description;

	private int inventory;

	private BigDecimal price;

	private BigDecimal marketPrice;

	private ProductStatus status;

	@SearchableProperty(index = Index.NO, store = Store.YES)
	private int displayOrder;

	@NotInCopy
	private int pictureQuantity;

	private boolean newArrival;

	private Date newArrivalTimeLimit;

	private boolean released;

	@SearchableProperty(index = Index.NO, store = Store.YES)
	private Date releaseDate;

	@NotInCopy
	private Date createDate;

	@NotInCopy
	private Date modifyDate;

	@NotInCopy
	@SearchableComponent
	private Category category;

	@NotInCopy
	private Collection<Product> relatedProducts = new HashSet<Product>(0);

	@NotInCopy
	private Set<Product> reverseRelatedProducts = new HashSet<Product>(0);

	@NotInCopy
	private Set<SimpleElement> roles = new HashSet<SimpleElement>(0);

	@NotInCopy
	@CreateIfNull
	@SearchableComponent
	private List<Attribute> attributes = new ArrayList<Attribute>(0);

	@NotInCopy
	@SearchableComponent
	private List<SimpleElement> tags = new ArrayList<SimpleElement>(0);

	@NotInCopy
	@SearchableProperty
	private Boolean open;

	public Product() {
		createDate = new Date();
		displayOrder = 100;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getInventory() {
		return inventory;
	}

	public void setInventory(int inventory) {
		this.inventory = inventory;
	}

	public boolean isNewArrival() {
		return newArrival;
	}

	public void setNewArrival(boolean newArrival) {
		this.newArrival = newArrival;
	}

	public Date getNewArrivalTimeLimit() {
		return newArrivalTimeLimit;
	}

	public void setNewArrivalTimeLimit(Date newArrivalTimeLimit) {
		this.newArrivalTimeLimit = newArrivalTimeLimit;
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

	public Set<SimpleElement> getRoles() {
		return roles;
	}

	public void setRoles(Set<SimpleElement> roles) {
		this.roles = roles;
	}

	@NotInCopy
	public String getRolesAsString() {
		return StringUtils.join(roles.iterator(), ',');
	}

	public void setRolesAsString(String rolesAsString) {
		SimpleElement.fillCollectionWithString(roles, rolesAsString);
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

	public Collection<Product> getRelatedProducts() {
		return relatedProducts;
	}

	public void setRelatedProducts(Collection<Product> relatedProducts) {
		this.relatedProducts = relatedProducts;
	}

	@NotInCopy
	public String getRelatedProductsAsString() {
		return StringUtils.join(this.relatedProducts.iterator(), ",");
	}

	public Set<Product> getReverseRelatedProducts() {
		return reverseRelatedProducts;
	}

	public void setReverseRelatedProducts(Set<Product> reverseRelatedProducts) {
		this.reverseRelatedProducts = reverseRelatedProducts;
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

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
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

	public BigDecimal getMarketPrice() {
		return marketPrice;
	}

	public void setMarketPrice(BigDecimal marketPrice) {
		this.marketPrice = marketPrice;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Boolean getOpen() {
		if (open == null) {
			if (this.released && this.roles.size() == 0)
				open = Boolean.TRUE;
			else
				open = Boolean.FALSE;
		}
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
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
