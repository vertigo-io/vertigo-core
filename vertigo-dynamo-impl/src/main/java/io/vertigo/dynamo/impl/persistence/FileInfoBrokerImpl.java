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
package io.vertigo.dynamo.impl.persistence;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.impl.persistence.logical.LogicalFileStore;
import io.vertigo.dynamo.persistence.FileInfoBroker;
import io.vertigo.lang.Assertion;

/**
 * Implémentation Standard du Broker.
 * Cette implémentation s'appuie sur le concept de Store.
 * Un store définit les modalités du stockage
 * alors que le broker se concentre sur la problématique des accès aux ressources.
 * @author pchretien
 */
final class FileInfoBrokerImpl implements FileInfoBroker {
	private final FileStore fileStore;

	/**
	 * Constructeur.
	 * Une fois le broker construit la configuration est bloquée.
	 * @param brokerConfiguration Configuration du broker
	 */
	FileInfoBrokerImpl(final BrokerConfigurationImpl brokerConfiguration) {
		Assertion.checkNotNull(brokerConfiguration);
		//-----
		fileStore = new LogicalFileStore(brokerConfiguration.getLogicalFileStoreConfiguration());
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		Assertion.checkNotNull(fileInfo);
		//-----
		fileStore.create(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.checkNotNull(fileInfo);
		//-----
		fileStore.update(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteFileInfo(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri);
		//-----
		fileStore.remove(uri);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo getFileInfo(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final FileInfo fileInfo = fileStore.load(uri);
		//-----
		Assertion.checkNotNull(fileInfo, "Le fichier {0} n''a pas été trouvé", uri);
		return fileInfo;
	}
}
