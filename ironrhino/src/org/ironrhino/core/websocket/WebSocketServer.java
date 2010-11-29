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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

@Singleton
@Named
public class WebSocketServer {
	public static final int DEFAULT_PORT = 9080;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Value("${webSocketServer.port:" + DEFAULT_PORT + "}")
	private int port = DEFAULT_PORT;
	@Value("${webSocketServer.timeout:0}")
	private int timeout;
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private boolean running;
	private Map<String, WebSocketHandler> mapping = new HashMap<String, WebSocketHandler>();
	@Inject
	private ApplicationContext ctx;
	private Thread serverThread;

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

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public boolean isClosed() {
		return serverSocket == null || serverSocket.isClosed();
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

	@PostConstruct
	public void start() {
		if (running)
			throw new RuntimeException("already started");
		if (ctx != null)
			for (WebSocketHandler handler : ctx.getBeansOfType(
					WebSocketHandler.class).values())
				addHandler(handler);
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
		} else {
			return;
		}
		if (executorService == null)
			executorService = Executors.newCachedThreadPool();
		try {
			if (serverSocket == null) {
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(timeout);
			}
			running = true;
			serverThread = new Thread(getClass().getSimpleName()) {
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
											// e.printStackTrace();
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
							// e.printStackTrace();
						}
					}
				}
			};
			serverThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void close() throws IOException {
		if (!running)
			return;
		running = false;
		for (WebSocketHandler handler : mapping.values())
			for (WebSocket ws : handler.getWebSockets())
				ws.close();
		serverThread.interrupt();
		executorService.shutdownNow();
		serverSocket.close();
	}

}
