package org.ironrhino.common.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ironrhino.common.model.Coordinate;

public class LocationUtils {

	public static final String[] nations = "满族,蒙古族,回族,朝鲜族,达斡尔族,畲族,土家族,苗族,侗族,瑶族,壮族,各族,仫佬族,毛南族,黎族,羌族,彝族,藏族,仡佬族,布依族,水族,傣族,哈尼族,纳西族,傈僳族,拉祜族,佤族,白族,景颇族,独龙族,怒族,普米族,布朗族,哈萨克族,东乡族,裕固族,土族,保安族,撒拉族"
			.split(",");

	public static final Map<String, String> mapping = new HashMap<String, String>() {
		private static final long serialVersionUID = 1843445431842190721L;

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

	public static final List<String> sameNames = Arrays
			.asList("邯郸县,邢台县,承德县,大同县,长治县,抚顺县,辽阳县,铁岭县,朝阳县,吉林市,通化县,伊春区,绍兴县,芜湖县,铜陵县,黄山区,南昌县,九江县,上饶县,吉安县,东营区,开封县,安阳县,新乡县,濮阳县,许昌县,荆州区,长沙县,株洲县,湘潭县,衡阳县,岳阳县,邵阳县,宜宾县,广安区,遵义县,白银区,乌鲁木齐县,克拉玛依区"
					.split(","));

	public static final String[] suffix = "县,市,州,省,特别行政区,矿区,新区,地区,区".split(",");

	public static String shortenAddress(String address) {
		for (Map.Entry<String, String> entry : mapping.entrySet())
			address = address.replace(entry.getKey(), entry.getValue());
		int loop = 0;
		while (address.indexOf('族') > 0 && loop < 5) {
			for (String nation : nations)
				address = address.replace(nation, "");
			loop++;
		}
		address = address.replaceAll("自治", "");
		address = address.replaceAll("特别行政区", "");
		return address;
	}

	public static String shortenName(String name) {
		if (name.length() < 3 || sameNames.contains(name))
			return name;
		for (Map.Entry<String, String> entry : mapping.entrySet())
			name = name.replace(entry.getKey(), entry.getValue());
		int loop = 0;
		while (name.indexOf('族') > 0 && loop < 5) {
			for (String nation : nations)
				name = name.replace(nation, "");
			loop++;
		}
		name = name.replaceAll("自治", "");
		for (String s : suffix) {
			if (name.endsWith(s)) {
				name = name.substring(0, name.length() - s.length());
				return name;
			}
		}
		return name;
	}

	private static double earth_radius = 6371000;

	public static long distance(Coordinate c1, Coordinate c2) {
		Double latitude = (c1.getLatitude() - c2.getLatitude()) * Math.PI / 180;
		Double longitude = (c1.getLongitude() - c2.getLongitude()) * Math.PI
				/ 180;
		Double aDouble = Math.sin(latitude / 2) * Math.sin(latitude / 2)
				+ Math.cos(c1.getLatitude() * Math.PI / 180)
				* Math.cos(c2.getLatitude() * Math.PI / 180)
				* Math.sin(longitude / 2) * Math.sin(longitude / 2);
		Double distance = 2 * Math.atan2(Math.sqrt(aDouble),
				Math.sqrt(1 - aDouble));
		return Math.round((earth_radius * distance) * 1000) / 1000;
	}

}
