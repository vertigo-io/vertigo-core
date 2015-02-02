package io.vertigo.boot.xml;

import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.config.ModuleConfig;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class XMLAppConfigBuilder implements Builder<AppConfig> {
	private final AppConfigBuilder appConfigBuilder = new AppConfigBuilder();

	public XMLAppConfigBuilder withSilence(final boolean silence) {
		appConfigBuilder.withSilence(silence);
		return this;
	}

	public XMLAppConfigBuilder withLogConfig(final String logFileName) {
		appConfigBuilder.withLogConfig(new LogConfig(logFileName));
		return this;
	}

	/**
	* Append Config of a set of modules.
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
		final List<ModuleConfig> moduleConfigs = XMLModulesParser.parseAll(xmlModulesParams, xmlModulesAsUrls);
		appConfigBuilder.withModules(moduleConfigs);
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
	 * @return URN non null
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
