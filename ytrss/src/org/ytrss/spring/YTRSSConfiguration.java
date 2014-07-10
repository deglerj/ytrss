package org.ytrss.spring;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.ytrss.db.DatabaseInitializer;

@Configuration
@ComponentScan("org.ytrss")
@EnableWebMvc
public class YTRSSConfiguration extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("/WEB-INF/css/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(1));
		registry.addResourceHandler("/js/**").addResourceLocations("/WEB-INF/js/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(1));
		registry.addResourceHandler("/fonts/**").addResourceLocations("/WEB-INF/fonts/").setCachePeriod((int) TimeUnit.DAYS.toSeconds(100));
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public DataSource getDataSource(DatabaseInitializer initializer) {
		JDBCPool dataSource = new JDBCPool();
		dataSource.setUrl("jdbc:hsqldb:file:~/.ytrss/data/data");
		dataSource.setUser("sa");
		dataSource.setPassword("");

		initializer.initialize(dataSource);

		return dataSource;
	}

}
