package org.ironrhino.common.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.annotation.NotInJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonUtils {

	private static Gson gson = new GsonBuilder().excludeFieldsWithAnnotations(
			NotInJson.class).setDateFormat("yyyy-MM-dd HH:mm:ss").create();

	public static String toJson(Object src) {
		return gson.toJson(src);
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

	public static <T> T fromJson(String json, Type type) {
		return gson.fromJson(json, type);
	}

	/**
	 * 
	 * @param <T>
	 * @param json
	 * @param clazz
	 *            support
	 *            Short,Integer,Long,Float,Double,BigDecimal,Boolean,Date
	 *            ,default String
	 * @return
	 */
	public static <T> List<T> listFromJson(String json, Class<T> clazz) {
		Type type;
		if (Short.class.equals(clazz))
			type = new TypeToken<List<Short>>() {
			}.getType();
		else if (Integer.class.equals(clazz))
			type = new TypeToken<List<Integer>>() {
			}.getType();
		else if (Long.class.equals(clazz))
			type = new TypeToken<List<Long>>() {
			}.getType();
		else if (Float.class.equals(clazz))
			type = new TypeToken<List<Float>>() {
			}.getType();
		else if (Double.class.equals(clazz))
			type = new TypeToken<List<Double>>() {
			}.getType();
		else if (BigDecimal.class.equals(clazz))
			type = new TypeToken<List<BigDecimal>>() {
			}.getType();
		else if (Boolean.class.equals(clazz))
			type = new TypeToken<List<Boolean>>() {
			}.getType();
		else if (Date.class.equals(clazz))
			type = new TypeToken<List<Date>>() {
			}.getType();
		else
			type = new TypeToken<List<String>>() {
			}.getType();
		return gson.fromJson(json, type);
	}

	/**
	 * keyClass is String
	 * 
	 * @param <T>
	 * @param json
	 * @param valueClass
	 *            support
	 *            Short,Integer,Long,Float,Double,BigDecimal,Boolean,Date
	 *            ,default String
	 * @return
	 */
	public static <T> Map<String, T> mapFromJson(String json,
			Class<T> valueClass) {
		Type type;
		if (Short.class.equals(valueClass))
			type = new TypeToken<Map<String, Short>>() {
			}.getType();
		else if (Integer.class.equals(valueClass))
			type = new TypeToken<Map<String, Integer>>() {
			}.getType();
		else if (Long.class.equals(valueClass))
			type = new TypeToken<Map<String, Long>>() {
			}.getType();
		else if (Float.class.equals(valueClass))
			type = new TypeToken<Map<String, Float>>() {
			}.getType();
		else if (Double.class.equals(valueClass))
			type = new TypeToken<Map<String, Double>>() {
			}.getType();
		else if (BigDecimal.class.equals(valueClass))
			type = new TypeToken<Map<String, BigDecimal>>() {
			}.getType();
		else if (Boolean.class.equals(valueClass))
			type = new TypeToken<Map<String, Boolean>>() {
			}.getType();
		else if (Date.class.equals(valueClass))
			type = new TypeToken<Map<String, Date>>() {
			}.getType();
		else
			type = new TypeToken<Map<String, String>>() {
			}.getType();
		return gson.fromJson(json, type);
	}

}