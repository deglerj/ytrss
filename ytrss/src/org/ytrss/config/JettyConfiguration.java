package org.ytrss.config;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Configure the embedded Jetty server and the SpringMVC dispatcher servlet.
 */
@Configuration
// Based on: https://github.com/jasonish/jetty-springmvc-jsp-template
public class JettyConfiguration {

	@Autowired
	private ApplicationContext	applicationContext;

	@Value("${jetty.port:8080}")
	private int					jettyPort;

	/**
	 * Jetty Server bean.
	 * <p/>
	 * Instantiate the Jetty server.
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server jettyServer() throws IOException {

		/* Create the server. */
		final Server server = new Server();

		/* Create a basic connector. */
		final ServerConnector httpConnector = new ServerConnector(server);
		httpConnector.setPort(jettyPort);
		server.addConnector(httpConnector);

		server.setHandler(jettyWebAppContext());

		return server;
	}

	@Bean
	public WebAppContext jettyWebAppContext() throws IOException {

		final WebAppContext ctx = new WebAppContext();
		ctx.setContextPath("/");
		ctx.setWar(new ClassPathResource("webapp").getURI().toString());

		/* Disable directory listings if no index.html is found. */
		ctx.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

		/*
		 * Create the root web application context and set it as a servlet attribute so the dispatcher servlet can find it.
		 */
		final GenericWebApplicationContext webApplicationContext = new GenericWebApplicationContext();
		webApplicationContext.setParent(applicationContext);
		webApplicationContext.refresh();
		ctx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);

		ctx.addEventListener(new WebAppInitializer());

		return ctx;
	}

}
