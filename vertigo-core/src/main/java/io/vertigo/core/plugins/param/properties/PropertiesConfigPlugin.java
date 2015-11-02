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
package io.vertigo.core.plugins.param.properties;

import io.vertigo.core.param.ConfigPlugin;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Plugin de gestion de configuration de fichiers properties.
 *
 * @author skerdudou
 */
public final class PropertiesConfigPlugin implements ConfigPlugin {
	private final Properties properties;
	private final String managedConfigPath;

	/**
	 * Constructeur.
	 *
	 * @param resourceManager Selector
	 * @param url Url du fichier XML de configuration
	 * @throws IOException erreur de lecture du fichier
	 */
	@Inject
	public PropertiesConfigPlugin(final ResourceManager resourceManager, @Named("url") final String url, @Named("configPath") final String configPath) throws IOException {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkArgNotEmpty(url);
		Assertion.checkArgNotEmpty(configPath);
		//-----
		final URL configURL = resourceManager.resolve(url);
		managedConfigPath = configPath;
		properties = loadProperties(configURL);
	}

	private static Properties loadProperties(final URL configURL) throws IOException {
		try (final InputStream input = configURL.openStream()) {
			final Properties tmpProperties = new Properties();
			tmpProperties.load(input);
			return tmpProperties;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Option<String> getValue(final String configPath, final String property) {
		Assertion.checkArgNotEmpty(configPath);
		Assertion.checkArgNotEmpty(property);
		//-----
		return managedConfigPath.equals(configPath) ? Option.<String> option(properties.getProperty(property)) : Option.<String> none();
	}
}
