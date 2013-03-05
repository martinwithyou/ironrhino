package org.ironrhino.common.service;

import java.util.Date;
import java.util.Map;

import org.ironrhino.common.model.tuples.Pair;

public interface PageViewService {

	public void put(Date date, String ip, String url, String sessionId,
			String username, String referer);

	public long getPageView(String key);

	public long getUniqueIp(String day);

	public long getUniqueSessionId(String day);

	public long getUniqueUsername(String day);

	public Pair<String, Long> getMaxPageView();

	public Pair<String, Long> getMaxUniqueIp();

	public Pair<String, Long> getMaxUniqueSessionId();

	public Pair<String, Long> getMaxUniqueUsername();

	public long getPageViewByUrl(String day, String url);

	public Map<String, Long> getTopPageViewUrls(String day, int top);

}
