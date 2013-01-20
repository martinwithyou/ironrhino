package org.ironrhino.core.websocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
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
		if (mapping.isEmpty())
			return;
		if (mapping.size() > 0) {
			// sort mapping by uri
			Map<String, WebSocketHandler> sortedMap = new TreeMap<String, WebSocketHandler>(
					new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							int i = o2.split("/").length - o1.split("/").length;
							return i != 0 ? i : o1.compareTo(o2);
						}
					});
			sortedMap.putAll(mapping);
			mapping = sortedMap;
			for (Map.Entry<String, WebSocketHandler> entry : mapping.entrySet()) {
				logger.info("mapping {} to {}", entry.getValue().getClass()
						.getName(), entry.getKey());
			}
		}
		if (executorService == null)
			executorService = Executors.newFixedThreadPool(2);
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
							final WebSocket ws = new WebSocket(
									serverSocket.accept(), timeout);
							String requestUri = ws.getRequestUri();
							WebSocketHandler temp = null;
							for (Map.Entry<String, WebSocketHandler> entry : mapping
									.entrySet())
								if (org.ironrhino.core.util.StringUtils
										.matchesWildcard(requestUri,
												entry.getKey())) {
									temp = entry.getValue();
									break;
								}
							final WebSocketHandler handler = temp;
							if (handler != null)
								executorService.execute(new Runnable() {
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
