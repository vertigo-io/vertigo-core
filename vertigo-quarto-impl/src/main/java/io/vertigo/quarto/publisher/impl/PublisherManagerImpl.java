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
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.PublisherManager;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Impl�mentation standard du manager des �ditions.
 *
 * @author pchretien, npiedeloup
 * @version $Id: PublisherManagerImpl.java,v 1.8 2014/01/28 18:53:45 pchretien Exp $
 */
public final class PublisherManagerImpl implements PublisherManager {
	@Inject
	private MergerPlugin mergerPlugin;
	private final FileManager fileManager;
	private final WorkManager workManager;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 * @param scriptManager Manager des scripts
	 * @param fileManager Manager des fichiers
	 */
	@Inject
	public PublisherManagerImpl(final WorkManager workManager, final ScriptManager scriptManager, final FileManager fileManager) {
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(scriptManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	public void publishASync(final String fileName, final URL modelFileURL, final PublisherData data, final WorkResultHandler<KFile> workResultHandler) {
		Assertion.checkNotNull(fileName);
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		WorkItem<KFile, ?> workItem = new WorkItem<>(new Callable<KFile>() {
			public KFile call() {
				return publish(fileName, modelFileURL, data);
			}
		}, workResultHandler);
		workManager.schedule(workItem);
	}

	/** {@inheritDoc} */
	public KFile publish(final String fileName, final URL modelFileURL, final PublisherData data) {
		Assertion.checkNotNull(fileName);
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		try {
			return generateFile(fileName, modelFileURL, data);
		} catch (final IOException e) {
			final String msg = "La generation du fichier a echoue.<!-- " + e.getMessage() + "--> pour le fichier " + fileName;
			throw new RuntimeException(msg, e);
		}
	}

	private KFile generateFile(final String fileName, final URL modelFileURL, final PublisherData data) throws IOException {
		// attention : pour ce generateFile le File retourn� n'a pas le nom de fichier donn� dans
		// mergeParameter.getOuputFileName() car on utilise cette m�thode notamment dans send
		// ci-dessus pour plusieurs utilisateurs simultan�ment avec probablement le m�me
		// mergeParameter.getOuputFileName()
		//----------------------------------------------------------------------
		final File fileToExport = mergerPlugin.execute(modelFileURL, data);
		return fileManager.createFile(fileName, mergerPlugin.getPublisherFormat().getMimeType(), fileToExport);
	}
}
