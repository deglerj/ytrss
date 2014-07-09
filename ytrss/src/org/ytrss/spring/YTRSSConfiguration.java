package org.ytrss.spring;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.ytrss.db.DatabaseInitializer;

@Configuration
@ComponentScan("org.ytrss")
@EnableWebMvc
public class YTRSSConfiguration {

	@Bean
	public DataSource getDataSource(DatabaseInitializer initializer) {
		JDBCPool dataSource = new JDBCPool();
		dataSource.setUrl("jdbc:hsqldb:file:~/.ytrss/data/data");
		dataSource.setUser("sa");
		dataSource.setPassword("");

		initializer.initialize(dataSource);

		return dataSource;
	}

	@Bean
	public UrlBasedViewResolver setupViewResolver() {
		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		resolver.setViewClass(JstlView.class);
		return resolver;
	}

}
