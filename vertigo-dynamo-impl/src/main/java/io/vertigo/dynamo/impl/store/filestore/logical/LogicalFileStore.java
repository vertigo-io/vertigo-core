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
package io.vertigo.dynamo.impl.store.filestore.logical;

import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.lang.Assertion;

/**
 * Permet de gérer le stockage des documents.
 * Transpose en store physique les appels logiques.
 *
 * @author npiedeloup
 */
public final class LogicalFileStore implements FileStorePlugin {
	private final LogicalFileStoreConfig logicalFileStoreConfiguration;

	/**
	 * Constructeur.
	 * @param logicalFileStoreConfiguration Configuration logique des stores physiques.
	 */
	public LogicalFileStore(final LogicalFileStoreConfig logicalFileStoreConfiguration) {
		Assertion.checkNotNull(logicalFileStoreConfiguration);
		//-----
		this.logicalFileStoreConfiguration = logicalFileStoreConfiguration;
	}

	private FileStorePlugin getPhysicalStore(final FileInfoDefinition fileInfoDefinition) {
		return logicalFileStoreConfiguration.getPhysicalFileStore(fileInfoDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo load(final FileInfoURI uri) {
		final FileInfoDefinition fileInfoDefinition = uri.getDefinition();
		return getPhysicalStore(fileInfoDefinition).load(uri);
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		getPhysicalStore(fileInfo.getDefinition()).create(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		getPhysicalStore(fileInfo.getDefinition()).update(fileInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final FileInfoURI uri) {
		final FileInfoDefinition fileInfoDefinition = uri.getDefinition();
		getPhysicalStore(fileInfoDefinition).remove(uri);
	}
}
