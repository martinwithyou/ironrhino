package org.ironrhino.core.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtils {

	private static Logger logger = LoggerFactory
			.getLogger(HttpClientUtils.class);

	private static Set<Header> DEFAULT_HEADERS = new HashSet<Header>();

	static {
		DEFAULT_HEADERS
				.add(new BasicHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.37 Safari/537.36"));
	}

	static class HttpClientHolder {
		static CloseableHttpClient httpClient = create();
	}

	public static CloseableHttpClient getDefaultInstance() {
		return HttpClientHolder.httpClient;
	}

	public static CloseableHttpClient create() {
		return create(false);
	}

	public static CloseableHttpClient create(boolean single) {
		return create(single, 10000);
	}

	@SuppressWarnings("resource")
	public static CloseableHttpClient create(boolean single, int connectTimeout) {
		HttpClientConnectionManager connManager;
		if (single)
			connManager = new BasicHttpClientConnectionManager();
		else {
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
					60, TimeUnit.SECONDS);
			cm.setDefaultMaxPerRoute(5);
			cm.setMaxTotal(100);
			connManager = cm;
		}
		RequestConfig requestConfig = RequestConfig.custom()
				.setCircularRedirectsAllowed(true)
				.setConnectTimeout(connectTimeout)
				.setExpectContinueEnabled(true).build();
		CloseableHttpClient httpclient = HttpClientBuilder.create()
				.setConnectionManager(connManager)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setDefaultRequestConfig(requestConfig)
				.setDefaultHeaders(DEFAULT_HEADERS).build();
		return httpclient;
	}

	public static String getResponseText(String url) {
		return getResponseText(url, null, "UTF-8");
	}

	public static String getResponseText(String url, Map<String, String> params) {
		return getResponseText(url, params, "UTF-8");
	}

	public static String getResponseText(String url,
			Map<String, String> params, Map<String, String> headers) {
		return getResponseText(url, params, headers, "UTF-8");
	}

	public static String getResponseText(String url,
			Map<String, String> params, String encoding) {
		return getResponseText(url, params, null, encoding);
	}

	public static String getResponseText(String url,
			Map<String, String> params, Map<String, String> headers,
			String encoding) {
		HttpGet httpRequest = null;
		try {
			StringBuilder sb = new StringBuilder(url);
			if (params != null && params.size() > 0) {
				sb.append(url.indexOf('?') < 0 ? '?' : '&');
				for (Map.Entry<String, String> entry : params.entrySet()) {
					sb.append(entry.getKey())
							.append("=")
							.append(URLEncoder.encode(entry.getValue(),
									encoding)).append("&");
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			httpRequest = new HttpGet(sb.toString());
			if (headers != null && headers.size() > 0)
				for (Map.Entry<String, String> entry : headers.entrySet())
					httpRequest.addHeader(entry.getKey(), entry.getValue());
			return getDefaultInstance().execute(httpRequest,
					new BasicResponseHandler());
		} catch (Exception e) {
			httpRequest.abort();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String postResponseText(String url, Map<String, String> params) {
		return postResponseText(url, params, "UTF-8");
	}

	public static String postResponseText(String url,
			Map<String, String> params, Map<String, String> headers) {
		return postResponseText(url, params, headers, "UTF-8");
	}

	public static String postResponseText(String url,
			Map<String, String> params, String encoding) {
		return postResponseText(url, params, null, encoding);
	}

	public static String postResponseText(String url,
			Map<String, String> params, Map<String, String> headers,
			String encoding) {
		HttpPost httpRequest = new HttpPost(url);
		try {
			if (params != null && params.size() > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : params.entrySet())
					nvps.add(new BasicNameValuePair(entry.getKey(), entry
							.getValue()));
				httpRequest.setEntity(new UrlEncodedFormEntity(nvps, encoding));
			}
			if (headers != null && headers.size() > 0)
				for (Map.Entry<String, String> entry : headers.entrySet())
					httpRequest.addHeader(entry.getKey(), entry.getValue());
			return getDefaultInstance().execute(httpRequest,
					new BasicResponseHandler());
		} catch (Exception e) {
			httpRequest.abort();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String postResponseText(String url, String body,
			Map<String, String> headers, String encoding) {
		HttpPost httpRequest = new HttpPost(url);
		try {
			httpRequest.setEntity(new StringEntity(body, encoding));
			if (headers != null && headers.size() > 0)
				for (Map.Entry<String, String> entry : headers.entrySet())
					httpRequest.addHeader(entry.getKey(), entry.getValue());
			return getDefaultInstance().execute(httpRequest,
					new BasicResponseHandler());
		} catch (Exception e) {
			httpRequest.abort();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String post(String url, String entity) {
		return invoke("POST", url, entity);
	}

	public static String put(String url, String entity) {
		return invoke("PUT", url, entity);
	}

	public static String delete(String url) {
		return invoke("DELETE", url, null);
	}

	public static String get(String url) {
		return invoke("GET", url, null);
	}

	private static String invoke(String method, String url, String entity) {
		HttpRequestBase httpRequest = null;
		if (method.equalsIgnoreCase("GET"))
			httpRequest = new HttpGet(url);
		else if (method.equalsIgnoreCase("POST"))
			httpRequest = new HttpPost(url);
		else if (method.equalsIgnoreCase("PUT"))
			httpRequest = new HttpPut(url);
		else if (method.equalsIgnoreCase("DELETE"))
			httpRequest = new HttpDelete(url);
		try {
			if (entity != null)
				((HttpEntityEnclosingRequestBase) httpRequest)
						.setEntity(new StringEntity(entity, "UTF-8"));
			return getDefaultInstance().execute(httpRequest,
					new BasicResponseHandler());
		} catch (Exception e) {
			httpRequest.abort();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
