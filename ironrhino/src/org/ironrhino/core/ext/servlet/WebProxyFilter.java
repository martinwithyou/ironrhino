package org.ironrhino.core.ext.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.lf5.util.StreamUtils;

public class WebProxyFilter implements Filter {

	public static final String WEB_PROXY = "/webproxy/";

	private HttpClient httpClient = new HttpClient();

	private boolean checkSameOrigin = true;

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		URL requestURL = new URL(request.getRequestURL().toString());
		if (path.startsWith(WEB_PROXY)) {
			if (checkSameOrigin) {
				String referer = request.getHeader("Referer");
				if (StringUtils.isBlank(referer))
					return;
				URL url = new URL(referer);
				if (!url.getProtocol().equalsIgnoreCase(
						requestURL.getProtocol())
						|| !url.getHost()
								.equalsIgnoreCase(requestURL.getHost())
						|| url.getPort() != requestURL.getPort())
					return;
			}
			String uri = StringUtils.substringAfter(path, WEB_PROXY);
			if (!uri.startsWith("http"))
				uri = "http://" + uri;
			if (uri.indexOf("http://") < 0 && uri.indexOf("http:/") >= 0)
				uri = uri.replace("http:/", "http://");
			HttpMethod method;
			if ("GET".equalsIgnoreCase(request.getMethod())) {
				method = new GetMethod(uri);
			} else if ("GET".equalsIgnoreCase(request.getMethod())) {
				method = new GetMethod(uri);
			} else if ("POST".equalsIgnoreCase(request.getMethod())) {
				method = new PostMethod(uri);
			} else if ("PUT".equalsIgnoreCase(request.getMethod())) {
				method = new PutMethod(uri);
			} else if ("DELETE".equalsIgnoreCase(request.getMethod())) {
				method = new DeleteMethod(uri);
			} else {
				method = new GetMethod(uri);
			}
			method.setFollowRedirects(true);
			String queryString = request.getQueryString();
			if (queryString != null) {
				if (queryString.startsWith("_"))
					queryString = "&" + queryString;
				queryString = queryString.replaceAll("\\&_\\w+_=[^&]+", "");
				if (queryString.startsWith("&"))
					queryString = queryString.substring(1);
				method.setQueryString(queryString);
			}
			HttpMethodParams params = new HttpMethodParams();
			params.setSoTimeout(5000);
			Enumeration<String> en = request.getParameterNames();
			while (en.hasMoreElements()) {
				String name = en.nextElement();
				if (name.startsWith("_") && name.endsWith("_")
						|| queryString != null
						&& queryString.contains(name + "="))
					continue;
				for (String value : request.getParameterValues(name))
					params.setParameter(name, value);
			}
			method.setParams(params);
			try {
				int code = httpClient.executeMethod(method);
				if (code >= 400) {
					response.sendError(code);
					return;
				}
				response.setStatus(code);
				for (Header h : method.getResponseHeaders())
					response.setHeader(h.getName(), h.getValue());
				InputStream input = method.getResponseBodyAsStream();
				StreamUtils.copy(input, response.getOutputStream());
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				method.releaseConnection();
			}
			return;
		}
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {
		String s = config.getInitParameter("checkSameOrigin");
		if (StringUtils.isBlank(s))
			return;
		checkSameOrigin = Boolean.parseBoolean(s.trim());
	}

}
