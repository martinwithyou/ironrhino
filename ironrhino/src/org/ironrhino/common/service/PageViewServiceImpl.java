package org.ironrhino.common.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.common.model.tuples.Pair;
import org.ironrhino.core.metadata.Trigger;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

@Singleton
@Named("pageViewService")
public class PageViewServiceImpl implements PageViewService {

	public static final String KEY_PAGE_VIEW = "{pv}";

	public static final String KEY_PAGE_VIEW_UIP = "{pv}:uip:";

	public static final String KEY_PAGE_VIEW_USID = "{pv}:usid:";

	public static final String KEY_PAGE_VIEW_UU = "{pv}:uu:";

	public static final String KEY_PAGE_VIEW_MAX = "{pv}:max";

	public static final String KEY_PAGE_VIEW_URL = "{pv}:url";

	@Autowired(required = false)
	@Qualifier("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	public void put(Date date, String ip, String url, String sessionId,
			String username, String referer) {
		if (stringRedisTemplate == null)
			return;
		addPageView(date);
		addUniqueIp(date, ip);
		addUniqueSessionId(date, sessionId);
		addUniqueUsername(date, username);
		addUrlVisit(date, url);
	}

	private void addPageView(Date date) {
		stringRedisTemplate.opsForValue().increment(KEY_PAGE_VIEW, 1);
		String key = DateUtils.format(date, "yyyyMMddHH");
		stringRedisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key,
				1);
		key = DateUtils.format(date, "yyyyMMdd");
		stringRedisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key,
				1);
		key = DateUtils.format(date, "yyyyMM");
		stringRedisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key,
				1);
		key = DateUtils.format(date, "yyyy");
		stringRedisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key,
				1);
	}

	private void addUniqueIp(Date date, String ip) {
		if (StringUtils.isBlank(ip))
			return;
		String key = KEY_PAGE_VIEW_UIP + DateUtils.format(date, "yyyyMMdd");
		String keyForSet = key + "_set";
		if (stringRedisTemplate.opsForSet().add(keyForSet, ip))
			stringRedisTemplate.opsForValue().increment(key, 1);
	}

	private void addUniqueSessionId(Date date, String sessionId) {
		if (StringUtils.isBlank(sessionId))
			return;
		String key = KEY_PAGE_VIEW_USID + DateUtils.format(date, "yyyyMMdd");
		String keyForSet = key + "_set";
		if (stringRedisTemplate.opsForSet().add(keyForSet, sessionId))
			stringRedisTemplate.opsForValue().increment(key, 1);
	}

	private void addUniqueUsername(Date date, String username) {
		if (StringUtils.isBlank(username))
			return;
		String key = KEY_PAGE_VIEW_UU + DateUtils.format(date, "yyyyMMdd");
		String keyForSet = key + "_set";
		if (stringRedisTemplate.opsForSet().add(keyForSet, username))
			stringRedisTemplate.opsForValue().increment(key, 1);
	}

	private void addUrlVisit(Date date, String url) {
		stringRedisTemplate.opsForZSet().incrementScore(KEY_PAGE_VIEW_URL, url,
				1);
		stringRedisTemplate.opsForZSet().incrementScore(
				KEY_PAGE_VIEW_URL + ":" + DateUtils.format(date, "yyyyMMdd"),
				url, 1);
	}

	public long getPageView(String key) {
		if (stringRedisTemplate == null)
			return 0;
		String value = stringRedisTemplate.opsForValue().get(
				KEY_PAGE_VIEW + (key == null ? "" : ":" + key));
		if (value != null)
			return Long.valueOf(value);
		if (key.length() == 8) {
			long total = 0;
			for (int i = 0; i < 24; i++)
				total += getPageView(key + NumberUtils.format(i, 2));
			return total;
		} else if (key.length() == 6) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Integer.valueOf(key.substring(0, 4)));
			cal.set(Calendar.MONTH, Integer.valueOf(key.substring(4)) - 1);
			int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			int total = 0;
			for (int i = 1; i <= max; i++)
				total += getPageView(key + NumberUtils.format(i, 2));
			return total;
		} else if (key.length() == 4) {
			int total = 0;
			for (int i = 1; i <= 12; i++)
				total += getPageView(key + NumberUtils.format(i, 2));
			return total;
		}
		return 0;
	}

	public long getUniqueIp(String key) {
		if (stringRedisTemplate == null)
			return 0;
		String value = stringRedisTemplate.opsForValue().get(
				KEY_PAGE_VIEW_UIP + key);
		return value != null ? Long.valueOf(value) : 0;
	}

	public long getUniqueSessionId(String key) {
		if (stringRedisTemplate == null)
			return 0;
		String value = stringRedisTemplate.opsForValue().get(
				KEY_PAGE_VIEW_USID + key);
		return value != null ? Long.valueOf(value) : 0;
	}

	public long getUniqueUsername(String key) {
		if (stringRedisTemplate == null)
			return 0;
		String value = stringRedisTemplate.opsForValue().get(
				KEY_PAGE_VIEW_UU + key);
		return value != null ? Long.valueOf(value) : 0;
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

	public long getPageViewByUrl(String day, String url) {
		if (stringRedisTemplate == null)
			return 0;
		String key = KEY_PAGE_VIEW_URL;
		if (day != null)
			key += ":" + day;
		Double d = stringRedisTemplate.opsForZSet().score(key, url);
		return d != null ? d.longValue() : 0;
	}

	public Map<String, Long> getTopPageViewUrls(String day, int top) {
		if (stringRedisTemplate == null)
			return Collections.emptyMap();
		String key = KEY_PAGE_VIEW_URL;
		if (day != null)
			key += ":" + day;
		Set<String> set = stringRedisTemplate.opsForZSet().reverseRange(key, 0,
				top - 1);
		Map<String, Long> map = new LinkedHashMap<String, Long>(set.size());
		for (String member : set)
			map.put(member, stringRedisTemplate.opsForZSet().score(key, member)
					.longValue());
		return map;
	}

	@Trigger
	@Scheduled(cron = "0 5 0 * * ?")
	public void archive() {
		if (stringRedisTemplate == null)
			return;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = cal.getTime();
		String day = DateUtils.format(yesterday, "yyyyMMdd");
		stringRedisTemplate.delete(KEY_PAGE_VIEW_UIP + day + "_set");
		stringRedisTemplate.delete(KEY_PAGE_VIEW_USID + day + "_set");
		stringRedisTemplate.delete(KEY_PAGE_VIEW_UU + day + "_set");

		updateMax(day, "pv");
		updateMax(day, "uip");
		updateMax(day, "usid");
		updateMax(day, "uu");
	}

	private Pair<String, Long> getMax(String type) {
		if (stringRedisTemplate == null)
			return null;
		String str = (String) stringRedisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, type);
		if (StringUtils.isNotBlank(str)) {
			String[] arr = str.split(",");
			return new Pair<String, Long>(arr[0], Long.valueOf(arr[1]));
		}
		return null;
	}

	private void updateMax(String day, String type) {
		if (stringRedisTemplate == null)
			return;
		long value = 0;
		if ("pv".equals(type))
			value = getPageView(day);
		else if ("uip".equals(type))
			value = getUniqueIp(day);
		else if ("usid".equals(type))
			value = getUniqueSessionId(day);
		else if ("uu".equals(type))
			value = getUniqueUsername(day);
		long oldvalue = 0;
		String str = (String) stringRedisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, type);
		if (StringUtils.isNotBlank(str))
			oldvalue = Long.valueOf(str.split(",")[1]);
		if (value > oldvalue)
			stringRedisTemplate.opsForHash().put(KEY_PAGE_VIEW_MAX, type,
					day + "," + value);
	}

}
