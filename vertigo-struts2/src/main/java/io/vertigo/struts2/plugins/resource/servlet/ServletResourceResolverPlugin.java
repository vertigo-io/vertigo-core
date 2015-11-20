/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.struts2.plugins.resource.servlet;

import io.vertigo.core.resource.ResourceResolverPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

/**
 * Résolution des URL liées à la servlet.
 * @author prahmoune
 */
public final class ServletResourceResolverPlugin implements ResourceResolverPlugin {
	private static WeakReference<ServletContext> servletContextRef;
	private final ServletContext servletContext;

	/**
	 * @param servletContext ServletContext
	 */
	public static synchronized void setServletContext(final ServletContext servletContext) {
		Assertion.checkNotNull(servletContext);
		//-----
		servletContextRef = new WeakReference<>(servletContext);
	}

	/**
	 * Constructor.
	 */
	public ServletResourceResolverPlugin() {
		Assertion.checkNotNull(servletContextRef.get(), "Ce servletContext n'est plus accessible");
		//-----
		servletContext = servletContextRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public Option<URL> resolve(final String resource) {
		Assertion.checkNotNull(resource);
		//-----
		// 2. On recherche dans le context de la webapp
		try {
			return Option.option(servletContext.getResource(resource));
		} catch (final MalformedURLException e) {
			return Option.none();
		}
	}
}
