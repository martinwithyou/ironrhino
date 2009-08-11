package org.ironrhino.core.monitor;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class Key implements Serializable {

	private String namespace;

	private String[] names;

	// minutes
	private int interval = 1;

	private long lastWriteTime = System.currentTimeMillis();

	public Key(String... strings) {
		namespace = null;
		names = strings;
	}

	public Key(int interval, String... strings) {
		if (interval > 0)
			this.interval = interval;
		namespace = null;
		names = strings;
	}

	public Key(String namespace, int interval, String... strings) {
		if (interval > 0)
			this.interval = interval;
		this.namespace = namespace;
		names = strings;
	}

	public int getInterval() {
		return interval;
	}

	public String[] getNames() {
		return names;
	}

	public String getNamespace() {
		return namespace;
	}

	public Key fork(String subkey) {
		String[] newkeys = new String[this.names.length + 1];
		System.arraycopy(names, 0, newkeys, 0, names.length);
		newkeys[this.names.length] = subkey;
		return new Key(newkeys);
	}

	public long getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		return this.toString().equals(o.toString());
	}

	public String toString() {
		return (StringUtils.isNotBlank(namespace) ? namespace + ":" : "")
				+ StringUtils.join(names, '>');
	}

	public static Key fromString(String s) {
		if (StringUtils.isBlank(s))
			return null;
		String[] array = s.split(":");
		String namespace = null;
		String names = null;
		if (array.length == 1) {
			names = array[0];
		} else {
			namespace = array[0];
			names = array[1];
		}
		return new Key(namespace, 0, names.split(">"));
	}
}
