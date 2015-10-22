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
package io.vertigo.commons.plugins.resource.java;

import io.vertigo.core.spaces.resource.ResourceResolverPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.net.URL;

/**
 * Résolution des URL liées au classPath.
 * Cette résolution est en absolue.
 *
 * @author prahmoune
 */
public final class ClassPathResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	@Override
	public Option<URL> resolve(final String resource) {
		Assertion.checkNotNull(resource);
		//-----
		//le getClassLoader permet de se mettre en absolue (getClass().getRessource serait relatif)
		final URL url = getClassLoader().getResource(resource);
		return Option.option(url);
	}

	private static ClassLoader getClassLoader() {
		//On récupère le classLoader courant (celui qui a créé le thread).
		return Thread.currentThread().getContextClassLoader();
	}
}
