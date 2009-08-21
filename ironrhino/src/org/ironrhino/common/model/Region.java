package org.ironrhino.common.model;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.model.BaseTreeableEntity;

@PublishAware
@AutoConfig
public class Region extends BaseTreeableEntity<Region> {

	private static final long serialVersionUID = 8878381261391688086L;

	private Double latitude;

	private Double longitude;

	public Region() {

	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Region(String name) {
		this.name = name;
	}

	public Region(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	@Override
	public String toString() {
		return getFullname();
	}

}
