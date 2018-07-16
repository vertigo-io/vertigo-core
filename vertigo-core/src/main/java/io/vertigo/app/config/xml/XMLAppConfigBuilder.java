/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.BootConfigBuilder;
import io.vertigo.app.config.LogConfig;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.WrappedException;

/**
 * @author npiedeloup, pchretien
 */
public final class XMLAppConfigBuilder implements Builder<AppConfig> {
	private final AppConfigBuilder appConfigBuilder = AppConfig.builder();
	
	/**
	 * Begin the boot config of the app.
	 * @return the bootConfig builder
	 */
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
		final List<URL> xmlModulesAsUrls = Stream.of(xmlModulesFileNames)
				.map(xmlModulesFileName -> createURL(xmlModulesFileName, relativeRootClass))
				.collect(Collectors.toList());

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

	/** {@inheritDoc} */
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
	private  URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			try {
				final String resourceName = resolveName(fileName, relativeRootClass);
				final Optional<URI> optUri = relativeRootClass.getModule().getLayer().configuration().findModule(relativeRootClass.getModule().getName()).get().reference().open().find(resourceName);
				Assertion.checkState(optUri.isPresent(), "Impossible de récupérer le fichier [" + fileName + "]");
				return optUri.get().toURL();
			} catch (IOException e1) {
				throw WrappedException.wrap(e1);
			}
		}
	}
	
	/**
     * Add a package name prefix if the name is not absolute Remove leading "/"
     * if name is absolute
     */
    private String resolveName(String myName, final Class clazz ) {
    	String name  = myName;
        if (!name.startsWith("/")) {
            Class<?> c = clazz;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getPackageName();
            if (baseName != null && !baseName.isEmpty()) {
                name = baseName.replace('.', '/') + "/" + name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }
}
