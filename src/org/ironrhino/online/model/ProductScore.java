package org.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.model.BaseEntity;

public class ProductScore extends BaseEntity {

	private static final long serialVersionUID = 398695275541790536L;

	@NaturalId
	private String username;

	@NaturalId
	private String productCode;

	private int score;

	private Date scoreDate;

	public ProductScore() {
		scoreDate = new Date();
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Date getScoreDate() {
		return scoreDate;
	}

	public void setScoreDate(Date scoreDate) {
		this.scoreDate = scoreDate;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}
