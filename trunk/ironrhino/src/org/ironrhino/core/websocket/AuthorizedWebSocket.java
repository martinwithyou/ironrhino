package org.ironrhino.core.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.util.AuthzUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthorizedWebSocket {

	public static final String USER_PROPERTIES_NAME_USER = "user";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private final Set<Session> sessions = Collections
			.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());

	protected boolean authorize(UserDetails user) {
		return true;
	}

	public void broadcast(String message, String... roles) {
		for (Session s : sessions)
			if (s.isOpen())
				try {
					if (roles.length == 0
							|| AuthzUtils.authorizeUserDetails(
									(UserDetails) s.getUserProperties().get(
											USER_PROPERTIES_NAME_USER), null,
									StringUtils.join(roles, ","), null))
						s.getBasicRemote().sendText(message);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
	}

	@OnMessage
	public void onMessage(Session session, String message) {
		logger.info("received from {} : {}",
				session.getUserProperties().get(USER_PROPERTIES_NAME_USER),
				message);
	}

	@OnOpen
	public void onOpen(Session session) {
		UserDetails user = null;
		Object principal = session.getUserPrincipal();
		if (principal instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken upat = (UsernamePasswordAuthenticationToken) principal;
			principal = upat.getPrincipal();
			if (principal instanceof UserDetails)
				user = (UserDetails) principal;
		}
		if (user == null || !authorize(user)) {
			try {
				session.getBasicRemote().sendText("access.denied");
				session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE,
						"access.denied"));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			session.getUserProperties().put(USER_PROPERTIES_NAME_USER, user);
			sessions.add(session);
		}
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

	@OnError
	public void onError(Session session, Throwable err) {
		sessions.remove(session);
		try {
			session.close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, err
					.getMessage()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}