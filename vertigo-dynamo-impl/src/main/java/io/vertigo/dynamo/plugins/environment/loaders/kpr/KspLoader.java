/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.environment.loaders.kpr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslKspRule;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.StringUtil;

/**
 * Parser d'un fichier KSP.
 *
 * @author pchretien
 */
final class KspLoader {

	private final Charset charset;
	private final URL kspURL;

	/**
	 * Constructor.
	 *
	 * @param kspURL URL du fichier KSP.
	 * @param charset charset d'encoding du fihcier KSP à lire
	 */
	KspLoader(final URL kspURL, final Charset charset) {
		Assertion.checkNotNull(kspURL);
		Assertion.checkNotNull(charset);
		//-----
		this.kspURL = kspURL;
		this.charset = charset;
	}

	/**
	 * Chargement et analyse du fichier.
	 *
	 * @param dynamicModelrepository DynamicDefinitionRepository
	 */
	void load(final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkNotNull(dynamicModelrepository);
		try {
			final String s = parseFile();
			new DslKspRule(dynamicModelrepository)
					.parse(s, 0);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Echec de lecture du fichier KSP {0}\n{1}", kspURL.getFile(), e.getFullMessage());
			throw new WrappedException(message, e);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture du fichier KSP {0}\n{1}", kspURL.getFile(), e.getMessage());
			throw new WrappedException(message, e);
		}
	}

	/**
	 * Transforme un fichier en une chaine de caractÃ¨re parsable.
	 *
	 * @return String Chaine parsable correspondant au fichier.
	 * @throws IOException Erreur d'entrÃ©e/sortie
	 */
	private String parseFile() throws URISyntaxException, IOException {
		try (Stream<String> stream = Files.lines(Paths.get(kspURL.toURI()), charset)) {
			return stream
					.collect(Collectors.joining("\r\n", "", "\r\n"));
		}

	}
}
