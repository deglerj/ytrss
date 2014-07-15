package org.ytrss.config;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.ytrss.db.DatabaseInitializer;

@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration {

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
	public JdbcTemplate jdbcTemplate(final DataSource dataSource) throws Exception {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager transactionManager(final DataSource dataSource) throws Exception {
		return new DataSourceTransactionManager(dataSource);
	}

}
