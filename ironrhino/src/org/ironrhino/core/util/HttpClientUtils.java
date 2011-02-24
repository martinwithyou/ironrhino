package org.ironrhino.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

public class HttpClientUtils {

	static class HttpClientHolder {
		static HttpClient httpClient = create();
	}

	public static HttpClient getDefaultInstance() {
		return HttpClientHolder.httpClient;
	}

	public static HttpClient create() {
		return create(false);
	}

	public static HttpClient create(boolean single) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUserAgent(params, null);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpConnectionParams.setConnectionTimeout(params, 10000);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
				.getSocketFactory()));
		ClientConnectionManager clientConnectionManager;
		if (single)
			clientConnectionManager = new SingleClientConnManager(
					schemeRegistry);
		else {
			ThreadSafeClientConnManager tsccm = new ThreadSafeClientConnManager(
					schemeRegistry);
			tsccm.setDefaultMaxPerRoute(5);
			tsccm.setMaxTotal(100);
			clientConnectionManager = tsccm;
		}
		DefaultHttpClient httpclient = new DefaultHttpClient(
				clientConnectionManager, params);
		httpclient.addRequestInterceptor(gzipHttpRequestInterceptor);
		httpclient.addResponseInterceptor(gzipHttpResponseInterceptor);
		httpclient.setKeepAliveStrategy(connectionKeepAliveStrategy);
		return httpclient;
	}

	private static ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy();

	private static HttpRequestInterceptor gzipHttpRequestInterceptor = new HttpRequestInterceptor() {
		public void process(final HttpRequest request, final HttpContext context)
				throws HttpException, IOException {
			if (!request.containsHeader("Accept-Encoding")) {
				request.addHeader("Accept-Encoding", "gzip");
			}
		}
	};

	private static HttpResponseInterceptor gzipHttpResponseInterceptor = new HttpResponseInterceptor() {
		public void process(final HttpResponse response,
				final HttpContext context) throws HttpException, IOException {
			HttpEntity entity = response.getEntity();
			Header ceheader = entity.getContentEncoding();
			if (ceheader != null) {
				HeaderElement[] codecs = ceheader.getElements();
				for (int i = 0; i < codecs.length; i++) {
					if (codecs[i].getName().equalsIgnoreCase("gzip")) {
						response.setEntity(new GzipDecompressingEntity(response
								.getEntity()));
						return;
					}
				}
			}
		}
	};

	private static class GzipDecompressingEntity extends HttpEntityWrapper {
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException {
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}

	public static String getResponseText(String url, Map<String, String> params) {
		return getResponseText(url, params, "UTF-8");
	}

	public static String getResponseText(String url,
			Map<String, String> params, String encoding) {
		HttpGet httpRequest = null;
		try {
			StringBuilder sb = new StringBuilder(url);
			if (params != null && params.size() > 0) {
				sb.append(url.indexOf('?') < 0 ? '?' : '&');
				for (Map.Entry<String, String> entry : params.entrySet()) {
					sb.append(entry.getKey()).append("=").append(
							URLEncoder.encode(entry.getValue(), encoding))
							.append("&");
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			httpRequest = new HttpGet(sb.toString());
			return getDefaultInstance().execute(httpRequest,
					new BasicResponseHandler());
		} catch (Exception e) {
			httpRequest.abort();
			e.printStackTrace();
		}
		return null;
	}

	public static String postResponseText(String url, Map<String, String> params) {
		return postResponseText(url, params, "UTF-8");
	}

	public static String postResponseText(String url,
			Map<String, String> params, String encoding) {
		HttpPost httpRequest = new HttpPost(url);
		try {
			if (params != null && params.size() > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : params.entrySet())
					nvps.add(new BasicNameValuePair(entry.getKey(), entry
							.getValue()));
				httpRequest.setEntity(new UrlEncodedFormEntity(nvps, encoding));
			}
			return getDefaultInstance().execute(httpRequest,
					new BasicResponseHandler());
		} catch (Exception e) {
			httpRequest.abort();
			e.printStackTrace();
		}
		return null;
	}

}
