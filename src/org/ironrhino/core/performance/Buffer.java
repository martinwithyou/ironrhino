package org.ironrhino.core.performance;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;

import org.apache.commons.lang.StringUtils;

public class Buffer {

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
