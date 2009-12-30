package org.ironrhino.core.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

public class FluxOptimizationFilter implements Filter {

	private boolean etagEnabled = true;

	private boolean compressEnabled = true;

	public void doFilter(ServletRequest rq, ServletResponse rs,
			FilterChain chain) throws IOException, ServletException {
		if (!etagEnabled && !compressEnabled)
			chain.doFilter(rq, rs);
		HttpServletRequest request = (HttpServletRequest) rq;
		HttpServletResponse response = (HttpServletResponse) rs;
		LazyCommitResponseWrapper brw = new LazyCommitResponseWrapper(response);
		chain.doFilter(request, brw);
		byte[] bytes = brw.getContents();
		if (bytes != null) {
			boolean notmodified = false;
			if (etagEnabled) {
				String token = '"' + DigestUtils.md5Hex(bytes) + '"';
				response.setHeader("ETag", token);
				String previousToken = request.getHeader("If-None-Match");
				if (previousToken != null && previousToken.equals(token)) {
					response.setHeader("Last-Modified", request
							.getHeader("If-Modified-Since"));
					response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
					notmodified = true;
				} else {
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.MILLISECOND, 0);
					Date lastModified = cal.getTime();
					response.setDateHeader("Last-Modified", lastModified
							.getTime());
				}
			}
			if (!notmodified) {
				if (compressEnabled
						&& brw.getContentType().indexOf("text") >= 0) {
					String acceptEncoding = request
							.getHeader("Accept-Encoding");
					if (acceptEncoding != null) {
						acceptEncoding = acceptEncoding.toLowerCase();
						if (acceptEncoding.indexOf("gzip") >= 0) {
							ByteArrayOutputStream boas = new ByteArrayOutputStream(
									10 * 1024);
							GZIPOutputStream gzos = new GZIPOutputStream(boas);
							gzos.write(bytes);
							gzos.flush();
							gzos.close();
							bytes = boas.toByteArray();
							response
									.setHeader(
											"Content-Encoding",
											acceptEncoding.indexOf("x-gzip") >= 0 ? "x-gzip"
													: "gzip");
						} else if (acceptEncoding.indexOf("deflate") >= 0) {
							// has problem with IE6
							ByteArrayOutputStream boas = new ByteArrayOutputStream(
									bytes.length / 2);
							DeflaterOutputStream dfos = new DeflaterOutputStream(
									boas, new Deflater(
											Deflater.BEST_COMPRESSION),
									bytes.length / 2);
							dfos.write(bytes);
							dfos.flush();
							dfos.close();
							bytes = boas.toByteArray();
							response.setHeader("Content-Encoding", "deflate");
						}
					}
				}

				response.setContentLength(bytes.length);
				ServletOutputStream sos = response.getOutputStream();
				sos.write(bytes);
				sos.flush();
				sos.close();
			}
		}
	}

	public void init(FilterConfig filterConfig) {
		if ("false".equals(filterConfig.getInitParameter("etagEnabled")))
			etagEnabled = false;
		if ("false".equals(filterConfig.getInitParameter("compressEnabled")))
			compressEnabled = false;
	}

	public void destroy() {

	}

}
