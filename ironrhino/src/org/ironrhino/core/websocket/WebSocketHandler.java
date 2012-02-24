package org.ironrhino.core.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

public abstract class WebSocketHandler {

	private ThreadLocal<WebSocket> _webSocket = new ThreadLocal<WebSocket>();

	private Collection<WebSocket> webSockets = new HashSet<WebSocket>();

	protected void setWebSocket(WebSocket webSocket) {
		this._webSocket.set(webSocket);
		webSockets.add(webSocket);
	}

	protected void close() throws IOException {
		WebSocket ws = _webSocket.get();
		_webSocket.remove();
		if (ws != null) {
			webSockets.remove(ws);
			ws.close();
		}
	}

	public WebSocket getWebSocket() {
		return _webSocket.get();
	}

	public Collection<WebSocket> getWebSockets() {
		return webSockets;
	}

	public abstract void handle() throws IOException;

	public String getDefaultPattern() {
		String name = getClass().getSimpleName();
		if (name.endsWith("Handler"))
			name = name.substring(0, name.length() - "Handler".length());
		return "/" + StringUtils.uncapitalize(name);
	}

}
