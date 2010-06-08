package org.ironrhino.core.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.util.JsonUtils;
import org.springframework.context.ApplicationContext;

@Singleton
@Named
public class SessionCompressorManager {

	private Log log = LogFactory.getLog(getClass());

	private TypeReference<Map<String, String>> type = new TypeReference<Map<String, String>>() {
	};

	@Inject
	private ApplicationContext ctx;

	private Collection<SessionCompressor> compressors;

	@PostConstruct
	public void afterPropertiesSet() {
		compressors = ctx.getBeansOfType(SessionCompressor.class).values();
	}

	public String compress(WrappedHttpSession session) {
		Map<String, Object> map = session.getAttrMap();
		Map<String, String> compressedMap = new HashMap<String, String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value != null && value instanceof String) {
				compressedMap.put(key, (String) value);
				continue;
			}
			SessionCompressor compressor = null;
			for (SessionCompressor var : compressors) {
				if (var.supportsKey(key)) {
					compressor = var;
					break;
				}
			}
			if (compressor != null) {
				String s = compressor.compress(value);
				if (StringUtils.isNotBlank(s))
					compressedMap.put(key, s);
			} else {
				log.error("No compressor for " + key + ",it won't be saved");
			}
		}
		if (!compressedMap.isEmpty())
			return JsonUtils.toJson(compressedMap);
		return null;
	}

	public void uncompress(WrappedHttpSession session, String str) {
		Map<String, Object> map = session.getAttrMap();
		if (StringUtils.isNotBlank(str)) {
			Map<String, String> compressedMap = null;
			try {
				compressedMap = JsonUtils.fromJson(str, type);
			} catch (Exception e) {
				log.info(e.getMessage(), e);
			}
			if (compressedMap != null)
				for (Map.Entry<String, String> entry : compressedMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					SessionCompressor compressor = null;
					for (SessionCompressor var : compressors) {
						if (var.supportsKey(key)) {
							compressor = var;
							break;
						}
					}
					if (compressor != null) {
						map.put(key, compressor.uncompress(value));
					} else {
						map.put(key, value);
					}
				}
		}
	}
}
