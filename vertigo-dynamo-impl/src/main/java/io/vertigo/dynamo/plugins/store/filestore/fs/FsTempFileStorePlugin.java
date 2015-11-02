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
package io.vertigo.dynamo.plugins.store.filestore.fs;

import io.vertigo.core.Home;
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
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;
import io.vertigo.dynamo.impl.store.filestore.FileStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.transaction.VTransaction;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.DateUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup, skerdudou
 */
public final class FsTempFileStorePlugin implements FileStorePlugin {
	private static final String DEFAULT_STORE_NAME = "temp";
	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String INFOS_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 *
	 * @author npiedeloup
	 */
	private enum DtoFields {
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
	private final String name;
	private final String documentRoot;

	/**
	 * Constructeur.
	 * @param name Store name
	 * @param fileManager Manager de gestion des fichiers
	 * @param path Root directory
	 */
	@Inject
	public FsTempFileStorePlugin(@Named("name") final Option<String> name, @Named("path") final String path, final FileManager fileManager) {
		Assertion.checkNotNull(name);
		Assertion.checkArgNotEmpty(path);
		Assertion.checkNotNull(fileManager);
		//-----
		this.name = name.getOrElse(DEFAULT_STORE_NAME);
		readOnly = false;
		this.fileManager = fileManager;
		documentRoot = translatePath(path);
	}

	private static String translatePath(final String path) {
		return path
				.replaceAll(USER_HOME, System.getProperty(USER_HOME).replace('\\', '/'))
				.replaceAll(USER_DIR, System.getProperty(USER_DIR).replace('\\', '/'))
				.replaceAll(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/'));
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public FileInfo load(final FileInfoURI uri) {
		// récupération des metadata.
		try {
			final String metadataUri = String.class.cast(uri.getKey()) + ".info";
			final Path metadataPath = Paths.get(documentRoot, metadataUri);
			final List<String> infos = Files.readAllLines(metadataPath, Charset.forName("utf8"));
			// récupération des infos
			final String fileName = infos.get(0);
			final String mimeType = infos.get(1);
			final Date lastModified = DateUtil.parse(infos.get(2), INFOS_DATE_PATTERN);
			final Long length = Long.valueOf(infos.get(3));

			final InputStreamBuilder inputStreamBuilder = new FileInputStreamBuilder(new File(documentRoot + String.class.cast(uri.getKey())));
			final VFile vFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);

			// retourne le fileinfo avec le fichier et son URI
			final FsFileInfo fsFileInfo = new FsFileInfo(uri.getDefinition(), vFile);
			fsFileInfo.setURIStored(uri);
			return fsFileInfo;
		} catch (final IOException e) {
			throw new RuntimeException("Can't read fileInfo " + uri.toURN(), e);
		}
	}

	private static class FsFileInfo extends AbstractFileInfo {
		private static final long serialVersionUID = -1610176974946554828L;

		protected FsFileInfo(final FileInfoDefinition fileInfoDefinition, final VFile vFile) {
			super(fileInfoDefinition, vFile);
		}
	}

	private DtObject createFileInfoDto(final FileInfo fileInfo) {
		final DtObject fileInfoDto = createDtObject(fileInfo.getDefinition());
		//-----
		final VFile vFile = fileInfo.getVFile();
		setValue(fileInfoDto, DtoFields.FILE_NAME, vFile.getFileName());
		setValue(fileInfoDto, DtoFields.MIME_TYPE, vFile.getMimeType());
		setValue(fileInfoDto, DtoFields.LAST_MODIFIED, vFile.getLastModified());
		setValue(fileInfoDto, DtoFields.LENGTH, vFile.getLength());
		if (fileInfo.getURI() == null) {
			// cas de la création, on ajoute en base un chemin fictif (colonne not null)
			setValue(fileInfoDto, DtoFields.FILE_PATH, "/dev/null");
		} else {
			// cas de l'update
			setPkValue(fileInfoDto, fileInfo.getURI().getKey());

			// récupération de l'objet en base pour récupérer le path du fichier et ne pas modifier la base
			final URI<DtObject> dtoUri = createDtObjectURI(fileInfo.getURI());
			final DtObject fileInfoDtoBase = getStoreManager().getDataStore().get(dtoUri);
			final String pathToSave = getValue(fileInfoDtoBase, DtoFields.FILE_PATH, String.class);
			setValue(fileInfoDto, DtoFields.FILE_PATH, pathToSave);
		}
		return fileInfoDto;
	}

	private void saveFile(final FileInfo fileInfo, final String pathToSave) {
		try (InputStream inputStream = fileInfo.getVFile().createInputStream()) {
			obtainFsTransactionRessource().saveFile(inputStream, documentRoot + pathToSave);
		} catch (final IOException e) {
			throw new RuntimeException("Impossible de lire le fichier uploadé.", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void create(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkNotNull(fileInfo.getURI() == null, "Only file without any id can be created.");
		//-----
		final DtObject fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getStoreManager().getDataStore().create(fileInfoDto);

		// cas de la création
		final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
		Assertion.checkNotNull(fileInfoDtoId, "ID  du fichier doit être renseignée.");
		final FileInfoURI uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
		fileInfo.setURIStored(uri);

		// on met a jour la base
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/", Locale.FRANCE);
		final String pathToSave = format.format(new Date()) + fileInfoDtoId;
		setValue(fileInfoDto, DtoFields.FILE_PATH, pathToSave);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);
		//-----
		saveFile(fileInfo, pathToSave);
	}

	/** {@inheritDoc} */
	@Override
	public void update(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		Assertion.checkNotNull(fileInfo.getURI() != null, "Only file with an id can be updated.");
		//-----
		final DtObject fileInfoDto = createFileInfoDto(fileInfo);
		//-----
		getStoreManager().getDataStore().update(fileInfoDto);

		final String pathToSave = getValue(fileInfoDto, DtoFields.FILE_PATH, String.class);
		//-----
		saveFile(fileInfo, pathToSave);
	}

	private static FileInfoURI createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new FileInfoURI(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final FileInfoURI uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);

		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		//-----suppression du fichier
		final DtObject fileInfoDto = getStoreManager().getDataStore().get(dtoUri);
		final String path = getValue(fileInfoDto, DtoFields.FILE_PATH, String.class);
		obtainFsTransactionRessource().deleteFile(documentRoot + path);
		//-----suppression en base
		getStoreManager().getDataStore().delete(dtoUri);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 *
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private URI<DtObject> createDtObjectURI(final FileInfoURI uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return new URI<>(storeDtDefinition, uri.getKey());
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 *
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private DtObject createDtObject(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition, "fileInfoDefinition du fichier doit être renseignée.");
		//-----
		// Il doit exister un DtObjet associé, avec la structure attendue.
		return DtObjectUtil.createDtObject(storeDtDefinition);
	}

	/**
	 * Retourne une valeur d'un champ à partir du DtObject.
	 *
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @return Valeur typé du champ
	 */
	private static <V> V getValue(final DtObject dto, final DtoFields field, final Class<V> valueClass) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return valueClass.cast(dtField.getDataAccessor().getValue(dto));
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

	private static void setPkValue(final DtObject dto, final Object value) {
		final DtField dtField = DtObjectUtil.findDtDefinition(dto).getIdField().get();
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInputStreamBuilder implements InputStreamBuilder {

		private final File file;

		FileInputStreamBuilder(final File file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return new FileInputStream(file);
		}
	}

	private static StoreManager getStoreManager() {
		return Home.getComponentSpace().resolve(StoreManager.class);
	}

	/** récupère la transaction courante. */
	private VTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	/**
	 * Retourne l'id de la ressource FileSystem.
	 *
	 * @return Id de la Ressource Connexion FileSystem dans la transaction
	 */
	private static VTransactionResourceId<FsTransactionResource> getVTransactionResourceId() {
		return FS_RESOURCE_ID;
	}

	/** récupère la ressource FS de la transaction et la créé si nécessaire. */
	private FsTransactionResource obtainFsTransactionRessource() {
		FsTransactionResource resource = getCurrentTransaction().getResource(getVTransactionResourceId());

		if (resource == null) {
			// Si aucune ressource de type FS existe sur la transaction, on la créé
			resource = new FsTransactionResource();
			getCurrentTransaction().addResource(getVTransactionResourceId(), resource);
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
