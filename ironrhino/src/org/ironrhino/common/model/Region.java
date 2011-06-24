package org.ironrhino.common.model;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.common.util.RegionUtils;
import org.ironrhino.core.aop.PublishAware;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseTreeableEntity;

@PublishAware
@AutoConfig
@Searchable(alias = "region")
public class Region extends BaseTreeableEntity<Region> {

	private static final long serialVersionUID = 8878381261391688086L;

	private Coordinate coordinate;

	public Region() {

	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public Region(String name) {
		this.name = name;
	}

	public Region(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	@Override
	@SearchableProperty(boost = 2)
	public String getFullname() {
		return super.getFullname();
	}

	@NotInJson
	public String getShortFullname() {
		return RegionUtils.shortenAddress(getFullname());
	}

	@Override
	public String toString() {
		return getFullname();
	}

}
