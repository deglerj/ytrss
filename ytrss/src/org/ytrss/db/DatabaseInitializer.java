package org.ytrss.db;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationInfo;

@Component
public class DatabaseInitializer {

	private static Log	log	= LogFactory.getLog(DatabaseInitializer.class);

	public void initialize(DataSource dataSource) {
		log.info("Preparing DB initialization");
		final Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setLocations("org.ytrss");

		final MigrationInfo current = flyway.info().current();
		if (current == null) {
			log.info("No existing DB found");
		}
		else {
			log.info("Current DB version is " + current.getVersion());
		}

		log.info("Starting DB initialization");
		flyway.migrate();

		log.info("Completed DB initialization. DB version is now " + flyway.info().current().getVersion());
	}

}
