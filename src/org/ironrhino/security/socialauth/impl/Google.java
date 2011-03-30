package org.ironrhino.security.socialauth.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.compass.core.util.reader.StringReader;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.util.XmlUtils;
import org.ironrhino.security.socialauth.Profile;
import org.ironrhino.security.socialauth.Profile.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Named("google")
@Singleton
public class Google extends OAuth20Provider {

	@Value("${google.logo:http://www.google.com/images/logos/accounts_logo.gif}")
	private String logo;

	@Value("${google.authorizeUrl:https://accounts.google.com/o/oauth2/auth}")
	private String authorizeUrl;

	@Value("${google.accessTokenEndpoint:https://accounts.google.com/o/oauth2/token}")
	private String accessTokenEndpoint;

	private String scope = "https://www-opensocial.googleusercontent.com/api/people/ https://www.google.com/m8/feeds/";

	@Override
	public String getLogo() {
		return logo;
	}

	@Override
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return accessTokenEndpoint;
	}

	@Override
	public String getScope() {
		return scope;
	}

	public String getAccessKey() {
		return settingControl.getStringValue("socialauth." + getName()
				+ ".accessKey");
	}

	@Override
	public boolean isUseAuthorizationHeader() {
		return true;
	}

	@Override
	protected Profile doGetProfile(String token) throws Exception {
		String content = invoke(token,
				"http://www-opensocial.googleusercontent.com/api/people/%40me/%40self");
		JsonNode data = JsonUtils.getObjectMapper().readValue(content,
				JsonNode.class);
		JsonNode entry = data.get("entry");
		String uid = entry.get("id").getTextValue();
		String displayName = entry.get("displayName").getTextValue();
		Profile p = new Profile();
		p.setId(generateId(uid));
		p.setDisplayName(displayName);
		// p.setLocation(data.get("location").getTextValue());
		p.setImage(entry.get("thumbnailUrl").getTextValue());

		content = invoke(token,
				"https://www.google.com/m8/feeds/contacts/default/full");
		Document doc = XmlUtils.getDocumentBuilder().parse(
				new InputSource(new StringReader(content)));
		String name = XmlUtils.eval("/feed/author/name", doc);
		p.setName(name);
		String email = XmlUtils.eval("/feed/author/email", doc);
		p.setEmail(email);
		NodeList nodelist = XmlUtils.evalNodeList("/feed/entry", doc);
		if (nodelist != null) {
			p.setContacts(new ArrayList<Contact>(nodelist.getLength()));
			for (int i = 0; i < nodelist.getLength(); i++) {
				Contact c = new Contact();
				p.getContacts().add(c);
				Element ele = (Element) nodelist.item(i);
				c.setName(((Element) ele.getElementsByTagName("title").item(0))
						.getTextContent());
				c.setEmail(((Element) ele.getElementsByTagName("gd:email")
						.item(0)).getAttribute("address"));
			}
		}
		return p;
	}

	@Override
	protected String invoke(String protectedURL, Map<String, String> params,
			Map<String, String> headers) {
		if (params == null)
			params = new HashMap<String, String>();
		params.put("key", getAccessKey());
		return super.invoke(protectedURL, params, headers);
	}

}
