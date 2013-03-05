package org.ironrhino.common.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.common.model.tuples.Pair;
import org.ironrhino.core.metadata.Trigger;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

@Singleton
@Named("pageViewService")
public class PageViewServiceImpl implements PageViewService {

	public static final String KEY_PAGE_VIEW = "{pv}:";

	@Autowired(required = false)
	@Qualifier("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	public void put(Date date, String ip, String url, String sessionId,
			String username, String referer) {
		if (stringRedisTemplate == null)
			return;
		addPageView(date);
		String day = DateUtils.formatDate8(date);
		addUnique(day, "uip", ip);
		addUnique(day, "usid", sessionId);
		addUnique(day, "uu", username);
		addUrlVisit(day, url);
		analyzeReferer(day, url);
	}

	private void addPageView(Date date) {
		StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
		sb.append("pv");
		stringRedisTemplate.opsForValue().increment(sb.toString(), 1);
		sb.append(":");
		String key = DateUtils.format(date, "yyyyMMddHH");
		stringRedisTemplate.opsForValue().increment(sb.toString() + key, 1);
		key = DateUtils.formatDate8(date);
		stringRedisTemplate.opsForValue().increment(sb.toString() + key, 1);
		key = DateUtils.format(date, "yyyyMM");
		stringRedisTemplate.opsForValue().increment(sb.toString() + key, 1);
		key = DateUtils.format(date, "yyyy");
		stringRedisTemplate.opsForValue().increment(sb.toString() + key, 1);
	}

	private void addUrlVisit(String day, String url) {
		StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
		sb.append("url");
		stringRedisTemplate.opsForZSet().incrementScore(sb.toString(), url, 1);
		sb.append(":");
		sb.append(day);
		stringRedisTemplate.opsForZSet().incrementScore(sb.toString(), url, 1);
	}

	private void analyzeReferer(String day, String referer) {
		if (StringUtils.isBlank(referer))
			return;
		String[] result = parseSearchUrl(referer);
		if (result == null)
			return;
		String searchengine = result[0];
		String keyword = result[1];
		if (StringUtils.isNotBlank(searchengine)) {
			StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
			sb.append("se");
			stringRedisTemplate.opsForZSet().incrementScore(sb.toString(),
					searchengine, 1);
			sb.append(":");
			sb.append(day);
			stringRedisTemplate.opsForZSet().incrementScore(sb.toString(),
					searchengine, 1);
		}
		if (StringUtils.isNotBlank(keyword)) {
			StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
			sb.append("kw");
			stringRedisTemplate.opsForZSet().incrementScore(sb.toString(),
					keyword, 1);
			sb.append(":");
			sb.append(day);
			stringRedisTemplate.opsForZSet().incrementScore(sb.toString(),
					keyword, 1);
		}
	}

	public long getPageView(String key) {
		return get(key, "pv");
	}

	public long getUniqueIp(String key) {
		return get(key, "uip");
	}

	public long getUniqueSessionId(String key) {
		return get(key, "usid");
	}

	public long getUniqueUsername(String key) {
		return get(key, "uu");
	}

	public Pair<String, Long> getMaxPageView() {
		return getMax("pv");
	}

	public Pair<String, Long> getMaxUniqueIp() {
		return getMax("uip");
	}

	public Pair<String, Long> getMaxUniqueSessionId() {
		return getMax("usid");
	}

	public Pair<String, Long> getMaxUniqueUsername() {
		return getMax("uu");
	}

	public Map<String, Long> getTopPageViewUrls(String day, int top) {
		return getTop(day, "url", top);
	}

	public Map<String, Long> getTopKeywords(String day, int top) {
		return getTop(day, "kw", top);
	}

	public Map<String, Long> getTopSearchEngines(String day, int top) {
		return getTop(day, "se", top);
	}

	@Trigger
	@Scheduled(cron = "0 5 0 * * ?")
	public void archive() {
		if (stringRedisTemplate == null)
			return;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = cal.getTime();
		String day = DateUtils.formatDate8(yesterday);
		stringRedisTemplate.delete(KEY_PAGE_VIEW + "uip:" + day + "_set");
		stringRedisTemplate.delete(KEY_PAGE_VIEW + "usid:" + day + "_set");
		stringRedisTemplate.delete(KEY_PAGE_VIEW + "uu:" + day + "_set");
		updateMax(day, "pv");
		updateMax(day, "uip");
		updateMax(day, "usid");
		updateMax(day, "uu");
	}

	private void addUnique(String day, String type, String value) {
		if (StringUtils.isBlank(value))
			return;
		StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
		sb.append(type).append(":").append(day);
		String key = sb.toString();
		sb.append("_set");
		if (stringRedisTemplate.opsForSet().add(sb.toString(), value))
			stringRedisTemplate.opsForValue().increment(key, 1);
	}

	private long get(String key, String type) {
		if (stringRedisTemplate == null)
			return 0;
		StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
		sb.append(type);
		if (key != null)
			sb.append(":").append(key);
		String value = stringRedisTemplate.opsForValue().get(sb.toString());
		if (value != null)
			return Long.valueOf(value);
		return 0;
	}

	private Pair<String, Long> getMax(String type) {
		if (stringRedisTemplate == null)
			return null;
		String str = (String) stringRedisTemplate.opsForHash().get(
				KEY_PAGE_VIEW + "max", type);
		if (StringUtils.isNotBlank(str)) {
			String[] arr = str.split(",");
			return new Pair<String, Long>(arr[0], Long.valueOf(arr[1]));
		}
		return null;
	}

	private void updateMax(String day, String type) {
		if (stringRedisTemplate == null)
			return;
		long value = get(day, type);
		long oldvalue = 0;
		String key = KEY_PAGE_VIEW + "max";
		String str = (String) stringRedisTemplate.opsForHash().get(key, type);
		if (StringUtils.isNotBlank(str))
			oldvalue = Long.valueOf(str.split(",")[1]);
		if (value > oldvalue)
			stringRedisTemplate.opsForHash().put(key, type, day + "," + value);
	}

	public Map<String, Long> getTop(String day, String type, int top) {
		if (stringRedisTemplate == null)
			return Collections.emptyMap();
		StringBuilder sb = new StringBuilder(KEY_PAGE_VIEW);
		sb.append(type);
		if (day != null)
			sb.append(":").append(day);
		String key = sb.toString();
		Set<String> set = stringRedisTemplate.opsForZSet().reverseRange(key, 0,
				top - 1);
		Map<String, Long> map = new LinkedHashMap<String, Long>(set.size());
		for (String member : set)
			map.put(member, stringRedisTemplate.opsForZSet().score(key, member)
					.longValue());
		return map;
	}

	private static String[] parseSearchUrl(String searchUrl) {
		try {
			URL url = new URL(searchUrl);
			String host = url.getHost();
			String query = url.getQuery();
			for (Map.Entry<String, String> entry : searchengines.entrySet()) {
				if (host.indexOf(entry.getKey() + ".") == 0
						|| host.indexOf("." + entry.getKey() + ".") > 0) {
					String[] result = new String[2];
					result[0] = entry.getKey();
					result[1] = RequestUtils.getValueFromQueryString(query,
							entry.getValue());
					return result;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Map<String, String> searchengines = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("google", "q");
			put("bing", "q");
			put("taobao", "q");
			put("360", "q");
			put("yahoo", "p");
			put("baidu", "wd");
			put("sogou", "query");
			put("soso", "w");
			put("youdao", "q");
		}
	};

}
