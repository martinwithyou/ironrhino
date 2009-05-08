package org.ironrhino.common.util;

import org.ironrhino.core.annotation.NotInJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

	private static Gson gson = new GsonBuilder().excludeFieldsWithAnnotations(
			NotInJson.class).create();

	public static String toJson(Object src) {
		return gson.toJson(src);
	}

}