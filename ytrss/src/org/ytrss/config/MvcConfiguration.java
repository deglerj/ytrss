package org.ytrss.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * The SpringMVC application context.
 *
 * Any @Controller classes will be picked up by component scanning. All other components are ignored as they will be picked up by the root application context.
 */
@EnableWebMvc
@Configuration
@ComponentScan(useDefaultFilters = false, basePackages = { "org.ytrss" }, includeFilters = { @ComponentScan.Filter(Controller.class) })
// Based on: https://github.com/jasonish/jetty-springmvc-jsp-template
public class MvcConfiguration extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/fonts/**").addResourceLocations("/WEB-INF/fonts/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(100));
		registry.addResourceHandler("/images/**").addResourceLocations("/WEB-INF/images/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(100));
		registry.addResourceHandler("/audiojs/**").addResourceLocations("/WEB-INF/audiojs/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(100));
	}

	@Override
	public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/**
	 * Basic setup for JSP views.
	 */
	@Bean
	public InternalResourceViewResolver configureInternalResourceViewResolver() {
		final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

}
