package org.ironrhino.common.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.annotation.NotInCopy;
import org.ironrhino.core.annotation.NotInJson;
import org.ironrhino.core.annotation.Publishable;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.model.Secured;

@Publishable
@AutoConfig
public class Region extends BaseTreeableEntity<Region> implements Secured {

	private Set<SimpleElement> roles = new HashSet<SimpleElement>(0);

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

	public String getFullname() {
		String fullname = name;
		Region r = this;
		while ((r = r.getParent()) != null) {
			fullname = r.getName() + fullname;
		}
		return fullname;
	}
	
	public String toString(){
		return getFullname();
	}

	@NotInCopy
	@NotInJson
	public Set<SimpleElement> getRoles() {
		return roles;
	}

	public void setRoles(Set<SimpleElement> roles) {
		this.roles = roles;
	}

	@NotInCopy
	@NotInJson
	public String getRolesAsString() {
		return StringUtils.join(roles.iterator(), ',');
	}

	public void setRolesAsString(String rolesAsString) {
		SimpleElement.fillCollectionWithString(roles, rolesAsString);
	}

}
