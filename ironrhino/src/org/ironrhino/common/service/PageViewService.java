package org.ironrhino.common.service;

import java.util.Date;

import org.ironrhino.common.model.tuples.Pair;

public interface PageViewService {

	public void put(Date date, String ip, String url, String sessionId,
			String referer);

	public long getPageView(String key);

	public long getUniqueIp(String day);

	public long getUniqueSessionId(String day);

	public Pair<String, Long> getMaxPageView();

	public Pair<String, Long> getMaxUniqueIp();

	public Pair<String, Long> getMaxUniqueSessionId();
}
