package io.vertigo.struts2.impl.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author npiedeloup
 */
public abstract class AbstractServletContextListener implements ServletContextListener {
	/** clés dans le fichier Web.xml */

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
