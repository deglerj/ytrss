package org.ytrss.config;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

//Based on: https://github.com/jasonish/jetty-springmvc-jsp-template
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer implements ServletContextListener {

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
	}

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		try {
			onStartup(servletContextEvent.getServletContext());
		}
		catch (final ServletException e) {
			logger.error("Failed to initialize web application", e);
			System.exit(0);
		}
	}

	@Override
	public void onStartup(final ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);

		final Dynamic securityFilter = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy());
		securityFilter.setAsyncSupported(true);
		securityFilter.addMappingForUrlPatterns(null, false, "/*");
	}

	/**
	 * See {@link AbstractAnnotationConfigDispatcherServletInitializer}.
	 */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	/**
	 * Set the application context for the Spring MVC web tier.
	 *
	 * @See {@link AbstractAnnotationConfigDispatcherServletInitializer}
	 */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { MvcConfiguration.class, WebSocketConfiguration.class };
	}

	/**
	 * Map the Spring MVC servlet as the root.
	 *
	 * @See {@link AbstractAnnotationConfigDispatcherServletInitializer}
	 */
	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}
}
