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
package io.vertigo.dynamo.plugins.persistence.filestore.fs;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;
import io.vertigo.dynamo.impl.persistence.FileStorePlugin;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionResourceId;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 * 
 * @author pchretien, npiedeloup, skerdudou
 */
public final class FsFileStorePlugin implements FileStorePlugin {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Identifiant de ressource FileSystem par défaut.
	 */
	private static final KTransactionResourceId<FsTransactionResource> FS_RESOURCE_ID = new KTransactionResourceId<>(KTransactionResourceId.Priority.NORMAL, "FS");

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 * 
	 * @author npiedeloup
	 */
	private static enum DtoFields {
		/** Champ FIL_ID */
		FIL_ID,
		/** Champ FILE_NAME */
		FILE_NAME,
		/** Champ MIME_TYPE */
		MIME_TYPE,
		/** Champ LAST_MODIFIED */
		LAST_MODIFIED,
		/** Champ LENGTH */
		LENGTH,
		/** Champ FILE_PATH */
		FILE_PATH
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;
	private final String documentRoot;
	private final KTransactionManager transactionManager;

	/**
	 * Constructeur.
	 * 
	 * @param fileManager Manager de gestion des fichiers
	 * @param path le chemin jndi pour récupérer le paramètre path dans le context
	 */
	@Inject
	public FsFileStorePlugin(final KTransactionManager transactionManager, final FileManager fileManager, @Named("path") final String path) {
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(path);
		// ---------------------------------------------------------------------
		this.transactionManager = transactionManager;
		this.fileManager = fileManager;
		readOnly = false;
		documentRoot = path;
	}

	/** {@inheritDoc} */
	public FileInfo load(final URI<FileInfo> uri) {
		// récupération de l'objet en base
		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		final DtObject fileInfoDto = getPersistenceManager().getBroker().get(dtoUri);

		// récupération du fichier
		final String fileName = this.<String> getValue(fileInfoDto, DtoFields.FILE_NAME);
		final String mimeType = this.<String> getValue(fileInfoDto, DtoFields.MIME_TYPE);
		final Date lastModified = this.<Date> getValue(fileInfoDto, DtoFields.LAST_MODIFIED);
		final Long length = this.<Long> getValue(fileInfoDto, DtoFields.LENGTH);
		final String filePath = this.<String> getValue(fileInfoDto, DtoFields.FILE_PATH);

		final InputStreamBuilder inputStreamBuilder = new FileInputStreamBuilder(new File(documentRoot + filePath));
		final KFile kFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

		// retourne le fileinfo avec le fichier et son URI
		final FsFileInfo fsFileInfo = new FsFileInfo(uri.<FileInfoDefinition> getDefinition(), kFile);
		fsFileInfo.setURIStored(uri);
		return fsFileInfo;
	}

	private static class FsFileInfo extends AbstractFileInfo {
		private static final long serialVersionUID = -1610176974946554828L;

		protected FsFileInfo(final FileInfoDefinition fileInfoDefinition, final KFile kFile) {
			super(fileInfoDefinition, kFile);
		}
	}

	/** {@inheritDoc} */
	public void put(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		String pathToSave = null;
		final KFile kFile = fileInfo.getKFile();

		// ---------------------------------------------------------------------
		final DtObject fileInfoDto = createDtObject(fileInfo.getDefinition());
		// ---------------------------------------------------------------------
		setValue(fileInfoDto, DtoFields.FILE_NAME, kFile.getFileName());
		setValue(fileInfoDto, DtoFields.MIME_TYPE, kFile.getMimeType());
		setValue(fileInfoDto, DtoFields.LAST_MODIFIED, kFile.getLastModified());
		setValue(fileInfoDto, DtoFields.LENGTH, kFile.getLength());

		if (fileInfo.getURI() == null) {
			// cas de la création, on ajoute en base un chemin fictif (colonne not null)
			setValue(fileInfoDto, DtoFields.FILE_PATH, "/dev/null");
		} else {
			// cas de l'update
			setValue(fileInfoDto, DtoFields.FIL_ID, fileInfo.getURI().getKey());

			// récupération de l'objet en base pour récupérer le path du fichier et ne pas modifier la base
			final URI<DtObject> dtoUri = createDtObjectURI(fileInfo.getURI());
			final DtObject fileInfoDtoBase = getPersistenceManager().getBroker().get(dtoUri);
			pathToSave = this.<String> getValue(fileInfoDtoBase, DtoFields.FILE_PATH);
			setValue(fileInfoDto, DtoFields.FILE_PATH, pathToSave);
		}

		// ---------------------------------------------------------------------
		getPersistenceManager().getBroker().save(fileInfoDto);

		if (fileInfo.getURI() == null) {
			// cas de la création
			final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
			Assertion.checkNotNull(fileInfoDtoId, "ID  du fichier doit être renseignée.");
			final URI<FileInfo> uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
			fileInfo.setURIStored(uri);

			// on met a jour la base
			final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/", Locale.FRANCE);
			pathToSave = format.format(new Date()) + fileInfoDtoId;
			setValue(fileInfoDto, DtoFields.FILE_PATH, pathToSave);

			getPersistenceManager().getBroker().save(fileInfoDto);
		}

		// sauvegarde du fichier
		try (InputStream inputStream = kFile.createInputStream()) {
			obtainFsTransactionRessource().saveFile(inputStream, documentRoot + pathToSave);
		} catch (final IOException e) {
			throw new RuntimeException("Impossible de lire le fichier uploadé.", e);
		}

	}

	private static URI<FileInfo> createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new URI<>(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	public void remove(final URI<FileInfo> uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);

		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		// ----------------- suppression du fichier ----------------------------
		final DtObject fileInfoDto = getPersistenceManager().getBroker().get(dtoUri);
		final String path = this.<String> getValue(fileInfoDto, DtoFields.FILE_PATH);
		obtainFsTransactionRessource().deleteFile(documentRoot + path);
		// ------------------ suppression en base ------------------------------
		getPersistenceManager().getBroker().delete(dtoUri);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 * 
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private static URI<DtObject> createDtObjectURI(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		// ---------------------------------------------------------------------
		final FileInfoDefinition fileInfoDefinition = uri.<FileInfoDefinition> getDefinition();
		final String fileDefinitionRoot = fileInfoDefinition.getRoot();
		// Pour ce fileStore, on utilise le root de la fileDefinition comme nom de la table de stockage.
		// Il doit exister un DtObjet associé, avec la structure attendue.
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve(fileDefinitionRoot, DtDefinition.class);
		return new URI<>(dtDefinition, uri.getKey());
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 * 
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private static DtObject createDtObject(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition, "fileInfoDefinition du fichier doit être renseignée.");
		// ---------------------------------------------------------------------
		final String fileDefinitionRoot = fileInfoDefinition.getRoot();
		// Pour ce fileStore, on utilise le root de la fileDefinition comme nom de la table de stockage.
		// Il doit exister un DtObjet associé, avec la structure attendue.
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve(fileDefinitionRoot, DtDefinition.class);
		return DtObjectUtil.createDtObject(dtDefinition);
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 * 
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	// non static pour le typage du generic O
	@SuppressWarnings("unchecked")
	private <O> O getValue(final DtObject dto, final DtoFields field) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(field.name());
		return (O) dtField.getDataAccessor().getValue(dto);
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 * 
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private static void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInputStreamBuilder implements InputStreamBuilder {

		private final File file;

		FileInputStreamBuilder(final File file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		public InputStream createInputStream() throws IOException {
			return new FileInputStream(file);
		}
	}

	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
	}

	/** récupère la transaction courante. */
	private KTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	/**
	 * Retourne l'id de la ressource FileSystem.
	 * 
	 * @return Id de la Ressource Connexion FileSystem dans la transaction
	 */
	private static KTransactionResourceId<FsTransactionResource> getKTransactionResourceId() {
		return FS_RESOURCE_ID;
	}

	/** récupère la ressource FS de la transaction et la créé si nécessaire. */
	private FsTransactionResource obtainFsTransactionRessource() {
		FsTransactionResource resource = getCurrentTransaction().getResource(getKTransactionResourceId());

		if (resource == null) {
			// Si aucune ressource de type FS existe sur la transaction, on la créé
			resource = new FsTransactionResource();
			getCurrentTransaction().addResource(getKTransactionResourceId(), resource);
		}
		return resource;
	}

	/**
	 * récupère la valeur de documentRoot.
	 * 
	 * @return valeur de documentRoot
	 */
	public String getDocumentRoot() {
		return documentRoot;
	}
}
