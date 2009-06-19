package org.ironrhino.core.ext.spring.security;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.intercept.web.FilterInvocationDefinitionSourceEditor;
import org.springframework.util.Assert;

public class RefreshableFilterInvocationDefinitionSource extends
		AbstractRefreshableTargetSource implements ResourceLoaderAware,
		BeanNameAware, InitializingBean {

	private Resource definitionResource;

	private String beanName;

	private String directory = "/WEB-INF/conf/";

	private ResourceLoader resourceLoader;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	public String getDirectory() {
		return directory.endsWith("/") ? directory : directory + "/";
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(directory);
		definitionResource = resourceLoader.getResource(getDirectory()
				+ beanName + ".conf");
	}

	public void setDefinitionAsText(String definition) {
		w.lock();
		try {
			FileUtils.writeStringToFile(resourceLoader.getResource(
					getDirectory() + beanName + ".conf").getFile(), definition,
					"UTF-8");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			w.unlock();
		}
	}

	public String getDefinitionAsText() {
		r.lock();
		try {
			return FileUtils.readFileToString(definitionResource.getFile(),
					"UTF-8");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return "";
		} finally {
			r.unlock();
		}
	}

	protected Object freshTarget() {
		r.lock();
		try {
			StringBuilder sb = new StringBuilder();
			LineIterator it = FileUtils.lineIterator(definitionResource
					.getFile(), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				if (!line.startsWith("#"))
					sb.append(line + "\n");
			}
			LineIterator.closeQuietly(it);
			logger
					.info("loaded Spring Security FilterInvocationDefinition config file["
							+ definitionResource.getURL() + "]");
			FilterInvocationDefinitionSourceEditor configEditor = new FilterInvocationDefinitionSourceEditor();
			configEditor.setAsText(sb.toString());
			return configEditor.getValue();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			r.unlock();
		}

	}

}
