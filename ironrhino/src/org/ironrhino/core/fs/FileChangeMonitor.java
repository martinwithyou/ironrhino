package org.ironrhino.core.fs;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

public class FileChangeMonitor {

	private final Map<FileChangeListener, Long> listeners = new ConcurrentHashMap<FileChangeListener, Long>();

	private Timer timer;

	@Value("${fileChangeMonitor.period:5000}")
	private int period = 5000;

	@Autowired(required = false)
	@Qualifier("executorService")
	private ExecutorService executorService;

	@Autowired
	private ApplicationContext ctx;

	private boolean executorServiceNeedClose;

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public void addListener(FileChangeListener listener) {
		if (listener != null && listener.getFile() != null)
			listeners.put(listener, listener.getFile().lastModified());
	}

	public void removeListener(FileChangeListener listener) {
		listeners.remove(listener);
	}

	@PostConstruct
	public void start() {
		if (ctx != null)
			for (FileChangeListener listener : ctx.getBeansOfType(
					FileChangeListener.class).values())
				addListener(listener);
		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(2);
			executorServiceNeedClose = true;
		}
		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (Map.Entry<FileChangeListener, Long> entry : listeners
						.entrySet()) {
					final FileChangeListener listener = entry.getKey();
					long oldLastModified = entry.getValue();
					long newLastModified = listener.getFile().lastModified();
					if (oldLastModified == 0 && newLastModified > 0) {
						executorService.execute(new Runnable() {
							@Override
							public void run() {
								listener.onChange(FileChangeType.ADD);
							}
						});

					} else if (oldLastModified > 0 && newLastModified == 0) {
						executorService.execute(new Runnable() {
							@Override
							public void run() {
								listener.onChange(FileChangeType.REMOVE);
							}
						});
					} else if (oldLastModified < newLastModified) {
						executorService.execute(new Runnable() {
							@Override
							public void run() {
								listener.onChange(FileChangeType.UPDATE);
							}
						});
					}
					if (listener.isRemoved())
						listeners.remove(listener);
					else
						entry.setValue(newLastModified);
				}
			}
		}, period, period);
	}

	protected void run() {

	}

	@PreDestroy
	public void stop() {
		if (timer != null)
			timer.cancel();
		if (executorServiceNeedClose)
			executorService.shutdown();
	}

	public static abstract class FileChangeListener {

		protected File file;

		protected boolean removed;

		public FileChangeListener(File file) {

		}

		public abstract void onChange(FileChangeType type);

		public File getFile() {
			return this.file;
		}

		public boolean isRemoved() {
			return removed;
		}

		public void setRemoved(boolean removed) {
			this.removed = removed;
		}

		public void setFile(File file) {
			this.file = file;
		}

	}

	public static enum FileChangeType {
		ADD, UPDATE, REMOVE;
	}

}
