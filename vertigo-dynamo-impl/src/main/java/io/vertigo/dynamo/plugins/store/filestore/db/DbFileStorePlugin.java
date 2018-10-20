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
package io.vertigo.dynamo.plugins.store.filestore.db;

import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.core.component.Activeable;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.DataStream;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup
 */
public final class DbFileStorePlugin extends AbstractDbFileStorePlugin implements FileStorePlugin, Activeable {

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 * @author npiedeloup
	 */
	private enum DtoFields implements DtFieldName {
		FILE_NAME, MIME_TYPE, LAST_MODIFIED, LENGTH, FILE_DATA
	}

	private final FileManager fileManager;
	private final String storeDtDefinitionName;
	private DtDefinition storeDtDefinition;

	/**
	 * Constructor.
	 * @param name Store name
	 * @param storeDtDefinitionName Nom du dt de stockage
	 * @param fileManager Manager de gestion des fichiers
	 */
	@Inject
	public DbFileStorePlugin(
			@Named("name") final Optional<String> name,
			@Named("storeDtName") final String storeDtDefinitionName,
			final FileManager fileManager) {
		super(name);
		Assertion.checkArgNotEmpty(storeDtDefinitionName);
		Assertion.checkNotNull(fileManager);
		//-----
		this.storeDtDefinitionName = storeDtDefinitionName;
		this.fileManager = fileManager;
	}

	@Override
	public void start() {
		storeDtDefinition = Home.getApp().getDefinitionSpace().resolve(storeDtDefinitionName, DtDefinition.class);
	}

	@Override
	public void stop() {
		// nothing

	}

	/** {@inheritDoc} */
	@Override
	public FileInfo read(final FileInfoURI uri) {
		Assertion.checkNotNull(uri);
		checkDefinitionStoreBinding(uri.getDefinition());
		//-----
		final URI<Entity> dtoUri = URI.of(storeDtDefinition, uri.getKey());
		final Entity fileInfoDto = getStoreManager().getDataStore().readOne(dtoUri);
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder(getValue(fileInfoDto, DtoFields.FILE_DATA, DataStream.class));
		final String fileName = getValue(fileInfoDto, DtoFields.FILE_NAME, String.class);
		final String mimeType = getValue(fileInfoDto, DtoFields.MIME_TYPE, String.class);
		final Instant lastModified = getValue(fileInfoDto, DtoFields.LAST_MODIFIED, Instant.class);
		final Long length = getValue(fileInfoDto, DtoFields.LENGTH, Long.class);
		final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);
		return new DatabaseFileInfo(uri.getDefinition(), vFile);
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo create(final FileInfo fileInfo) {
		checkReadonly();
		Assertion.checkNotNull(fileInfo.getURI() == null, "Only file without any id can be created.");
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		//-----
		final Entity fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getStoreManager().getDataStore().create(fileInfoDto);
		//-----
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.checkNotNull(fileInfoDtoId, "ID  du fichier doit être renseignée.");
		final FileInfoURI uri = new FileInfoURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		checkReadonly();
		Assertion.checkNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		checkDefinitionStoreBinding(fileInfo.getDefinition());
		//-----
		final Entity fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final FileInfoURI uri) {
		checkReadonly();
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		checkDefinitionStoreBinding(uri.getDefinition());
		//-----
		final URI<Entity> dtoUri = URI.of(storeDtDefinition, uri.getKey());
		getStoreManager().getDataStore().delete(dtoUri);
	}

	private Entity createFileInfoDto(final FileInfo fileInfo) {
		//Il doit exister un DtObjet associé à storeDtDefinition avec la structure attendue.
		final Entity fileInfoDto = DtObjectUtil.createEntity(storeDtDefinition);
		//-----

		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.MIME_TYPE, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.LAST_MODIFIED, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.LENGTH, vFile.getLength());
		setValue(fileInfoDto, DtoFields.FILE_DATA, new VFileDataStream(vFile));

		if (fileInfo.getURI() != null) {
			setIdValue(fileInfoDto, fileInfo.getURI().getKey());
		}
		return fileInfoDto;
	}
}
