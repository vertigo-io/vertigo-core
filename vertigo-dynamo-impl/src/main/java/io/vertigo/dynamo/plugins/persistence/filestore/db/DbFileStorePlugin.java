package io.vertigo.dynamo.plugins.persistence.filestore.db;

import io.vertigo.dynamo.domain.metamodel.DataStream;
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
import io.vertigo.dynamo.impl.persistence.FileStorePlugin;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;

/**
 * Permet de gérer les accès atomiques à n'importe quel type de stockage SQL/
 * non SQL pour les traitements de FileInfo.
 *
 * @author pchretien, npiedeloup
 */
public final class DbFileStorePlugin implements FileStorePlugin {
	private static final String STORE_READ_ONLY = "Le store est en readOnly";

	/**
	 * Liste des champs du Dto de stockage.
	 * Ces champs sont obligatoire sur les Dt associés aux fileInfoDefinitions
	 * @author npiedeloup
	 */
	private static enum DtoFields {
		FILE_NAME, MIME_TYPE, LAST_MODIFIED, LENGTH, FILE_DATA, FIL_ID
	}

	/**
	 * Le store est-il en mode readOnly ?
	 */
	private final boolean readOnly;
	private final FileManager fileManager;

	/**
	 * Constructeur.
	 * @param fileManager Manager de gestion des fichiers
	 */
	@Inject
	public DbFileStorePlugin(final FileManager fileManager) {
		super();
		Assertion.checkNotNull(fileManager);
		//---------------------------------------------------------------------
		readOnly = false;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	public FileInfo load(final URI<FileInfo> uri) {
		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		final DtObject fileInfoDto = getPersistenceManager().getBroker().get(dtoUri);
		final InputStreamBuilder inputStreamBuilder = new DataStreamInputStreamBuilder(this.<DataStream> getValue(fileInfoDto, DtoFields.FILE_DATA));
		final String fileName = this.<String> getValue(fileInfoDto, DtoFields.FILE_NAME);
		final String mimeType = this.<String> getValue(fileInfoDto, DtoFields.MIME_TYPE);
		final Date lastModified = this.<Date> getValue(fileInfoDto, DtoFields.LAST_MODIFIED);
		final Long length = this.<Long> getValue(fileInfoDto, DtoFields.LENGTH);
		final KFile kFile = fileManager.createFile(fileName, mimeType, lastModified, length, inputStreamBuilder);
		//TODO passer par une factory de FileInfo à partir de la FileInfoDefinition (comme DomainFactory)
		return new DatabaseFileInfo(uri.<FileInfoDefinition> getDefinition(), kFile);
	}

	/** {@inheritDoc} */
	public void put(final FileInfo fileInfo) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		//---------------------------------------------------------------------
		final DtObject fileInfoDto = createDtObject(fileInfo.getDefinition());
		//---------------------------------------------------------------------
		final KFile kFile = fileInfo.getKFile();
		setValue(fileInfoDto, DtoFields.FILE_NAME, kFile.getFileName());
		setValue(fileInfoDto, DtoFields.MIME_TYPE, kFile.getMimeType());
		setValue(fileInfoDto, DtoFields.LAST_MODIFIED, kFile.getLastModified());
		setValue(fileInfoDto, DtoFields.LENGTH, kFile.getLength());
		setValue(fileInfoDto, DtoFields.FILE_DATA, new FileInfoDataStream(kFile));

		if (fileInfo.getURI() != null) {
			setValue(fileInfoDto, DtoFields.FIL_ID, fileInfo.getURI().getKey());
		}
		//---------------------------------------------------------------------

		getPersistenceManager().getBroker().save(fileInfoDto);
		if (fileInfo.getURI() == null) {
			final Object fileInfoDtoId = DtObjectUtil.getId(fileInfoDto);
			Assertion.checkNotNull(fileInfoDtoId, "ID  du fichier doit être renseignée.");
			final URI<FileInfo> uri = createURI(fileInfo.getDefinition(), fileInfoDtoId);
			fileInfo.setURIStored(uri);
		}
	}

	private static URI<FileInfo> createURI(final FileInfoDefinition fileInfoDefinition, final Object key) {
		return new URI<>(fileInfoDefinition, key);
	}

	/** {@inheritDoc} */
	public void remove(final URI<FileInfo> uri) {
		Assertion.checkArgument(!readOnly, STORE_READ_ONLY);
		//---------------------------------------------------------------------
		final URI<DtObject> dtoUri = createDtObjectURI(uri);
		getPersistenceManager().getBroker().delete(dtoUri);
	}

	/**
	 * Création d'une URI de DTO à partir de l'URI de FileInfo
	 * @param uri URI de FileInfo
	 * @return URI du DTO utilisé en BDD pour stocker.
	 */
	private URI<DtObject> createDtObjectURI(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri, "uri du fichier doit être renseignée.");
		//---------------------------------------------------------------------
		final FileInfoDefinition fileInfoDefinition = uri.<FileInfoDefinition> getDefinition();
		final String fileDefinitionRoot = fileInfoDefinition.getRoot();
		//Pour ce fileStore, on utilise le root de la fileDefinition comme nom de la table de stockage.
		//Il doit exister un DtObjet associé, avec la structure attendue.
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve(fileDefinitionRoot, DtDefinition.class);
		return new URI<>(dtDefinition, uri.getKey());
	}

	/**
	 * Création d'un DTO à partir d'une definition de FileInfo
	 * @param fileInfoDefinition Definition de FileInfo
	 * @return DTO utilisé en BDD pour stocker.
	 */
	private DtObject createDtObject(final FileInfoDefinition fileInfoDefinition) {
		Assertion.checkNotNull(fileInfoDefinition, "fileInfoDefinition du fichier doit être renseignée.");
		//---------------------------------------------------------------------
		final String fileDefinitionRoot = fileInfoDefinition.getRoot();
		//Pour ce fileStore, on utilise le root de la fileDefinition comme nom de la table de stockage.
		//Il doit exister un DtObjet associé, avec la structure attendue.
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
	//non static pour le typage du generic O
	private <O> O getValue(final DtObject dto, final DtoFields field) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		return (O) dtField.getDataAccessor().getValue(dto);
	}

	/**
	 * Fixe une valeur d'un champ d'un DtObject.
	 * 
	 * @param dto DtObject
	 * @param field Nom du champs
	 * @param value Valeur
	 */
	private void setValue(final DtObject dto, final DtoFields field, final Object value) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(field.name());
		dtField.getDataAccessor().setValue(dto, value);
	}

	private static final class FileInfoDataStream implements DataStream {
		private final KFile kFile;

		FileInfoDataStream(final KFile kFile) {
			this.kFile = kFile;
		}

		public InputStream createInputStream() throws IOException {
			return kFile.createInputStream();
		}

		public long getLength() {
			return kFile.getLength();
		}
	}

	private static final class DataStreamInputStreamBuilder implements InputStreamBuilder {
		private final DataStream dataStream;

		DataStreamInputStreamBuilder(final DataStream dataStream) {
			this.dataStream = dataStream;
		}

		/** {@inheritDoc} */
		public InputStream createInputStream() throws IOException {
			return dataStream.createInputStream();
		}
	}

	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
	}
}
