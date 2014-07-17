package org.ytrss;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.ytrss.config.RootConfiguration;
import org.ytrss.youtube.StreamMapEntryScorer;

//Based on: https://github.com/jasonish/jetty-springmvc-jsp-template
public class Main {

	public static void main(final String[] args) throws IOException {
		createDirectories();

		try {
			@SuppressWarnings("resource")
			final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

			final MutableBoolean webApplicationContextInitialized = new MutableBoolean(false);

			applicationContext.addApplicationListener(event -> {
				if (event instanceof ContextRefreshedEvent) {
					final ApplicationContext ctx = ((ContextRefreshedEvent) event).getApplicationContext();
					if (ctx instanceof GenericWebApplicationContext) {
						webApplicationContextInitialized.setTrue();
						;
					}
				}
			});

			applicationContext.registerShutdownHook();
			applicationContext.register(RootConfiguration.class);
			applicationContext.refresh();

			if (webApplicationContextInitialized.isFalse()) {
				log.error("Failed to initialize web application. Exiting...");
				System.exit(1);
			}

			log.info("Running");
		}
		catch (final Exception e) {
			log.error("Error starting application", e);
			System.exit(1);
		}
	}

	private static void createDirectories() throws IOException {
		final String baseDirectory = System.getProperty("user.home") + "/.ytrss";

		final File data = new File(baseDirectory + "/data");
		if (!data.exists()) {
			data.mkdirs();
		}

		final File videos = new File(baseDirectory + "/videos");
		if (!videos.exists()) {
			videos.mkdirs();
		}

		final File mp3s = new File(baseDirectory + "/mp3s");
		if (!mp3s.exists()) {
			mp3s.mkdirs();
		}
	}

	private static Logger	log	= LoggerFactory.getLogger(StreamMapEntryScorer.class);
}
