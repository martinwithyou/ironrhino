package org.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.annotation.RecordAware;
import org.ironrhino.core.model.BaseEntity;

@RecordAware
public class Announcement extends BaseEntity {

	private String title;

	private String content;

	private boolean released;

	private boolean sticky;

	private Date releaseDate;

	public Announcement() {
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
