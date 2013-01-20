package org.ironrhino.daq.model;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.security.model.UserRole;

@AutoConfig(order = "time desc,type")
@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
@Entity
@Table(name = "acquisition")
public class Acquisition extends BaseEntity {

	private static final long serialVersionUID = -8040786622424934566L;

	@UiConfig(displayOrder = 1)
	@Column(length = 50)
	@Access(AccessType.FIELD)
	private String type;

	@UiConfig(displayOrder = 2)
	@Column(length = 50)
	@Access(AccessType.FIELD)
	private String place;

	@UiConfig(displayOrder = 3)
	private Date time;

	@UiConfig(displayOrder = 4)
	private double value;

	@UiConfig(displayOrder = 5)
	@NotInJson
	private String ip;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
