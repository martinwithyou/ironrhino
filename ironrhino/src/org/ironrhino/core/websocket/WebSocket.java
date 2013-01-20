package org.ironrhino.core.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public final class WebSocket {

	protected Socket socket;
	protected InputStream in;
	protected OutputStream out;

	private class HandshakeRunner implements Runnable {

		private Exception exception = null;

		public Exception getException() {
			return exception;
		}

		public void run() {
			try {
				handshake();
			} catch (Exception e) {
				exception = e;
			}
		}

	}

	public WebSocket(Socket socket) throws IOException {
		this(socket, 0);
	}

	public WebSocket(Socket socket, int timeout) throws IOException {
		this.socket = socket;
		in = socket.getInputStream();
		out = socket.getOutputStream();
		HandshakeRunner taskBody = new HandshakeRunner();

		FutureTask<Object> task = new FutureTask<Object>(taskBody, null);
		try {
			(new Thread(task)).start();
			if (timeout > 0) {
				task.get(timeout, TimeUnit.MILLISECONDS);
			} else {
				task.get();
			}
		} catch (Exception e) {
			socket.close();
			throw new IOException("Handshake failed", e);
		}
		if (taskBody.getException() != null) {
			socket.close();
			throw new IOException("Handshake failed", taskBody.getException());
		}
	}

	protected String byteCollectionToString(Collection<Byte> collection) {
		byte[] byteArray = new byte[collection.size()];
		Integer i = 0;
		for (Iterator<Byte> iterator = collection.iterator(); iterator
				.hasNext();) {
			byteArray[i++] = iterator.next();
		}
		return new String(byteArray, Charset.forName("UTF-8"));
	}

	public void close() throws IOException {
		socket.close();
	}

	public String getMessage() throws IOException {
		Vector<Byte> message = new Vector<Byte>();
		synchronized (in) {
			Integer current = in.read();
			if (current.equals(-1))
				throw new IOException("End of stream");
			if (current.equals(0xFF)) {
				current = in.read();
				if (current.equals(0x00)) {
					close();
					throw new IOException("Connection termination requested");
				} else {
					throw new IOException("Wrong message format");
				}
			}
			if (!current.equals(0x00))
				throw new IOException("Wrong message format");
			current = in.read();
			while (!current.equals(0xFF)) {
				if (current.equals(-1))
					throw new IOException("End of stream");
				message.add(current.byteValue());
				current = in.read();
			}
		}
		return byteCollectionToString(message);
	}

	public boolean isClosed() {
		return socket.isClosed();
	}

	protected byte[] readBytes(Integer count) throws IOException {
		if (count <= 0)
			return new byte[0];
		byte[] bytes = new byte[count];
		for (int i = 0; i < count; ++i) {
			Integer current = in.read();
			if (current.equals(-1))
				throw new IOException("End of stream");
			bytes[i] = current.byteValue();
		}
		return bytes;
	}

	protected String readLine() throws IOException {
		Vector<Byte> line = new Vector<Byte>();
		Integer last = in.read();
		if (last.equals(-1))
			throw new IOException("End of stream");
		Integer current = in.read();
		while (!((last.equals(0x0D)) && (current.equals(0x0A)))) {
			if (current.equals(-1))
				throw new IOException("End of stream");
			line.add(last.byteValue());
			last = current;
			current = in.read();
		}
		return byteCollectionToString(line);
	}

	public void sendMessage(String message) throws IOException {
		synchronized (out) {
			out.write(0x00);
			out.write(message.getBytes(Charset.forName("UTF-8")));
			out.write(0xFF);
			out.flush();
		}
	}

	protected void writeLine(String line) throws IOException {
		out.write(line.getBytes(Charset.forName("UTF-8")));
		out.write(0x0D);
		out.write(0x0A);
	}

	protected byte[] makeResponseToken(int key1, int key2, byte[] token)
			throws NoSuchAlgorithmException {
		MessageDigest md5digest = MessageDigest.getInstance("MD5");
		for (Integer i = 0; i < 2; ++i) {
			byte[] asByte = new byte[4];
			int key = (i == 0) ? key1 : key2;
			asByte[0] = (byte) (key >> 24);
			asByte[1] = (byte) ((key << 8) >> 24);
			asByte[2] = (byte) ((key << 16) >> 24);
			asByte[3] = (byte) ((key << 24) >> 24);
			md5digest.update(asByte);
		}
		md5digest.update(token);
		return md5digest.digest();
	}

	private String origin;

	private String host;

	private String cookie;

	private String requestUri;

	private String queryString;

	public String getOrigin() {
		return origin;
	}

	public String getHost() {
		return host;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public String getParameter(String name) {
		if (StringUtils.isBlank(queryString))
			return null;
		try {
			String[] arr = URLDecoder.decode(queryString, "UTF-8").split("&");
			for (String s : arr) {
				String[] ar = s.split("=", 2);
				if (ar[0].equals(name))
					return ar[1];
			}
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public String getCookie(String name) {
		if (StringUtils.isBlank(cookie))
			return null;
		try {
			String[] arr = URLDecoder.decode(cookie, "UTF-8").split(";");
			for (String s : arr) {
				String[] ar = s.split("=", 2);
				if (ar[0].trim().equals(name))
					return ar[1];
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cookie;
	}

	protected void handshake() throws IOException, NoSuchAlgorithmException {
		String line = readLine();
		String[] requestLine = line.split(" ");
		if (requestLine.length < 2)
			throw new IOException("Wrong Request-Line format: " + line);
		String _requestUri = requestLine[1];
		int i;
		if ((i = _requestUri.indexOf('?')) > 0) {
			queryString = _requestUri.substring(i + 1);
			requestUri = _requestUri.substring(0, i);
			// TODO cannot use parameters
		} else {
			requestUri = _requestUri;
		}
		boolean upgrade = false, connection = false;
		Long[] keys = new Long[2];

		while (!(line = readLine()).equals("")) {
			String[] parts = line.split(": ", 2);
			if (parts.length != 2)
				throw new IOException("Wrong field format: " + line);
			String name = parts[0];
			String value = parts[1];

			if (name.equalsIgnoreCase("Upgrade")) {
				if (!value.equalsIgnoreCase("WebSocket"))
					throw new IOException("Wrong value of upgrade field: "
							+ line);
				upgrade = true;
			} else if (name.equalsIgnoreCase("Connection")) {
				if (!value.equalsIgnoreCase("Upgrade"))
					throw new IOException("Wrong value of connection field: "
							+ line);
				connection = true;
			} else if (name.equalsIgnoreCase("Host")) {
				host = value;
			} else if (name.equalsIgnoreCase("Origin")) {
				origin = value;
			} else if ((name.equalsIgnoreCase("Sec-WebSocket-Key1"))
					|| (name.equalsIgnoreCase("Sec-WebSocket-Key2"))) {
				Integer spaces = new Integer(0);
				Long number = new Long(0);
				for (Character c : value.toCharArray()) {
					if (c.equals(' '))
						++spaces;
					if (Character.isDigit(c)) {
						number *= 10;
						number += Character.digit(c, 10);
					}
				}
				number /= spaces;
				if (name.endsWith("Key1"))
					keys[0] = number;
				else
					keys[1] = number;
			} else if (name.equalsIgnoreCase("Cookie")) {
				cookie = value;
			} else {
				throw new IOException("Unexpected header field: " + line);
			}
		}
		if ((!upgrade) || (!connection) || (host == null) || (origin == null)
				|| (keys[0] == null) || (keys[1] == null))
			throw new IOException("Missing handshake arguments");
		byte[] token = readBytes(8);

		writeLine("HTTP/1.1 101 WebSocket Protocol Handshake");
		writeLine("Upgrade: WebSocket");
		writeLine("Connection: Upgrade");
		writeLine("Sec-WebSocket-Origin: " + origin);
		writeLine("Sec-WebSocket-Location: ws://" + host + _requestUri);
		if (cookie != null)
			writeLine("cookie: " + cookie);
		writeLine("");
		out.write(makeResponseToken(keys[0].intValue(), keys[1].intValue(),
				token));
		out.flush();
	}

}
