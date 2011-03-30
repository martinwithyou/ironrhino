package org.ironrhino.core.spring.security.password;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.encoding.PasswordEncoder;

public class MultiVersionPasswordEncoder implements PasswordEncoder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private SortedMap<Integer, VersionedPasswordEncoder> map;

	private List<VersionedPasswordEncoder> versions;

	public void setVersions(List<VersionedPasswordEncoder> versions) {
		this.versions = versions;
	}

	@PostConstruct
	public void afterPropertiesSet() {
		if (versions == null || versions.size() == 0) {
			logger.error("no PasswordDigester found");
			return;
		}
		map = new TreeMap<Integer, VersionedPasswordEncoder>();
		for (VersionedPasswordEncoder pd : versions) {
			map.put(pd.getVersion(), pd);
		}
	}

	public String encodePassword(String rawPass, Object salt) {
		VersionedPasswordEncoder pd = map.get(map.lastKey());
		return new StringBuilder(pd.encodePassword(rawPass, salt)).append('#')
				.append(pd.getVersion()).toString();
	}

	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		if (encPass == null)
			encPass = "";
		if (rawPass == null)
			rawPass = "";
		int i = encPass.indexOf('#');
		int version = 1;
		if (i > 0) {
			version = Integer.valueOf(encPass.substring(i + 1));
			encPass = encPass.substring(0, i);
		}
		VersionedPasswordEncoder pd = map.get(version);
		if (pd == null) {
			pd = map.get(map.lastKey());
			logger.warn(
					"no PasswordDigester of version {}, use version {} instead ",
					version, map.lastKey());
		}
		return encPass.equals(pd.encodePassword(rawPass, salt));
	}

	public boolean isLastVersion(String encPass) {
		int lastVersion = map.lastKey();
		int version = 1;
		int i = encPass.indexOf('#');
		if (i > 0)
			version = Integer.valueOf(encPass.substring(i + 1));
		return version == lastVersion;
	}

}