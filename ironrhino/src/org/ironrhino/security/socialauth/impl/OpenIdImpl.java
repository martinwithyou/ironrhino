package org.ironrhino.security.socialauth.impl;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.ironrhino.security.socialauth.Profile;
import org.ironrhino.security.socialauth.impl.openid4java.SessionConsumerAssociationStore;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

@Named
@Singleton
public class OpenIdImpl extends AbstractAuthProvider {

	private ConsumerManager manager;
	private DiscoveryInformation discovered;

	public OpenIdImpl() throws ConsumerException {
		manager = new ConsumerManager();
		manager.setAssociations(new SessionConsumerAssociationStore());
		discovered = null;
	}

	public String getLoginRedirectURL(HttpServletRequest request, String returnToURL)
			throws IOException {
		try {
			List discoveries = manager.discover(request.getParameter("id"));

			discovered = manager.associate(discoveries);

			AuthRequest authReq = manager.authenticate(discovered, returnToURL);

			FetchRequest fetch = FetchRequest.createFetchRequest();

			fetch.addAttribute("emailax", "http://axschema.org/contact/email",
					true);

			fetch.addAttribute("firstnameax",
					"http://axschema.org/namePerson/first", true);

			fetch.addAttribute("lastnameax",
					"http://axschema.org/namePerson/last", true);

			fetch.addAttribute("fullnameax", "http://axschema.org/namePerson",
					true);

			fetch.addAttribute("email",
					"http://schema.openid.net/contact/email", true);

			fetch.addAttribute("firstname",
					"http://schema.openid.net/namePerson/first", true);

			fetch.addAttribute("lastname",
					"http://schema.openid.net/namePerson/last", true);

			fetch.addAttribute("fullname",
					"http://schema.openid.net/namePerson", true);

			authReq.addExtension(fetch);

			return authReq.getDestinationUrl(true);
		} catch (OpenIDException e) {
		}

		return null;
	}

	public Profile getProfile(final HttpServletRequest httpReq)
			throws Exception {

		try {

			ParameterList response = new ParameterList(httpReq
					.getParameterMap());

			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0) {
				receivingURL.append("?").append(httpReq.getQueryString());
			}

			VerificationResult verification = manager.verify(receivingURL
					.toString(), response, discovered);

			Identifier verified = verification.getVerifiedId();
			if (verified != null) {
				Profile p = new Profile();
				p.setId(verified.getIdentifier());
				AuthSuccess authSuccess = (AuthSuccess) verification
						.getAuthResponse();

				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					FetchResponse fetchResp = (FetchResponse) authSuccess
							.getExtension(AxMessage.OPENID_NS_AX);

					p.setEmail(fetchResp.getAttributeValue("email"));
					p.setName(fetchResp.getAttributeValue("fullname"));

					if (p.getEmail() == null) {
						p.setEmail(fetchResp.getAttributeValue("emailax"));
					}
					if (p.getName() == null) {
						p.setName(fetchResp.getAttributeValue("fullnameax"));
					}

				}

				return p;
			}
		} catch (OpenIDException e) {
			throw e;
		}

		return null;
	}

}
