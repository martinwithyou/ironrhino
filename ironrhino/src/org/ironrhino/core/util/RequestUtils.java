package org.ironrhino.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class RequestUtils {

	public static String getRemoteAddr(HttpServletRequest request) {
		String addr = request.getHeader("X-Forward-For");
		int index = 0;
		if (StringUtils.isNotBlank(addr) && (index = addr.indexOf(',')) > 0)
			addr = addr.substring(0, index);
		return StringUtils.isNotBlank(addr) ? addr : request.getRemoteAddr();
	}

	public static String getBaseUrl(HttpServletRequest request) {
		StringBuffer sb = request.getRequestURL();
		sb.delete(sb.length() - request.getServletPath().length(), sb.length());
		return sb.toString();
	}

	public static String getBaseUrl(HttpServletRequest request, boolean secured) {
		String host = request.getServerName();
		String schema = request.getScheme();
		if ((schema.equalsIgnoreCase("https") && secured)
				|| (schema.equalsIgnoreCase("http") && !secured)) {
			String url = request.getRequestURL().toString();
			return url.substring(0, url.indexOf(request.getServletPath()));
		}
		int port = request.getServerPort();
		String contextPath = request.getContextPath();
		StringBuilder sb = new StringBuilder();
		sb.append(secured ? "https://" : "http://");
		sb.append(host);
		if (secured) {
			if (port == 8080)
				sb.append(":8443");
		} else {
			if (port == 8443)
				sb.append(":8080");
		}
		sb.append(contextPath);
		return sb.toString();
	}

	public static String getCookieValue(HttpServletRequest request,
			String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies)
			if (cookieName.equalsIgnoreCase(cookie.getName()))
				try {
					return URLDecoder.decode(cookie.getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return cookie.getValue();
				}
		return null;
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String cookieValue) {
		saveCookie(request, response, cookieName, cookieValue, false);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, boolean global) {
		saveCookie(request, response, cookieName, cookieValue, -1, global);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge) {
		saveCookie(request, response, cookieName, cookieValue, maxAge, false);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge, boolean global) {
		String domain = null;
		String path = "".equals(request.getContextPath()) ? "/" : request
				.getContextPath();
		if (global) {
			domain = parseGlobalDomain(request.getServerName());
			path = "/";
		}
		saveCookie(request, response, cookieName, cookieValue, maxAge, domain,
				path);
	}

	public static void saveCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName,
			String cookieValue, int maxAge, String domain, String path) {
		try {
			cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		Cookie cookie = new Cookie(cookieName, cookieValue);
		if (StringUtils.isNotBlank(domain))
			cookie.setDomain(domain);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName) {
		deleteCookie(request, response, cookieName, false);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, boolean global) {
		deleteCookie(request, response, cookieName, "".equals(request
				.getContextPath()) ? "/" : request.getContextPath(), global);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String path) {
		deleteCookie(request, response, cookieName, path, false);
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String cookieName, String path,
			boolean global) {
		String domain = null;
		if (global) {
			domain = parseGlobalDomain(request.getServerName());
			path = "/";
		}
		Cookie cookie = new Cookie(cookieName, null);
		if (StringUtils.isNotBlank(domain))
			cookie.setDomain(domain);
		cookie.setMaxAge(0);
		cookie.setPath(path);
		response.addCookie(cookie);
	}

	private static String parseGlobalDomain(String host) {
		boolean topDouble = false;
		for (String s : topDoubleDomains) {
			if (host.endsWith(s)) {
				topDouble = true;
				break;
			}
		}
		String[] array = host.split("\\.");
		if (!topDouble && array.length >= 2) {
			StringBuilder sb = new StringBuilder();
			sb.append('.');
			sb.append(array[array.length - 2]);
			sb.append('.');
			sb.append(array[array.length - 1]);
			return sb.toString();
		} else if (topDouble && array.length >= 3) {
			StringBuilder sb = new StringBuilder();
			sb.append('.');
			sb.append(array[array.length - 3]);
			sb.append('.');
			sb.append(array[array.length - 2]);
			sb.append('.');
			sb.append(array[array.length - 1]);
			return sb.toString();
		}
		return null;
	}

	private static String[] topDoubleDomains = new String[] { ".com.cn",
			".edu.cn", ".org.cn", ".net.cn", ".co.uk", "co.kr", "co.jp" };

}
