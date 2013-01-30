package org.ironrhino.core.remoting.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PreDestroy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;
import org.ironrhino.core.util.HttpClientUtils;
import org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocationResult;

public class HttpClientHttpInvokerRequestExecutor extends
		AbstractHttpInvokerRequestExecutor {

	private static final int DEFAULT_TIMEOUT = 5000;

	private HttpClient httpClient;

	public HttpClientHttpInvokerRequestExecutor() {
		httpClient = HttpClientUtils.create();
		setTimeout(DEFAULT_TIMEOUT);
	}

	public void setTimeout(int timeout) {
		HttpConnectionParams.setConnectionTimeout(this.httpClient.getParams(),
				timeout);
	}

	// slow than java.net.HttpURLConnection

	@Override
	protected RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException {
		HttpPost postMethod = new HttpPost(config.getServiceUrl());
		postMethod.setEntity(new ByteArrayEntity(baos.toByteArray()));
		HttpResponse rsp = httpClient.execute(postMethod);
		StatusLine sl = rsp.getStatusLine();
		if (sl.getStatusCode() >= 300) {
			throw new IOException(
					"Did not receive successful HTTP response: status code = "
							+ sl.getStatusCode() + ", status message = ["
							+ sl.getReasonPhrase() + "]");
		}
		HttpEntity entity = rsp.getEntity();
		InputStream responseBody = entity.getContent();
		return readRemoteInvocationResult(responseBody, config.getCodebaseUrl());

	}

	@PreDestroy
	public void destroy() {
		httpClient.getConnectionManager().shutdown();
	}

}
