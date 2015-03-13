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
package io.vertigo.quarto.publisher.impl;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.quarto.publisher.PublisherManager;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

/**
 * Implémentation standard du manager des éditions.
 *
 * @author pchretien, npiedeloup
 */
public final class PublisherManagerImpl implements PublisherManager {
	private final MergerPlugin mergerPlugin;
	private final FileManager fileManager;

	/**
	 * Constructeur.
	 * @param scriptManager Manager des scripts
	 * @param fileManager Manager des fichiers
	 */
	@Inject
	public PublisherManagerImpl(final ScriptManager scriptManager, final FileManager fileManager, final MergerPlugin mergerPlugin) {
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(mergerPlugin);
		//-----
		this.fileManager = fileManager;
		this.mergerPlugin = mergerPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public VFile publish(final String fileName, final URL modelFileURL, final PublisherData data) {
		Assertion.checkNotNull(fileName);
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//-----
		try {
			return generateFile(fileName, modelFileURL, data);
		} catch (final IOException e) {
			final String msg = "La generation du fichier a echoue.<!-- " + e.getMessage() + "--> pour le fichier " + fileName;
			throw new RuntimeException(msg, e);
		}
	}

	private VFile generateFile(final String fileName, final URL modelFileURL, final PublisherData data) throws IOException {
		// attention : pour ce generateFile le File retourné n'a pas le nom de fichier donné dans
		// mergeParameter.getOuputFileName() car on utilise cette méthode notamment dans send
		// ci-dessus pour plusieurs utilisateurs simultanément avec probablement le même
		// mergeParameter.getOuputFileName()
		//-----
		final File fileToExport = mergerPlugin.execute(modelFileURL, data);
		return fileManager.createFile(fileName, mergerPlugin.getPublisherFormat().getMimeType(), fileToExport);
	}
}
