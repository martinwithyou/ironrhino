package org.ironrhino.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.spring.security.DefaultGrantedAuthority;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

public class SerializationUtilsTest {

	static enum Status {
		ACTIVE, DISABLED;
		public String toString() {
			return name().toLowerCase();
		}
	}

	static class User implements Persistable<String> {

		private static final long serialVersionUID = 3491371316959760638L;
		private String id;
		private String username;
		@NotInJson
		private transient String password;
		private int age;
		private Status status;
		private User createUser;
		private transient User modifyUser;
		private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		private Date date = DateUtils.beginOfDay(new Date());

		public boolean isNew() {
			return id == null;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

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

		public List<GrantedAuthority> getAuthorities() {
			return authorities;
		}

		public void setAuthorities(List<GrantedAuthority> authorities) {
			this.authorities = authorities;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + age;
			result = prime * result
					+ ((createUser == null) ? 0 : createUser.hashCode());
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result
					+ ((status == null) ? 0 : status.hashCode());
			result = prime * result
					+ ((username == null) ? 0 : username.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			User other = (User) obj;
			if (age != other.age)
				return false;
			if (createUser == null) {
				if (other.createUser != null)
					return false;
			} else if (!createUser.equals(other.createUser))
				return false;
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (status != other.status)
				return false;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			return true;
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
		User user = (User) SerializationUtils.deserialize(bytes);
		assertNull(user.getPassword());
		assertEquals(u.getUsername(), user.getUsername());
		assertEquals(u.getStatus(), user.getStatus());
		assertEquals(u.getDate(), user.getDate());
		assertNull(user.getCreateUser().getPassword());
		assertEquals(createUser.getUsername(), user.getCreateUser()
				.getUsername());
		assertEquals(createUser.getStatus(), user.getCreateUser().getStatus());
		assertNull(user.getModifyUser());

		u.getAuthorities().add(new DefaultGrantedAuthority("test"));
		EntityOperationEvent event = new EntityOperationEvent(u,
				EntityOperationType.CREATE);
		bytes = SerializationUtils.serialize(event);
		EntityOperationEvent event2 = (EntityOperationEvent) SerializationUtils
				.deserialize(bytes);
		assertEquals(event.getTimestamp(), event2.getTimestamp());
		assertEquals(event.getInstanceId(), event2.getInstanceId());
		assertEquals(event.getType(), event2.getType());
		assertEquals(event.getEntity(), event2.getEntity());
		assertEquals(u.getAuthorities().get(0).getAuthority(),
				((User) event2.getEntity()).getAuthorities().get(0)
						.getAuthority());
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
