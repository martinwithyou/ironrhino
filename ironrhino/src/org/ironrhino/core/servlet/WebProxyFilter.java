package org.ironrhino.core.servlet;

import java.io.IOException;
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class WebProxyFilter implements Filter {

	private ClientConnectionManager clientConnectionManager;

	private HttpClient httpClient;

	private boolean checkSameOrigin = false;

	public void destroy() {
		if (clientConnectionManager != null)
			clientConnectionManager.shutdown();
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		URL requestURL = new URL(request.getRequestURL().toString());
		if (checkSameOrigin) {
			String referer = request.getHeader("Referer");
			if (StringUtils.isBlank(referer))
				return;
			URL url = new URL(referer);
			if (!url.getProtocol().equalsIgnoreCase(requestURL.getProtocol())
					|| !url.getHost().equalsIgnoreCase(requestURL.getHost())
					|| url.getPort() != requestURL.getPort())
				return;
		}
		String uri = requestURL.toString();
		String target = null;
		int index = uri.indexOf("http://", uri.indexOf("://") + 1);
		if (index > 0)
			target = uri.substring(index);
		if (target == null) {
			index = uri.indexOf("https://", uri.indexOf("://") + 1);
			if (index > 0)
				target = uri.substring(index);
		}
		if (target == null)
			return;
		String queryString = request.getQueryString();
		StringBuilder sb = new StringBuilder();
		sb.append(target);
		if (StringUtils.isNotBlank(queryString)) {
			sb.append('?');
			sb.append(queryString);
		}
		uri = sb.toString();
		HttpRequestBase httpRequest;
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			httpRequest = new HttpGet(uri);
		} else if ("POST".equalsIgnoreCase(request.getMethod())) {
			httpRequest = new HttpPost(uri);
		} else if ("PUT".equalsIgnoreCase(request.getMethod())) {
			httpRequest = new HttpPut(uri);
		} else if ("DELETE".equalsIgnoreCase(request.getMethod())) {
			httpRequest = new HttpDelete(uri);
		} else {
			httpRequest = new HttpGet(uri);
		}
		HttpParams params = new BasicHttpParams();
		Enumeration<String> en = request.getParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			for (String value : request.getParameterValues(name))
				if (queryString == null || !queryString.contains(name + "="))
					params.setParameter(name, value);
		}
		httpRequest.setParams(params);
		HttpEntity entity = null;
		try {
			HttpResponse rsp = httpClient.execute(httpRequest);
			entity = rsp.getEntity();
			if (entity != null) {
				for (Header h : httpRequest.getAllHeaders())
					response.setHeader(h.getName(), h.getValue());
				entity.writeTo(response.getOutputStream());
			}
		} catch (Exception e) {
			httpRequest.abort();
			e.printStackTrace();
		}
	}

	public void init(FilterConfig config) throws ServletException {
		String s = config.getInitParameter("checkSameOrigin");
		if (StringUtils.isNotBlank(s))
			checkSameOrigin = Boolean.parseBoolean(s.trim());
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		clientConnectionManager = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		httpClient = new DefaultHttpClient(clientConnectionManager, params);
	}

}
