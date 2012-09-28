package org.ironrhino.common.model;

import org.ironrhino.common.util.RegionUtils;
import org.ironrhino.core.aop.PublishAware;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.search.elasticsearch.annotations.Index;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;
import org.ironrhino.core.util.StringUtils;

@PublishAware
@AutoConfig
@Searchable(type = "region")
public class Region extends BaseTreeableEntity<Region> {

	private static final long serialVersionUID = 8878381261391688086L;

	private String areacode;

	private String postcode;

	private Coordinate coordinate;

	public Region() {

	}

	public String getAreacode() {
		return areacode;
	}

	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
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

	@SearchableProperty(boost = 3, index = Index.NOT_ANALYZED)
	public String getNameAsPinyin() {
		return StringUtils.pinyin(name);
	}

	@SearchableProperty(boost = 3, index = Index.NOT_ANALYZED)
	public String getNameAsPinyinAbbr() {
		return StringUtils.pinyinAbbr(name);
	}

	@NotInJson
	public String getShortFullname() {
		return RegionUtils.shortenAddress(getFullname());
	}

	public Region getDescendantOrSelfByAreacode(String areacode) {
		if (areacode == null)
			throw new IllegalArgumentException("areacode must not be null");
		if (areacode.equals(this.getAreacode()))
			return this;
		for (Region t : getChildren()) {
			if (areacode.equals(t.getAreacode())) {
				return t;
			} else {
				Region tt = t.getDescendantOrSelfByAreacode(areacode);
				if (tt != null)
					return tt;
			}
		}
		return null;
	}

}
