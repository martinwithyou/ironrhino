package org.ironrhino.common.model;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.compass.annotations.Store;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Ordered;
import org.ironrhino.core.model.Recordable;
import org.springframework.security.core.userdetails.UserDetails;

@Searchable(alias = "page")
@AutoConfig(searchable = true)
public class Page extends BaseEntity implements Recordable, Ordered {

	private static final long serialVersionUID = 4688382703803043164L;

	@NaturalId(mutable = true, caseInsensitive = true)
	@SearchableProperty(index = Index.UN_TOKENIZED)
	private String path;

	@SearchableProperty
	private String title;

	@NotInJson
	@SearchableProperty
	private String content;

	private int displayOrder;

	@NotInJson
	private String draft;

	private Date draftDate;

	@NotInCopy
	@SearchableProperty(index = Index.NO, store = Store.YES)
	private Date createDate;

	@NotInCopy
	@SearchableProperty(index = Index.NO, store = Store.YES)
	private Date modifyDate;

	@NotInCopy
	private String createUserAsString;

	@NotInCopy
	private String modifyUserAsString;

	@NotInCopy
	@NotInJson
	@SearchableProperty(index = Index.NOT_ANALYZED)
	private Set<String> tags = new LinkedHashSet<String>(0);

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	@NotInCopy
	public String getTagsAsString() {
		if (tags.size() > 0)
			return StringUtils.join(tags.iterator(), ',');
		return null;
	}

	public void setTagsAsString(String tagsAsString) {
		tags.clear();
		if (StringUtils.isNotBlank(tagsAsString))
			tags.addAll(Arrays.asList(tagsAsString.split(",")));
	}

	@Override
	@NotInCopy
	public UserDetails getCreateUser() {
		return null;
	}

	@Override
	public void setCreateUser(UserDetails user) {
		createUserAsString = user.getUsername();
	}

	@Override
	@NotInCopy
	public UserDetails getModifyUser() {
		return null;
	}

	@Override
	public void setModifyUser(UserDetails user) {
		modifyUserAsString = user.getUsername();
	}

	public String getCreateUserAsString() {
		return createUserAsString;
	}

	public void setCreateUserAsString(String createUserAsString) {
		this.createUserAsString = createUserAsString;
	}

	public String getModifyUserAsString() {
		return modifyUserAsString;
	}

	public void setModifyUserAsString(String modifyUserAsString) {
		this.modifyUserAsString = modifyUserAsString;
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
		return this.path;
	}
}
