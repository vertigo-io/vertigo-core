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
package io.vertigo;

import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.xml.XMLModulesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Charge l'environnement de test par defaut.
 * @author pchretien
 */
@Deprecated
public abstract class AbstractTestCaseJU4 extends AbstractTestCase2JU4 {

	/**
	 * @return fichier managers.xml (par defaut managers-test.xml)
	 */
	protected String getManagersXmlFileName() {
		return "./managers-test.xml";
	}

	/**
	 * @return fichier properties de paramétrage des managers (par defaut Option.none())
	 */
	protected Option<String> getPropertiesFileName() {
		return Option.none(); //par défaut pas de properties
	}

	/** {@inheritDoc} */
	@Override
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		componentSpaceConfiguilder.withLoader(new XMLModulesLoader(loadManagersXml(), loadProperties()));
	}

	private URL loadManagersXml() {
		final URL url = getClass().getResource(getManagersXmlFileName());
		Assertion.checkNotNull(url, "Configuration " + getManagersXmlFileName() + " non trouvée");
		return url;
	}

	private Properties loadProperties() {
		try {
			final Option<String> propertiesName = getPropertiesFileName();
			final Properties properties = new Properties();
			if (propertiesName.isDefined()) {
				try (final InputStream in = createURL(propertiesName.get()).openStream()) {
					properties.load(in);
				}
			}
			return properties;
		} catch (final IOException e) {
			throw new VRuntimeException(e);
		}
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 * 
	 * @param fileName Nom du fichier
	 * @return URN non null
	 */
	private URL createURL(final String fileName) {
		Assertion.checkArgNotEmpty(fileName);
		//---------------------------------------------------------------------
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath 
			final URL url = getClass().getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}
}
