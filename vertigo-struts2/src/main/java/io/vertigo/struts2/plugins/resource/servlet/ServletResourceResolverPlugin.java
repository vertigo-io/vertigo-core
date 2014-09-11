package io.vertigo.struts2.plugins.resource.servlet;

import io.vertigo.commons.impl.resource.ResourceResolverPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

/**
 * Résolution des URL li�es à la servlet.
 * @author prahmoune
 */
public final class ServletResourceResolverPlugin implements ResourceResolverPlugin {
	private static WeakReference<ServletContext> servletContextRef;

	public static void setServletContext(final ServletContext servletContext) {
		Assertion.checkNotNull(servletContext);
		//---------------------------------------------------------------------
		servletContextRef = new WeakReference<>(servletContext);
	}

	private final ServletContext servletContext;

	public ServletResourceResolverPlugin() {
		Assertion.checkNotNull(servletContextRef.get(), "Ce servletContext n'est plus accessible");
		//---------------------------------------------------------------------
		servletContext = servletContextRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public Option<URL> resolve(final String resource) {
		Assertion.checkNotNull(resource);
		// ---------------------------------------------------------------------
		// 2. On recherche dans le context de la webapp
		try {
			return Option.some(servletContext.getResource(resource));
		} catch (final MalformedURLException e) {
			return Option.none();
		}
	}
}
