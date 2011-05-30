package org.ironrhino.common.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Coordinate implements Serializable {

	private static final long serialVersionUID = 5828814302557010566L;

	private Double latitude;

	private Double longitude;

	public Coordinate() {

	}

	public Coordinate(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(String latlong) {
		String[] arr = latlong.split(",");
		this.latitude = parseLatLong(arr[0]);
		this.longitude = parseLatLong(arr[1]);
	}

	public Coordinate(String latitude, String longitude) {
		this.latitude = parseLatLong(latitude);
		this.longitude = parseLatLong(longitude);
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getLatitudeAsString() {
		return String.valueOf(latitude);
	}

	public void setLatitudeAsString(String latitude) {
		this.latitude = parseLatLong(latitude);
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getLongitudeAsString() {
		return String.valueOf(longitude);
	}

	public void setLongitudeAsString(String longitude) {
		this.longitude = parseLatLong(longitude);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return latitude + "," + longitude;
	}

	public static Double parseLatLong(String input) {
		try {
			return Double.valueOf(input);
		} catch (Exception e) {
			int i = input.indexOf('°');
			double d = Double.valueOf(input.substring(0, i));
			input = input.substring(i + 1);
			i = input.indexOf('\'');
			if (i > 0) {
				d += Double.valueOf(input.substring(0, i)) / 60;
				input = input.substring(i + 1);
				i = input.indexOf('"');
				if (i > 0) {
					d += Double.valueOf(input.substring(0, i)) / (60 * 60);
					input = input.substring(i + 1).trim().toUpperCase();
					if (input.equals("S") || input.equals("W"))
						d = 0 - d;
				}
			}
			return d;

		}
	}

}
