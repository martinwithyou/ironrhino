package org.ironrhino.common.util;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.common.model.Region;

public class RegionUtils {

	public static Region parseByHost(String host, Region regionTree) {
		// localhost 127.0.0.1
		Location loc = LocationParser.parse(host);
		if (loc == null)
			return null;
		Region firstRegion = getChildOrSelfByName(regionTree,
				loc.getFirstArea());
		if (firstRegion == null)
			return null;
		Region secondRegion = getChildOrSelfByName(firstRegion,
				loc.getSecondArea());
		if (secondRegion == null)
			return firstRegion;
		Region thirdRegion = getChildOrSelfByName(secondRegion,
				loc.getThirdArea());
		if (thirdRegion == null)
			return secondRegion;
		else
			return thirdRegion;
	}

	public static Region getChildOrSelfByName(Region parent, String name) {
		if (StringUtils.isBlank(name))
			return null;
		if (parent.getName().startsWith(name))
			return parent;
		for (Region r : parent.getChildren())
			if (r.getName().startsWith(name))
				return r;
		return null;
	}

	public static Region parseByAddress(String address, Region regionTree) {
		// 广东省深圳市福田区
		if (StringUtils.isBlank(address) || regionTree == null)
			return null;
		Region region = null;
		for (Region r : regionTree.getChildren())
			if (address.startsWith(r.getName())) {
				address = address.substring(r.getName().length());
				if (address.length() == 0)
					return r;
				region = r;
				break;
			}
		if (region == null || region.getId() < 1)
			return null;
		if (region.getChildren().size() > 0)
			return parseByAddress(address, region);
		return region;
	}

}
