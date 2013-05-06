package org.ironrhino.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class XStreamUtilsTest {

	@XStreamAlias("user")
	public static class User implements Serializable {

		private static final long serialVersionUID = -7632092470064636390L;
		private String username;
		private String password;
		private boolean enabled;
		private Date createDate;
		private List<Name> names;

		public User() {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MILLISECOND, 0);
			createDate = cal.getTime();
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Date getCreateDate() {
			return createDate;
		}

		public void setCreateDate(Date createDate) {
			this.createDate = createDate;
		}

		public List<Name> getNames() {
			return names;
		}

		public void setNames(List<Name> names) {
			this.names = names;
		}

	}

	@XStreamAlias("name")
	public static class Name implements Serializable {

		private static final long serialVersionUID = 2600540506012485655L;
		private String first;
		private String last;

		public String getFirst() {
			return first;
		}

		public void setFirst(String first) {
			this.first = first;
		}

		public String getLast() {
			return last;
		}

		public void setLast(String last) {
			this.last = last;
		}

		public boolean equals(Object o) {
			return EqualsBuilder.reflectionEquals(this, o, false);
		}

		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this, false);
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

	@Test
	public void test() {
		User user = new User();
		user.setUsername("test");
		user.setPassword("password");
		List<Name> names = new ArrayList<Name>();
		user.setNames(names);
		Name name = new Name();
		name.setFirst("hello");
		name.setLast("world");
		names.add(name);
		String xml = XStreamUtils.toXml(user);
		assertTrue(xml.startsWith("<user>"));
		User u = XStreamUtils.fromXml(xml, User.class);
		assertEquals(user.getUsername(), u.getUsername());
		assertEquals(user.getPassword(), u.getPassword());
		assertEquals(user.isEnabled(), u.isEnabled());
		assertEquals(user.getCreateDate(), u.getCreateDate());
		assertEquals(user.getNames(), u.getNames());
	}

}
