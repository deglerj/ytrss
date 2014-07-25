package org.ytrss.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.ytrss.db.SettingsService;

@Configuration
@EnableScheduling
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

	@Autowired
	private SettingsService	settingsService;

	@Override
	public Executor getAsyncExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public ScheduledExecutorService getScheduledExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}

	@Bean
	@Qualifier("downloader")
	public Executor getStreamDownloaderExecutor() {
		return Executors.newFixedThreadPool(settingsService.getSetting("downloaderThreads", Integer.class));
	}

	@Bean
	@Qualifier("transcoder")
	public Executor getTranscoderExecutor() {
		return Executors.newFixedThreadPool(settingsService.getSetting("transcoderThreads", Integer.class));
	}

}
