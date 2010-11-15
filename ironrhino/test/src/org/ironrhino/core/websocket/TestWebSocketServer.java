package org.ironrhino.core.websocket;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestWebSocketServer {

	public static void main(String[] args) throws IOException {
		final WebSocketServer server = new WebSocketServer();
		// server.setPort(8080);
		// server.setTimeout(200000);
		server.addHandler("/echo", new WebSocketHandler() {
			public void handle() throws IOException {
				WebSocket ws = getWebSocket();
				while (true) {
					String message = ws.getMessage();
					ws.sendMessage(message);
				}

			}
		});
		server.addHandler(new DatetimeHandler());
		server.addHandler("/chat", new WebSocketHandler() {
			public void handle() throws IOException {
				WebSocket ws = getWebSocket();
				String name = ws.getParameter("name");
				while (true) {
					String message = ws.getMessage();
					for (WebSocket var : getWebSockets())
						var.sendMessage(name + " says : " + message);
				}
			}
		});

		System.out.println("server starting");
		server.start();
		System.out.println("server started");
		System.out.println("server waiting");
		System.in.read();
		System.out.println("server closing");
		server.close();
		System.out.println("server closed");

	}

	public static class DatetimeHandler extends WebSocketHandler {
		public void handle() throws IOException {
			WebSocket ws = getWebSocket();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while (true) {
				ws.sendMessage(df.format(new Date()));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
