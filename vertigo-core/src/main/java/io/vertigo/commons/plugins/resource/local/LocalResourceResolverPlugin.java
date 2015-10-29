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
package io.vertigo.commons.plugins.resource.local;

import io.vertigo.core.resource.ResourceResolverPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Résolution des URL liées à l'emplacement local.
 *
 * @author prahmoune
 */
public final class LocalResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	@Override
	public Option<URL> resolve(final String resource) {
		Assertion.checkNotNull(resource);
		//-----
		try {
			return Option.option(new File(resource).toURI().toURL());
		} catch (final MalformedURLException e) {
			return Option.none();
		}
	}
}
