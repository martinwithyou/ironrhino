package org.ironrhino.common.model;

import java.util.Date;

import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;

public class Page extends BaseEntity implements Recordable {

	private static final long serialVersionUID = 4688382703803043164L;

	@NaturalId(caseInsensitive = true)
	private String path;

	private String title;

	private String content;

	private String draft;

	private Date draftDate;

	private Date createDate;

	private Date modifyDate;

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

}
