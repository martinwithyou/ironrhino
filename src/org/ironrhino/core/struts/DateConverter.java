package org.ironrhino.core.struts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

public class DateConverter extends StrutsTypeConverter {
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	public DateFormat[] ACCEPT_DATE_FORMATS = {
			new SimpleDateFormat(DEFAULT_DATE_FORMAT),
			new SimpleDateFormat("yyyy/MM/dd"),
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") };

	@Override
	public Object convertFromString(Map context, String[] values, Class toClass) {
		if (values[0] == null || values[0].trim().equals(""))
			return null;
		for (DateFormat format : ACCEPT_DATE_FORMATS) {
			try {
				return format.parse(values[0]);
			} catch (ParseException e) {
				continue;
			} catch (RuntimeException e) {
				continue;
			}
		}
		return null;
	}

	@Override
	public String convertToString(Map arg0, Object o) {
		if (o instanceof Date) {
			SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
			try {
				return format.format((Date) o);
			} catch (RuntimeException e) {
				return "";
			}
		}
		return "";
	}

}
