package org.ytrss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Controller;
import org.ytrss.transcoders.FFMPEGCommandTranscoder;
import org.ytrss.transcoders.Transcoder;

import com.google.common.eventbus.EventBus;

/**
 * The root application context.
 * <p/>
 * Scanning is enabled but will skip @Configuration and @Controller classes.
 *
 * @Configuration classes are skipped to prevent picking theses ones up again as these files are in the scan path. @Controller classes will be picked up by
 *                MvcConfiguration.
 */
@Configuration
@Import({ JettyConfiguration.class, AsyncConfiguration.class, DatabaseConfiguration.class, SecurityConfiguration.class })
@ComponentScan(basePackages = { "org.ytrss" }, excludeFilters = { @ComponentScan.Filter(Controller.class), @ComponentScan.Filter(Configuration.class) })
// Based on: https://github.com/jasonish/jetty-springmvc-jsp-template
public class RootConfiguration {

	/**
	 * Allows access to properties. eg @Value("${jetty.port}").
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public ConversionService getConversionService() {
		return new DefaultConversionService();
	}

	@Bean
	public EventBus getEventBus() {
		return new EventBus();
	}

	@Bean
	public Transcoder getTranscoder() {
		return new FFMPEGCommandTranscoder();
	}

}
