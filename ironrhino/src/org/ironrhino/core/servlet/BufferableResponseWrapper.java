package org.ironrhino.core.servlet;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

public class BufferableResponseWrapper extends HttpServletResponseWrapper {

	private final RoutablePrintWriter routablePrintWriter;

	private final RoutableServletOutputStream routableServletOutputStream;

	private Buffer buffer;

	private boolean aborted = false;

	private boolean parseablePage = false;

	public BufferableResponseWrapper(final HttpServletResponse response) {
		super(response);
		routablePrintWriter = new RoutablePrintWriter(
				new RoutablePrintWriter.DestinationFactory() {
					public PrintWriter activateDestination() throws IOException {
						return response.getWriter();
					}
				});
		routableServletOutputStream = new RoutableServletOutputStream(
				new RoutableServletOutputStream.DestinationFactory() {
					public ServletOutputStream create() throws IOException {
						return response.getOutputStream();
					}
				});
	}

	@Override
	public void setContentType(String type) {
		super.setContentType(type);

		if (type != null) {
			HttpContentType httpContentType = new HttpContentType(type);
			activate(httpContentType.getType(), httpContentType.getEncoding());

		}

	}

	public void activate(String contentType, String encoding) {
		if (parseablePage) {
			return; // already activated
		}
		buffer = new Buffer(encoding);
		routablePrintWriter
				.updateDestination(new RoutablePrintWriter.DestinationFactory() {
					public PrintWriter activateDestination() {
						return buffer.getWriter();
					}
				});
		routableServletOutputStream
				.updateDestination(new RoutableServletOutputStream.DestinationFactory() {
					public ServletOutputStream create() {
						return buffer.getOutputStream();
					}
				});
		parseablePage = true;
	}

	private void deactivate() {
		parseablePage = false;
		buffer = null;
		routablePrintWriter
				.updateDestination(new RoutablePrintWriter.DestinationFactory() {
					public PrintWriter activateDestination() throws IOException {
						return getResponse().getWriter();
					}
				});
		routableServletOutputStream
				.updateDestination(new RoutableServletOutputStream.DestinationFactory() {
					public ServletOutputStream create() throws IOException {
						return getResponse().getOutputStream();
					}
				});
	}

	@Override
	public void setContentLength(int contentLength) {
		if (!parseablePage)
			super.setContentLength(contentLength);
	}

	@Override
	public void flushBuffer() throws IOException {
		if (!parseablePage)
			super.flushBuffer();
	}

	public void commitBuffer() throws IOException {
		byte[] bytes = getContents();
		if (bytes == null || bytes.length == 0)
			return;
		setContentLength(bytes.length);
		ServletOutputStream sos = super.getOutputStream();
		sos.write(bytes);
		sos.flush();
		sos.close();
	}

	@Override
	public void setHeader(String name, String value) {
		if (name.toLowerCase().equals("content-type")) {
			setContentType(value);
		} else if (!parseablePage
				|| !name.toLowerCase().equals("content-length")) {
			super.setHeader(name, value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if (name.toLowerCase().equals("content-type")) {
			setContentType(value);
		} else if (!parseablePage
				|| !name.toLowerCase().equals("content-length")) {
			super.addHeader(name, value);
		}
	}

	@Override
	public void setStatus(int sc) {
		if (sc == HttpServletResponse.SC_NOT_MODIFIED) {
			aborted = true;
			deactivate();
		}
		super.setStatus(sc);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return routableServletOutputStream;
	}

	@Override
	public PrintWriter getWriter() {
		return routablePrintWriter;
	}

	@Override
	public void sendError(int sc) throws IOException {
		aborted = true;
		super.sendError(sc);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		aborted = true;
		super.sendError(sc, msg);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		aborted = true;
		super.sendRedirect(location);
	}

	public boolean isUsingStream() {
		return buffer != null && buffer.isUsingStream();
	}

	public byte[] getContents() throws IOException {
		if (aborted || !parseablePage || buffer == null) {
			return null;
		} else {
			return buffer.getBytes();
		}
	}

	private static class HttpContentType {

		private final String type;

		private final String encoding;

		public HttpContentType(String fullValue) {
			// this is the content type + charset. eg: text/html;charset=UTF-8
			int offset = fullValue.lastIndexOf("charset=");
			encoding = offset != -1 ? extractContentTypeValue(fullValue,
					offset + 8) : null;
			type = extractContentTypeValue(fullValue, 0);
		}

		private String extractContentTypeValue(String type, int startIndex) {
			if (startIndex < 0)
				return null;

			// Skip over any leading spaces
			while (startIndex < type.length() && type.charAt(startIndex) == ' ')
				startIndex++;

			if (startIndex >= type.length()) {
				return null;
			}

			int endIndex = startIndex;

			if (type.charAt(startIndex) == '"') {
				startIndex++;
				endIndex = type.indexOf('"', startIndex);
				if (endIndex == -1)
					endIndex = type.length();
			} else {
				// Scan through until we hit either the end of the string or a
				// special character (as defined in RFC-2045). Note that we
				// ignore
				// '/'
				// since we want to capture it as part of the value.
				char ch;
				while (endIndex < type.length()
						&& (ch = type.charAt(endIndex)) != ' ' && ch != ';'
						&& ch != '(' && ch != ')' && ch != '[' && ch != ']'
						&& ch != '<' && ch != '>' && ch != ':' && ch != ','
						&& ch != '=' && ch != '?' && ch != '@' && ch != '"'
						&& ch != '\\')
					endIndex++;
			}
			return type.substring(startIndex, endIndex);
		}

		public String getType() {
			return type;
		}

		public String getEncoding() {
			return encoding;
		}
	}

	private static class Buffer {

		public static final int SIZE = 5 * 1024;

		private String encoding;

		private CharArrayWriter bufferedWriter;

		private ByteArrayOutputStream bufferedStream;

		private PrintWriter exposedWriter;

		private ServletOutputStream exposedStream;

		public Buffer(String encoding) {
			if (StringUtils.isBlank(encoding))
				encoding = "UTF-8";
			this.encoding = encoding;
		}

		public byte[] getBytes() throws IOException {
			if (bufferedWriter != null)
				return bufferedWriter.toString().getBytes(encoding);
			else if (bufferedStream != null)
				return bufferedStream.toByteArray();
			else
				return new byte[0];
		}

		public PrintWriter getWriter() {
			if (bufferedWriter == null) {
				if (bufferedStream != null) {
					throw new IllegalStateException(
							"response.getWriter() called after response.getOutputStream()");
				}
				bufferedWriter = new CharArrayWriter(128);
				exposedWriter = new PrintWriter(bufferedWriter);
			}
			return exposedWriter;
		}

		public ServletOutputStream getOutputStream() {
			if (bufferedStream == null) {
				if (bufferedWriter != null) {
					throw new IllegalStateException(
							"response.getOutputStream() called after response.getWriter()");
				}
				bufferedStream = new ByteArrayOutputStream(SIZE);
				exposedStream = new ServletOutputStream() {
					@Override
					public void write(int b) {
						bufferedStream.write(b);
					}
				};
			}
			return exposedStream;
		}

		public boolean isUsingStream() {
			return bufferedStream != null;
		}
	}

	private static class RoutableServletOutputStream extends
			ServletOutputStream {

		private ServletOutputStream destination;

		private DestinationFactory factory;

		/**
		 * Factory to lazily instantiate the destination.
		 */
		static interface DestinationFactory {
			ServletOutputStream create() throws IOException;
		}

		public RoutableServletOutputStream(DestinationFactory factory) {
			this.factory = factory;
		}

		private ServletOutputStream getDestination() throws IOException {
			if (destination == null) {
				destination = factory.create();
			}
			return destination;
		}

		public void updateDestination(DestinationFactory factory) {
			destination = null;
			this.factory = factory;
		}

		@Override
		public void close() throws IOException {
			getDestination().close();
		}

		@Override
		public void write(int b) throws IOException {
			getDestination().write(b);
		}

		@Override
		public void print(String s) throws IOException {
			getDestination().print(s);
		}

		@Override
		public void print(boolean b) throws IOException {
			getDestination().print(b);
		}

		@Override
		public void print(char c) throws IOException {
			getDestination().print(c);
		}

		@Override
		public void print(int i) throws IOException {
			getDestination().print(i);
		}

		@Override
		public void print(long l) throws IOException {
			getDestination().print(l);
		}

		@Override
		public void print(float v) throws IOException {
			getDestination().print(v);
		}

		@Override
		public void print(double v) throws IOException {
			getDestination().print(v);
		}

		@Override
		public void println() throws IOException {
			getDestination().println();
		}

		@Override
		public void println(String s) throws IOException {
			getDestination().println(s);
		}

		@Override
		public void println(boolean b) throws IOException {
			getDestination().println(b);
		}

		@Override
		public void println(char c) throws IOException {
			getDestination().println(c);
		}

		@Override
		public void println(int i) throws IOException {
			getDestination().println(i);
		}

		@Override
		public void println(long l) throws IOException {
			getDestination().println(l);
		}

		@Override
		public void println(float v) throws IOException {
			getDestination().println(v);
		}

		@Override
		public void println(double v) throws IOException {
			getDestination().println(v);
		}

		@Override
		public void write(byte b[]) throws IOException {
			getDestination().write(b);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			getDestination().write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			getDestination().flush();
		}
	}

	private static class RoutablePrintWriter extends PrintWriter {

		private PrintWriter destination;
		private DestinationFactory factory;

		/**
		 * Factory to lazily instantiate the destination.
		 */
		static interface DestinationFactory {
			PrintWriter activateDestination() throws IOException;
		}

		public RoutablePrintWriter(DestinationFactory factory) {
			super(new NullWriter());
			this.factory = factory;
		}

		private PrintWriter getDestination() {
			if (destination == null) {
				try {
					destination = factory.activateDestination();
				} catch (IOException e) {
					setError();
				}
			}
			return destination;
		}

		public void updateDestination(DestinationFactory factory) {
			destination = null;
			this.factory = factory;
		}

		@Override
		public void close() {
			getDestination().close();
		}

		@Override
		public void println(Object x) {
			getDestination().println(x);
		}

		@Override
		public void println(String x) {
			getDestination().println(x);
		}

		@Override
		public void println(char x[]) {
			getDestination().println(x);
		}

		@Override
		public void println(double x) {
			getDestination().println(x);
		}

		@Override
		public void println(float x) {
			getDestination().println(x);
		}

		@Override
		public void println(long x) {
			getDestination().println(x);
		}

		@Override
		public void println(int x) {
			getDestination().println(x);
		}

		@Override
		public void println(char x) {
			getDestination().println(x);
		}

		@Override
		public void println(boolean x) {
			getDestination().println(x);
		}

		@Override
		public void println() {
			getDestination().println();
		}

		@Override
		public void print(Object obj) {
			getDestination().print(obj);
		}

		@Override
		public void print(String s) {
			getDestination().print(s);
		}

		@Override
		public void print(char s[]) {
			getDestination().print(s);
		}

		@Override
		public void print(double d) {
			getDestination().print(d);
		}

		@Override
		public void print(float f) {
			getDestination().print(f);
		}

		@Override
		public void print(long l) {
			getDestination().print(l);
		}

		@Override
		public void print(int i) {
			getDestination().print(i);
		}

		@Override
		public void print(char c) {
			getDestination().print(c);
		}

		@Override
		public void print(boolean b) {
			getDestination().print(b);
		}

		@Override
		public void write(String s) {
			getDestination().write(s);
		}

		@Override
		public void write(String s, int off, int len) {
			getDestination().write(s, off, len);
		}

		@Override
		public void write(char buf[]) {
			getDestination().write(buf);
		}

		@Override
		public void write(char buf[], int off, int len) {
			getDestination().write(buf, off, len);
		}

		@Override
		public void write(int c) {
			getDestination().write(c);
		}

		@Override
		public boolean checkError() {
			return getDestination().checkError();
		}

		@Override
		public void flush() {
			getDestination().flush();
		}

		/**
		 * Just to keep super constructor for PrintWriter happy - it's never
		 * actually used.
		 */
		private static class NullWriter extends Writer {

			protected NullWriter() {
				super();
			}

			@Override
			public void write(char cbuf[], int off, int len) throws IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public void flush() throws IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() throws IOException {
				throw new UnsupportedOperationException();
			}

		}

	}

}