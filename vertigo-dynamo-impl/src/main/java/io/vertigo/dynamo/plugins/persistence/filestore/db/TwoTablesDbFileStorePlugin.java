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
package io.vertigo.dynamo.plugins.persistence.filestore.db;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.FileInfoURI;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.persistence.filestore.FileStorePlugin;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;

/**
 * Permet de gérer le CRUD sur un fichier stocké sur deux tables (Méta données / Données).
 *
 * @author sezratty
 */
public final class TwoTablesDbFileStorePlugin implements FileStorePlugin {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 */
	private static enum DtoFields {
		FILE_NAME, MIME_TYPE, LAST_MODIFIED, LENGTH, FILE_DATA, FMD_ID, FDT_ID
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;

	/**
	 * Constructeur.
	 *
	 * @param fileManager Manager de gestion des fichiers
	 */
	@Inject
	public TwoTablesDbFileStorePlugin(final FileManager fileManager) {
		Assertion.checkNotNull(fileManager);
		//-----
		readOnly = false;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo load(final FileInfoURI fileInfoUri) {
		// Ramène FileMetada
		final URI<DtObject> dtoMetaDataUri = createMetaDataURI(fileInfoUri);
		final DtObject fileMetadataDto = getPersistenceManager().getBroker().get(dtoMetaDataUri);
		final Object fdtId = TwoTablesDbFileStorePlugin.<Object> getValue(fileMetadataDto, DtoFields.FDT_ID);

		// Ramène FileData
		final URI<DtObject> dtoDataUri = createDataURI(fileInfoUri.<FileInfoDefinition> getDefinition(), fdtId);
		final DtObject fileDataDto = getPersistenceManager().getBroker().get(dtoDataUri);
		// Construction du vFile.
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder((DataStream) getValue(fileDataDto, DtoFields.FILE_DATA));
		final String fileName = (String) getValue(fileMetadataDto, DtoFields.FILE_NAME);
		final String mimeType = (String) getValue(fileMetadataDto, DtoFields.MIME_TYPE);
		final Date lastModified = (Date) getValue(fileMetadataDto, DtoFields.LAST_MODIFIED);
		final Long length = (Long) getValue(fileMetadataDto, DtoFields.LENGTH);
		final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

		//TODO passer par une factory de FileInfo à partir de la FileInfoDefinition (comme DomainFactory)
		final FileInfo fileInfo = new DatabaseFileInfo(fileInfoUri.getDefinition(), vFile);
		fileInfo.setURIStored(fileInfoUri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkArgument(fileInfo.getURI() == null, "Only file without any id can be created");
		//-----
		final DtObject fileMetadataDto = createMetadataDtObject(fileInfo);
		final DtObject fileDataDto = createDataDtObject(fileInfo);
		//-----
		getPersistenceManager().getBroker().create(fileDataDto);
		setValue(fileMetadataDto, DtoFields.FDT_ID, DtObjectUtil.getId(fileDataDto));
		getPersistenceManager().getBroker().create(fileMetadataDto);
		final FileInfoURI fileInfoUri = createURI(fileInfo.getDefinition(), DtObjectUtil.getId(fileMetadataDto));
		fileInfo.setURIStored(fileInfoUri);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkArgument(fileInfo.getURI() != null, "Only file with id can be updated");
		//-----
		final DtObject fileMetadataDto = createMetadataDtObject(fileInfo);
		final DtObject fileDataDto = createDataDtObject(fileInfo);
		//-----
		setValue(fileMetadataDto, DtoFields.FMD_ID, fileInfo.getURI().getKey());
		// Chargement du FDT_ID
		final URI<DtObject> dtoMetaDataUri = createMetaDataURI(fileInfo.getURI());
		final DtObject fileMetadataDtoOld = getPersistenceManager().getBroker().get(dtoMetaDataUri);
		final Object fdtId = TwoTablesDbFileStorePlugin.<Object> getValue(fileMetadataDtoOld, DtoFields.FDT_ID);
		setValue(fileMetadataDto, DtoFields.FDT_ID, fdtId);
		setValue(fileDataDto, DtoFields.FDT_ID, fdtId);
		getPersistenceManager().getBroker().update(fileDataDto);
		getPersistenceManager().getBroker().update(fileMetadataDto);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final FileInfoURI fileInfoUri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		//-----
		final URI<DtObject> dtoMetaDataUri = createMetaDataURI(fileInfoUri);
		final DtObject fileMetadataDtoOld = getPersistenceManager().getBroker().get(dtoMetaDataUri);
		final Object fdtId = TwoTablesDbFileStorePlugin.<Object> getValue(fileMetadataDtoOld, DtoFields.FDT_ID);
		final URI<DtObject> dtoDataUri = createDataURI(fileInfoUri.getDefinition(), fdtId);

		getPersistenceManager().getBroker().delete(dtoDataUri);
		getPersistenceManager().getBroker().delete(dtoMetaDataUri);
	}

	/**
	 * Création d'une URI du DTO de métadonnées à partir de l'URI de FileInfo
	 *
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker les métadonnées.
	 */
	private static URI<DtObject> createMetaDataURI(final FileInfoURI uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		//-----
		final FileInfoDefinition fileInfoDefinition = uri.<FileInfoDefinition> getDefinition();
		final DtDefinition dtDefinition = getRootDtDefinition(fileInfoDefinition, 0);
		return new URI<>(dtDefinition, uri.getKey());
	}

	/**
	 * Création d'une URI de DTO des données à partir de l'URI de FileInfo et la clé de la ligne
	 *
	 * @param fileInfoDefinition Definition de FileInfo
	 * @param fdtId Identifiant de la ligne
	 * @return URI du DTO utilisé en BDD pour stocker les données.
	 */
	private static URI<DtObject> createDataURI(final FileInfoDefinition fileInfoDefinition, final Object fdtId) {
		final DtDefinition dtDefinition = getRootDtDefinition(fileInfoDefinition, 1);
		return new URI<>(dtDefinition, fdtId);
	}

	private static DtDefinition getRootDtDefinition(final FileInfoDefinition fileInfoDefinition, final int rootIndex) {
		Assertion.checkNotNull(fileInfoDefinition, "Definition du fichier doit être renseignée.");
		Assertion.checkNotNull(fileInfoDefinition.getRoot(), "Pour ce FileStore le root contient le nom des deux tables : FILE_METADATA;FILE_DATA");
		Assertion.checkArgument(fileInfoDefinition.getRoot().contains(";"), "Pour ce FileStore le root contient le nom des deux tables : FILE_METADATA;FILE_DATA");
		Assertion.checkArgument(fileInfoDefinition.getRoot().split(";").length == 2, "Pour ce FileStore le root contient le nom des deux tables : FILE_METADATA;FILE_DATA");
		//-----
		final String fileDataDefinitionRoot = fileInfoDefinition.getRoot().split(";")[rootIndex];
		// Pour ce fileStore, on utilise le root des fileDefinitions comme nom des tables de stockage.
		// Il doit exister des DtObjet associés, avec la structure attendue.
		return Home.getDefinitionSpace().resolve(fileDataDefinitionRoot, DtDefinition.class);
	}

	private static DtObject createMetadataDtObject(final FileInfo fileInfo) {
		final DtObject fileMetadataDto = DtObjectUtil.createDtObject(getRootDtDefinition(fileInfo.getDefinition(), 0));
		final VFile vFile = fileInfo.getVFile();
		setValue(fileMetadataDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileMetadataDto, DtoFields.MIME_TYPE, vFile.getMimeType());
		setValue(fileMetadataDto, DtoFields.LAST_MODIFIED, vFile.getLastModified());
		setValue(fileMetadataDto, DtoFields.LENGTH, vFile.getLength());
		return fileMetadataDto;
	}

	private static DtObject createDataDtObject(final FileInfo fileInfo) {
		final DtObject fileDataDto = DtObjectUtil.createDtObject(getRootDtDefinition(fileInfo.getDefinition(), 1));
		final VFile vFile = fileInfo.getVFile();
		setValue(fileDataDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileDataDto, DtoFields.FILE_DATA, new FileInfoDataStream(vFile));
		return fileDataDto;
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	private static Object getValue(final DtObject dto, final DtoFields field) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return dtField.getDataAccessor().getValue(dto);
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private static void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInfoDataStream implements DataStream {
		private final VFile vFile;

		FileInfoDataStream(final VFile vFile) {
			Assertion.checkNotNull(vFile);
			//-----
			this.vFile = vFile;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return vFile.createInputStream();
		}

		/** {@inheritDoc} */
		@Override
		public long getLength() {
			return vFile.getLength();
		}
	}

	private static final class DataStreamInputStreamBuilder implements InputStreamBuilder {
		private final DataStream dataStream;

		DataStreamInputStreamBuilder(final DataStream dataStream) {
			Assertion.checkNotNull(dataStream);
			//-----
			this.dataStream = dataStream;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return dataStream.createInputStream();
		}
	}

	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
	}
}
