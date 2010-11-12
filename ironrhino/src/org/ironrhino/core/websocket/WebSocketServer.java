package org.ironrhino.core.websocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.struts.AutoConfigPackageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServer {

	private final Logger logger = LoggerFactory
			.getLogger(AutoConfigPackageProvider.class);

	private int port = 8080;
	private int timeout;
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private boolean running;
	private Map<String, WebSocketHandler> mapping = new HashMap<String, WebSocketHandler>();

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isClosed() {
		return serverSocket == null || serverSocket.isClosed();
	}

	public void close() throws IOException {
		running = false;
		for (WebSocketHandler handler : mapping.values())
			for (WebSocket ws : handler.getWebSockets())
				ws.close();
		executorService.shutdownNow();
		serverSocket.close();
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public void addHandler(WebSocketHandler handler) {
		addHandler(handler.getDefaultPattern(), handler);
	}

	public void addHandler(String pattern, WebSocketHandler handler) {
		if (StringUtils.isBlank(pattern))
			pattern = handler.getDefaultPattern();
		if (StringUtils.isNotBlank(pattern)) {
			if (!pattern.startsWith("/"))
				pattern = "/" + pattern;
			mapping.put(pattern, handler);
		}
	}

	public void start() {
		if (running)
			throw new RuntimeException("already started");
		if (mapping.size() > 0) {
			// sort mapping by uri
			List<Map.Entry<String, WebSocketHandler>> list = new ArrayList<Map.Entry<String, WebSocketHandler>>();
			list.addAll(mapping.entrySet());
			Collections.sort(list,
					new Comparator<Map.Entry<String, WebSocketHandler>>() {
						@Override
						public int compare(Entry<String, WebSocketHandler> o1,
								Entry<String, WebSocketHandler> o2) {
							return o2.getKey().split("/").length
									- o1.getKey().split("/").length;
						}
					});
			mapping = new LinkedHashMap<String, WebSocketHandler>();
			for (Map.Entry<String, WebSocketHandler> entry : list) {
				mapping.put(entry.getKey(), entry.getValue());
				logger.info("mapping {} to {}", entry.getValue().getClass()
						.getName(), entry.getKey());
			}
		}
		if (executorService == null)
			executorService = Executors.newCachedThreadPool();
		try {
			if (serverSocket == null) {
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(timeout);
			}
			running = true;
			new Thread() {
				public void run() {
					while (running) {
						try {
							final WebSocket ws = new WebSocket(serverSocket
									.accept(), timeout);
							String requestUri = ws.getRequestUri();
							WebSocketHandler temp = null;
							for (Map.Entry<String, WebSocketHandler> entry : mapping
									.entrySet())
								if (org.ironrhino.core.util.StringUtils
										.matchesWildcard(requestUri, entry
												.getKey())) {
									temp = entry.getValue();
									break;
								}
							final WebSocketHandler handler = temp;
							if (handler != null)
								executorService.submit(new Runnable() {
									public void run() {
										handler.setWebSocket(ws);
										try {
											handler.handle();
										} catch (IOException e) {
											 e.printStackTrace();
										} finally {
											try {
												handler.close();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}
								});
							else {
								try {
									ws.close();
								} catch (IOException e) {
								}
							}
						} catch (Exception e) {
							 e.printStackTrace();
						}
					}
				}
			}.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
