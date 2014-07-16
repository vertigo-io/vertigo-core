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
package io.vertigo.dynamo.plugins.environment.loaders.kpr;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.kernel.lang.Assertion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Parser d'un fichier KPR.
 * Un fichier KPR est un fichier qui liste l'ensemble des fichiers KSP du projet.
 *
 * @author  pchretien
 */
public final class KprLoaderPlugin implements LoaderPlugin {
	private static final String KPR_EXTENSION = ".kpr";
	private static final String KSP_EXTENSION = ".ksp";

	private final ResourceManager resourceManager;

	/**
	 * Constructeur.
	 * @param kprFileName Adresse du fichier KPR.
	 */
	@Inject
	public KprLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		this.resourceManager = resourceManager;
	}

	/** {@inheritDoc} */
	public void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final URL kprURL = resourceManager.resolve(resourcePath);

		for (final URL url : getKspFiles(kprURL, resourceManager)) {
			final KspLoader loader = new KspLoader(url);
			loader.load(dynamicModelrepository);
		}
	}

	/**
	 * récupère la liste des fichiers KSP correspondant à un KPR.
	 * @param kprURL fichier KPR
	 * @return List liste des fichiers KSP.
	 */
	private static List<URL> getKspFiles(final URL kprURL, final ResourceManager resourceManager) {
		try {
			return doGetKspFiles(kprURL, resourceManager);
		} catch (final Exception e) {
			throw new RuntimeException("Echec de lecture du fichier KPR " + kprURL.getFile(), e);
		}

	}

	private static List<URL> doGetKspFiles(final URL kprURL, final ResourceManager resourceManager) throws Exception {
		final List<URL> kspFileList = new ArrayList<>();
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(kprURL.openStream()))) {
			String line = reader.readLine();
			//-----------------------------------
			String path = kprURL.getPath();
			path = path.substring(0, path.lastIndexOf('/'));
			while (line != null) {
				final String fileName = line.trim();
				if (fileName.length() > 0) {
					URL url;
					// voir http://commons.apache.org/vfs/filesystems.html
					if (fileName.indexOf('!') != -1) {
						// pour client riche JavaWebStart (Jar, Tar, Zip)
						final String archFileUri = fileName.substring(fileName.indexOf('!') + 1).replace('\\', '/');
						url = resourceManager.resolve(archFileUri + '/' + fileName);
					} else {
						// Protocol : vfszip pour jboss par exemple
						url = new URL(kprURL.getProtocol() + ':' + path + '/' + fileName);
					}
					if (fileName.endsWith(KPR_EXTENSION)) {
						//kpr
						kspFileList.addAll(getKspFiles(url, resourceManager));
					} else if (fileName.endsWith(KSP_EXTENSION)) {
						//ksp
						kspFileList.add(url);
					} else {
						throw new RuntimeException("Type de fichier inconnu : " + fileName);
					}
				}
				line = reader.readLine();
			}
		}

		return kspFileList;
	}

	public String getType() {
		return "kpr";
	}

}
