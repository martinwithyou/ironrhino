package org.ironrhino.common.service;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import org.ironrhino.common.model.tuples.Pair;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PageViewServiceImpl implements PageViewService {

	public static final String KEY_PAGE_VIEW = "{pv}";

	public static final String KEY_PAGE_VIEW_UIP = "{pv}:uip:";

	public static final String KEY_PAGE_VIEW_USID = "{pv}:usid:";

	public static final String KEY_PAGE_VIEW_MAX = "{pv}:max";

	@Inject
	private RedisTemplate redisTemplate;

	public void put(Date date, String ip, String url, String sessionId,
			String referer) {
		addPageView(date);
		addUniqueIp(date, ip);
		addUniqueSessionId(date, sessionId);
	}

	private void addPageView(Date date) {
		redisTemplate.opsForValue().increment(KEY_PAGE_VIEW, 1);
		String key = DateUtils.format(date, "yyyyMMddHH");
		redisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key, 1);
		key = DateUtils.format(date, "yyyyMMdd");
		redisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key, 1);
		key = DateUtils.format(date, "yyyyMM");
		redisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key, 1);
		key = DateUtils.format(date, "yyyy");
		redisTemplate.opsForValue().increment(KEY_PAGE_VIEW + ":" + key, 1);
	}

	private void addUniqueIp(Date date, String ip) {
		String key = KEY_PAGE_VIEW_UIP + DateUtils.format(date, "yyyyMMdd");
		String keyForSet = key + "_set";
		if (redisTemplate.opsForSet().add(keyForSet, ip))
			redisTemplate.opsForValue().increment(key, 1);
	}

	private void addUniqueSessionId(Date date, String sessionId) {
		String key = KEY_PAGE_VIEW_USID + DateUtils.format(date, "yyyyMMdd");
		String keyForSet = key + "_set";
		if (redisTemplate.opsForSet().add(keyForSet, sessionId))
			redisTemplate.opsForValue().increment(key, 1);
	}

	public long getPageView(String key) {
		Long value = (Long) redisTemplate.opsForValue().get(
				KEY_PAGE_VIEW + (key == null ? "" : ":" + key));
		if (value != null)
			return value;
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
		Long value = (Long) redisTemplate.opsForValue().get(
				KEY_PAGE_VIEW_UIP + key);
		return value != null ? value : 0;
	}

	public long getUniqueSessionId(String key) {
		Long value = (Long) redisTemplate.opsForValue().get(
				KEY_PAGE_VIEW_USID + key);
		return value != null ? value : 0;
	}

	public Pair<String, Long> getMaxPageView() {
		return (Pair<String, Long>) redisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, "pv");
	}

	public Pair<String, Long> getMaxUniqueIp() {
		return (Pair<String, Long>) redisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, "uip");
	}

	public Pair<String, Long> getMaxUniqueSessionId() {
		return (Pair<String, Long>) redisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, "usid");
	}

	@Scheduled(cron = "0 5 0 * * ?")
	public void archive() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = cal.getTime();
		String day = DateUtils.format(yesterday, "yyyyMMdd");
		redisTemplate.delete(KEY_PAGE_VIEW_UIP + day + "_set");
		redisTemplate.delete(KEY_PAGE_VIEW_USID + day + "_set");

		long value = getPageView(day);
		Pair<String, Long> pair = (Pair<String, Long>) redisTemplate
				.opsForHash().get(KEY_PAGE_VIEW_MAX, "pv");
		if (pair == null || value > pair.getB())
			redisTemplate.opsForHash().put(KEY_PAGE_VIEW_MAX, "pv",
					new Pair(day, value));
		value = getUniqueIp(day);
		pair = (Pair<String, Long>) redisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, "uip");
		if (pair == null || value > pair.getB())
			redisTemplate.opsForHash().put(KEY_PAGE_VIEW_MAX, "uip",
					new Pair(day, value));
		value = getUniqueSessionId(day);
		pair = (Pair<String, Long>) redisTemplate.opsForHash().get(
				KEY_PAGE_VIEW_MAX, "usid");
		if (pair == null || value > pair.getB())
			redisTemplate.opsForHash().put(KEY_PAGE_VIEW_MAX, "usid",
					new Pair(day, value));
	}
}
