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
package io.vertigo.app.config.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.BootConfigBuilder;
import io.vertigo.app.config.LogConfig;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * @author npiedeloup, pchretien
 */
public final class XMLAppConfigBuilder implements Builder<AppConfig> {
	private final AppConfigBuilder appConfigBuilder = new AppConfigBuilder();

	public BootConfigBuilder beginBoot() {
		return appConfigBuilder.beginBoot();
	}

	/**
	* Append Config of a set of modules.
	 * @param relativeRootClass Class used to access files in a relative way.
	* @param xmlModulesParams properties used to configure the app
	* @param xmlModulesFileNames fileNames of the different xml files 
	* 
	* @return this builder
	*/
	public XMLAppConfigBuilder withModules(final Class relativeRootClass, final Properties xmlModulesParams, final String... xmlModulesFileNames) {
		Assertion.checkNotNull(relativeRootClass);
		Assertion.checkNotNull(xmlModulesParams);
		Assertion.checkNotNull(xmlModulesFileNames);
		//-----
		final List<URL> xmlModulesAsUrls = new ArrayList<>();
		for (final String xmlModulesFileName : xmlModulesFileNames) {
			xmlModulesAsUrls.add(createURL(xmlModulesFileName, relativeRootClass));
		}
		XMLModulesParser.parseAll(appConfigBuilder, xmlModulesParams, xmlModulesAsUrls);
		return this;
	}

	/**
	 * @param logConfig Config of logs
	 * @return  this builder
	 */
	public XMLAppConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		appConfigBuilder.beginBoot().withLogConfig(logConfig).endBoot();
		return this;
	}

	@Override
	public AppConfig build() {
		return appConfigBuilder.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URL non null
	 */
	private static URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}
}
