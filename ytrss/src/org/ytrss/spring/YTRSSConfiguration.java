package org.ytrss.spring;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.ytrss.db.DatabaseInitializer;
import org.ytrss.transcoders.JaveTranscoder;
import org.ytrss.transcoders.Transcoder;

@Configuration
@ComponentScan("org.ytrss")
@EnableWebMvc
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class YTRSSConfiguration extends WebMvcConfigurerAdapter implements AsyncConfigurer {

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("/WEB-INF/css/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(1));
		registry.addResourceHandler("/js/**").addResourceLocations("/WEB-INF/js/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(1));
		registry.addResourceHandler("/fonts/**").addResourceLocations("/WEB-INF/fonts/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(100));
		registry.addResourceHandler("/images/**").addResourceLocations("/WEB-INF/images/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(1));
	}

	@Override
	public void configureDefaultServletHandling(final DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public Executor getAsyncExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public DataSource getDataSource(final DatabaseInitializer initializer) {
		final JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:file:~/.ytrss/data/data");
		dataSource.setUser("sa");
		dataSource.setPassword("");

		initializer.initialize(dataSource);

		return dataSource;
	}

	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver() {
		final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	@Qualifier("streamDownloader")
	public Executor getStreamDownloaderExecutor() {
		return Executors.newFixedThreadPool(3);
	}

	@Bean
	public Transcoder getTranscoder() {
		return new JaveTranscoder();
	}

	@Bean
	@Qualifier("transcoder")
	public Executor getTranscoderExecutor() {
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	@Bean
	public JdbcTemplate jdbcTemplate(final DataSource dataSource) throws Exception {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager transactionManager(final DataSource dataSource) throws Exception {
		return new DataSourceTransactionManager(dataSource);
	}

}
