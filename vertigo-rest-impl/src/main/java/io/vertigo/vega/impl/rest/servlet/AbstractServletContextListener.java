package io.vertigo.vega.impl.rest.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Abstract to start vertigo Home.
 * @author npiedeloup
 */
public abstract class AbstractServletContextListener implements ServletContextListener {

	/** Servlet listener */
	private final HomeServlerStarter servlerHomeStarter = new HomeServlerStarter();

	/** {@inheritDoc} */
	public final void contextInitialized(final ServletContextEvent servletContextEvent) {
		servlerHomeStarter.contextInitialized(servletContextEvent.getServletContext());
	}

	/** {@inheritDoc} */
	public final void contextDestroyed(final ServletContextEvent servletContextEvent) {
		servlerHomeStarter.contextDestroyed(servletContextEvent.getServletContext());
	}
}
