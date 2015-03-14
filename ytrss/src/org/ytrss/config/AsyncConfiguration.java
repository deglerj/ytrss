package org.ytrss.config;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.ytrss.Ripper;
import org.ytrss.db.SettingsService;

import com.google.common.base.Throwables;

@Configuration
@EnableScheduling
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

	private static class LoggingThreadPoolExecutor extends ThreadPoolExecutor {
		public LoggingThreadPoolExecutor(final int threads) {
			super(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		}

		@Override
		public <T> Future<T> submit(final Callable<T> task) {
			final Callable<T> wrappedTask = () -> {
				try {
					return task.call();
				}
				catch (final Throwable t) {
					log.error("Uncaught exception in thread pool", t);
					throw Throwables.propagate(t);
				}
			};
			return super.submit(wrappedTask);
		}
	}

	private static Logger	log	= LoggerFactory.getLogger(Ripper.class);

	@Autowired
	private SettingsService	settingsService;

	@Override
	public Executor getAsyncExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) -> log.error(String.format("Unexpected error occurred invoking async " + "method '%s'.", method), ex);
	}

	@Bean
	public ScheduledExecutorService getScheduledExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}

	@Bean
	@Qualifier("downloader")
	public Executor getStreamDownloaderExecutor() {
		return new LoggingThreadPoolExecutor(settingsService.getSetting("downloaderThreads", Integer.class));
	}

	@Bean
	@Qualifier("transcoder")
	public Executor getTranscoderExecutor() {
		return new LoggingThreadPoolExecutor(settingsService.getSetting("transcoderThreads", Integer.class));
	}

}
