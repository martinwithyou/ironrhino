package org.ironrhino.core.monitor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;

public class Value implements Serializable {

	private final AtomicLong longValue;

	private double doubleValue;

	public Value(long c) {
		longValue = new AtomicLong(c);
	}

	public Value(double d) {
		longValue = new AtomicLong(0);
		doubleValue = d;
	}

	public Value(long c, double d) {
		longValue = new AtomicLong(c);
		doubleValue = d;
	}

	public Number[] add(long c, double d) {
		c = addLong(c);
		d = addDouble(d);
		return new Number[] { c, d };
	}

	public long addLong(long value) {
		return longValue.addAndGet(value);
	}

	public long getLong() {
		return longValue.get();
	}

	public double addDouble(double value) {
		return doubleValue += value;
	}

	public double getDouble() {
		return doubleValue;
	}

	public String toString() {
		return String.valueOf(getLong()) + "," + String.valueOf(getDouble());
	}

	public static Value fromString(String s) {
		if (StringUtils.isBlank(s))
			return null;
		String[] array = s.split(",");
		return new Value(Long.valueOf(array[0]), Double.valueOf(array[1]));
	}
}
