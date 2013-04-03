package org.ironrhino.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Ordered;
import org.ironrhino.core.model.Recordable;
import org.ironrhino.core.search.elasticsearch.annotations.Index;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.security.core.userdetails.UserDetails;

@Searchable(type = "page")
@AutoConfig(searchable = true)
@Entity
@Table(name = "common_page")
public class Page extends BaseEntity implements Recordable<UserDetails>,
		Ordered {

	private static final long serialVersionUID = 4688382703803043164L;

	@NaturalId(mutable = true, caseInsensitive = true)
	@SearchableProperty(index = Index.NOT_ANALYZED)
	@Column(unique = true, nullable = false)
	@org.hibernate.annotations.NaturalId(mutable = true)
	@Access(AccessType.FIELD)
	private String pagepath;

	@SearchableProperty
	private String title;

	@NotInJson
	@Column
	@Type(type = "java.sql.Clob")
	@Access(AccessType.FIELD)
	private String head;

	@NotInJson
	@SearchableProperty
	@Column(nullable = false)
	@Type(type = "java.sql.Clob")
	@Access(AccessType.FIELD)
	private String content;

	@SearchableProperty
	private int displayOrder;

	@NotInJson
	@Column
	@Type(type = "java.sql.Clob")
	@Access(AccessType.FIELD)
	private String draft;

	private Date draftDate;

	@NotInCopy
	@SearchableProperty
	private Date createDate;

	@NotInCopy
	@SearchableProperty
	private Date modifyDate;

	@NotInCopy
	@SearchableProperty(include_in_all = false)
	private String createUser;

	@NotInCopy
	@SearchableProperty(include_in_all = false)
	private String modifyUser;

	@NotInCopy
	@SearchableProperty(index = Index.NOT_ANALYZED)
	@Transient
	private Set<String> tags = new LinkedHashSet<String>(0);

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getPagepath() {
		return pagepath;
	}

	public void setPagepath(String pagepath) {
		this.pagepath = pagepath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDraft() {
		return draft;
	}

	public void setDraft(String draft) {
		this.draft = draft;
	}

	public Date getDraftDate() {
		return draftDate;
	}

	public void setDraftDate(Date draftDate) {
		this.draftDate = draftDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	@Transient
	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	@NotInCopy
	@NotInJson
	@Column(name = "tags", length = 1024)
	@Access(AccessType.PROPERTY)
	public String getTagsAsString() {
		if (tags.size() > 0)
			return StringUtils.join(tags.iterator(), ',');
		return null;
	}

	public void setTagsAsString(String tagsAsString) {
		tags.clear();
		if (StringUtils.isNotBlank(tagsAsString))
			tags.addAll(Arrays.asList(org.ironrhino.core.util.StringUtils
					.trimTail(tagsAsString, ",").split("\\s*,\\s*")));
	}

	@Override
	public void setCreateUserDetails(UserDetails user) {
		if (user != null)
			createUser = user.getUsername();
	}

	@Override
	public void setModifyUserDetails(UserDetails user) {
		if (user != null)
			modifyUser = user.getUsername();
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public String abbreviate(int size) {
		return StringUtils.isNotBlank(content) ? StringUtils.abbreviate(
				Jsoup.parse(content).text().trim(), size).trim() : null;
	}

	@Transient
	public List<Image> getImages() {
		List<Image> images = new ArrayList<Image>();
		if (StringUtils.isNotBlank(content)) {
			Elements elements = Jsoup.parse(content).select("img");
			for (int i = 0; i < elements.size(); i++) {
				Element img = elements.get(i);
				Image image = new Image();
				image.setSrc(img.attr("src"));
				image.setAlt(img.attr("alt"));
				image.setTitle(img.attr("title"));
				images.add(image);
			}
		}
		return images;
	}

	public static class Image implements Serializable {

		private static final long serialVersionUID = -3425565099362299759L;
		private String src;
		private String alt;
		private String title;

		public String getSrc() {
			return src;
		}

		public void setSrc(String src) {
			this.src = src;
		}

		public String getAlt() {
			return alt;
		}

		public void setAlt(String alt) {
			this.alt = alt;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	}

	public int compareTo(Object object) {
		if (!(object instanceof Ordered))
			return 0;
		Ordered ordered = (Ordered) object;
		if (this.getDisplayOrder() != ordered.getDisplayOrder())
			return this.getDisplayOrder() - ordered.getDisplayOrder();
		return this.toString().compareTo(ordered.toString());
	}

	public String toString() {
		return this.pagepath;
	}
}
