package org.ironrhino.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Region;
import org.ironrhino.core.util.Location;
import org.ironrhino.core.util.LocationParser;

public class RegionUtils {

	public static final String[] nations = "满族,蒙古族,回族,朝鲜族,达斡尔族,畲族,土家族,苗族,侗族,瑶族,壮族,各族,仫佬族,毛南族,黎族,羌族,彝族,藏族,仡佬族,布依族,水族,傣族,哈尼族,纳西族,傈僳族,拉祜族,佤族,白族,景颇族,独龙族,普米族,布朗族,哈萨克族,东乡族,裕固族,土族,撒拉族"
			.split(",");

	public static final Map<String, String> mapping = new HashMap<String, String>() {
		{
			put("广西壮族自治区", "广西");
			put("西藏自治区", "西藏");
			put("宁夏回族自治区", "宁夏");
			put("新疆维吾尔自治区", "新疆");
			put("内蒙古自治区", "内蒙古");
			put("东乡族自治县", "东乡县");
			put("鄂温克族自治旗", "鄂温克旗");
		}
	};

	public static final String[] suffix = "县,市,州,省,矿区,新区,地区,区".split(",");

	public static String shortenAddress(String address) {
		for (Map.Entry<String, String> entry : mapping.entrySet())
			address = address.replace(entry.getKey(), entry.getValue());
		while (address.indexOf('族') > 0)
			for (String nation : nations)
				address = address.replace(nation, "");
		address = address.replaceAll("自治", "");
		return address;
	}

	public static String shortenName(String name) {
		if (name.length() < 3)
			return name;
		for (Map.Entry<String, String> entry : mapping.entrySet())
			name = name.replace(entry.getKey(), entry.getValue());
		while (name.indexOf('族') > 0)
			for (String nation : nations)
				name = name.replace(nation, "");
		name = name.replaceAll("自治", "");
		for (String s : suffix) {
			if (name.endsWith(s)) {
				name = name.substring(0, name.length() - s.length());
				return name;
			}
		}
		return name;
	}

	public static Region parseByHost(String host, Region regionTree) {
		// localhost 127.0.0.1
		Location loc = LocationParser.parse(host);
		if (loc == null)
			return null;
		Region firstRegion = getChildOrSelfByName(regionTree, loc
				.getFirstArea());
		if (firstRegion == null)
			return null;
		Region secondRegion = getChildOrSelfByName(firstRegion, loc
				.getSecondArea());
		if (secondRegion == null)
			return firstRegion;
		Region thirdRegion = getChildOrSelfByName(secondRegion, loc
				.getThirdArea());
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
