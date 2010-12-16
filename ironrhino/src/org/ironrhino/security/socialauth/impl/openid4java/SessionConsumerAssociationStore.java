package org.ironrhino.security.socialauth.impl.openid4java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.struts2.ServletActionContext;
import org.openid4java.association.Association;
import org.openid4java.consumer.ConsumerAssociationStore;

public class SessionConsumerAssociationStore implements
		ConsumerAssociationStore {

	public void save(String opUrl, Association association) {
		HttpSession session = ServletActionContext.getRequest().getSession();
		String key = opUrl + association.getHandle();
		try {
			String value = compress(association);
			session.setAttribute(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Association load(String opUrl, String handle) {
		HttpSession session = ServletActionContext.getRequest().getSession();
		String key = opUrl + handle;
		String value = (String) session.getAttribute(key);
		if (value != null)
			try {
				return uncompress(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}

	public Association load(String opUrl) {
		Association latest = null;
		HttpSession session = ServletActionContext.getRequest().getSession();
		Enumeration<String> keys = session.getAttributeNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.startsWith(opUrl)) {
				String value = (String) session.getAttribute(key);
				try {
					Association association = uncompress(value);
					if (latest == null
							|| latest.getExpiry().before(
									association.getExpiry()))
						latest = association;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return latest;
	}

	public void remove(String opUrl, String handle) {
		HttpSession session = ServletActionContext.getRequest().getSession();
		String key = opUrl + handle;
		session.removeAttribute(key);
	}

	private static String compress(Association association) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(association);
		oos.close();
		String s = Base64.encodeBase64String(bos.toByteArray());
		bos.close();
		return s;
	}

	private static Association uncompress(String string) throws Exception {
		byte[] bytes = Base64.decodeBase64(string);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (Association) ois.readObject();
	}

}
