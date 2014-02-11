package org.ironrhino.core.util;

import java.io.IOException;
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

public class HttpClientUtils {

	private static Set<Header> DEFAULT_HEADERS = new HashSet<Header>();

	static {
		DEFAULT_HEADERS
				.add(new BasicHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"));
		DEFAULT_HEADERS
				.add(new BasicHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		DEFAULT_HEADERS.add(new BasicHeader("Accept-Encoding",
				"gzip,deflate,sdch"));
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

	public static String getResponseText(String url) throws IOException {
		return getResponseText(url, null, "UTF-8");
	}

	public static String getResponseText(String url, Map<String, String> params)
			throws IOException {
		return getResponseText(url, params, "UTF-8");
	}

	public static String getResponseText(String url,
			Map<String, String> params, Map<String, String> headers)
			throws IOException {
		return getResponseText(url, params, headers, "UTF-8");
	}

	public static String getResponseText(String url,
			Map<String, String> params, String encoding) throws IOException {
		return getResponseText(url, params, null, encoding);
	}

	public static String getResponseText(String url,
			Map<String, String> params, Map<String, String> headers,
			String encoding) throws IOException {
		HttpGet httpRequest = null;
		StringBuilder sb = new StringBuilder(url);
		if (params != null && params.size() > 0) {
			sb.append(url.indexOf('?') < 0 ? '?' : '&');
			for (Map.Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey()).append("=")
						.append(URLEncoder.encode(entry.getValue(), encoding))
						.append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		httpRequest = new HttpGet(sb.toString());
		if (headers != null && headers.size() > 0)
			for (Map.Entry<String, String> entry : headers.entrySet())
				httpRequest.addHeader(entry.getKey(), entry.getValue());
		return getDefaultInstance().execute(httpRequest,
				new BasicResponseHandler());
	}

	public static String postResponseText(String url, Map<String, String> params)
			throws IOException {
		return postResponseText(url, params, "UTF-8");
	}

	public static String postResponseText(String url,
			Map<String, String> params, Map<String, String> headers)
			throws IOException {
		return postResponseText(url, params, headers, "UTF-8");
	}

	public static String postResponseText(String url,
			Map<String, String> params, String encoding) throws IOException {
		return postResponseText(url, params, null, encoding);
	}

	public static String postResponseText(String url,
			Map<String, String> params, Map<String, String> headers,
			String encoding) throws IOException {
		HttpPost httpRequest = new HttpPost(url);
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
	}

	public static String postResponseText(String url, String body,
			Map<String, String> headers, String encoding) throws IOException {
		HttpPost httpRequest = new HttpPost(url);
		httpRequest.setEntity(new StringEntity(body, encoding));
		if (headers != null && headers.size() > 0)
			for (Map.Entry<String, String> entry : headers.entrySet())
				httpRequest.addHeader(entry.getKey(), entry.getValue());
		return getDefaultInstance().execute(httpRequest,
				new BasicResponseHandler());
	}

	public static String post(String url, String entity) throws IOException {
		return invoke("POST", url, entity);
	}

	public static String put(String url, String entity) throws IOException {
		return invoke("PUT", url, entity);
	}

	public static String delete(String url) throws IOException {
		return invoke("DELETE", url, null);
	}

	public static String get(String url) throws IOException {
		return invoke("GET", url, null);
	}

	private static String invoke(String method, String url, String entity)
			throws IOException {
		HttpRequestBase httpRequest = null;
		if (method.equalsIgnoreCase("GET"))
			httpRequest = new HttpGet(url);
		else if (method.equalsIgnoreCase("POST"))
			httpRequest = new HttpPost(url);
		else if (method.equalsIgnoreCase("PUT"))
			httpRequest = new HttpPut(url);
		else if (method.equalsIgnoreCase("DELETE"))
			httpRequest = new HttpDelete(url);
		if (entity != null)
			((HttpEntityEnclosingRequestBase) httpRequest)
					.setEntity(new StringEntity(entity, "UTF-8"));
		return getDefaultInstance().execute(httpRequest,
				new BasicResponseHandler());
	}

}
