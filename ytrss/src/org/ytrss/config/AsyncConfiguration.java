package org.ytrss.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public ScheduledExecutorService getScheduledExecutorService() {
		return Executors.newScheduledThreadPool(1);
	}

	@Bean
	@Qualifier("streamDownloader")
	public Executor getStreamDownloaderExecutor() {
		return Executors.newSingleThreadExecutor();
	}

	@Bean
	@Qualifier("transcoder")
	public Executor getTranscoderExecutor() {
		return Executors.newSingleThreadExecutor();
	}

}
