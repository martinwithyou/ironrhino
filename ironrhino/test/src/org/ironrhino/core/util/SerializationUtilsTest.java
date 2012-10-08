package org.ironrhino.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.Date;

import org.ironrhino.core.metadata.NotInJson;
import org.junit.Test;

public class SerializationUtilsTest {

	static enum Status {
		ACTIVE, DISABLED;
		public String toString() {
			return name().toLowerCase();
		}
	}

	static class User implements Serializable {

		private static final long serialVersionUID = 3491371316959760638L;
		private String username;
		@NotInJson
		private transient String password;
		private int age;
		private Status status;
		private User createUser;
		private transient User modifyUser;

		private Date date = DateUtils.beginOfDay(new Date());

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
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

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public User getCreateUser() {
			return createUser;
		}

		public void setCreateUser(User createUser) {
			this.createUser = createUser;
		}

		public User getModifyUser() {
			return modifyUser;
		}

		public void setModifyUser(User modifyUser) {
			this.modifyUser = modifyUser;
		}

	}

	@Test
	public void testSerialize() throws Exception {
		User createUser = new User();
		createUser.setUsername("username");
		createUser.setPassword("password");
		createUser.setStatus(Status.ACTIVE);
		User u = new User();
		u.setUsername("username");
		u.setPassword("password");
		u.setStatus(Status.ACTIVE);
		u.setAge(12);
		u.setCreateUser(createUser);
		u.setModifyUser(createUser);
		byte[] bytes = SerializationUtils.serialize(u);
		User user = (User)SerializationUtils.deserialize(bytes);
		assertNull(user.getPassword());
		assertEquals(u.getUsername(), user.getUsername());
		assertEquals(u.getStatus(), user.getStatus());
		assertEquals(u.getDate(), user.getDate());
		assertNull(user.getCreateUser().getPassword());
		assertEquals(createUser.getUsername(), user.getCreateUser()
				.getUsername());
		assertEquals(createUser.getStatus(), user.getCreateUser().getStatus());
		assertNull(user.getModifyUser());
	}

	@Test
	public void testSerializeObject() throws Exception {
		User createUser = new User();
		createUser.setUsername("username");
		createUser.setPassword("password");
		createUser.setStatus(Status.ACTIVE);
		User u = new User();
		u.setUsername("username");
		u.setPassword("password");
		u.setStatus(Status.ACTIVE);
		u.setAge(12);
		u.setCreateUser(createUser);
		u.setModifyUser(createUser);
		byte[] bytes = SerializationUtils.serializeObject(u);
		User user = SerializationUtils.deserializeObject(bytes, User.class);
		assertNull(user.getPassword());
		assertEquals(u.getUsername(), user.getUsername());
		assertEquals(u.getStatus(), user.getStatus());
		assertEquals(u.getDate(), user.getDate());
		assertNull(user.getCreateUser().getPassword());
		assertEquals(createUser.getUsername(), user.getCreateUser()
				.getUsername());
		assertEquals(createUser.getStatus(), user.getCreateUser().getStatus());
		assertNull(user.getModifyUser());
	}

}
