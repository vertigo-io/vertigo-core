package io.vertigo.dynamo.impl.persistence.logical;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.impl.persistence.FileStore;
import io.vertigo.kernel.lang.Assertion;

/**
 * Permet de gérer le stockage des documents.
 * Transpose en store physique les appels logiques.
 *
 * @author npiedeloup
 * @version $Id: LogicalFileStore.java,v 1.5 2014/01/20 17:49:32 pchretien Exp $
 */
public final class LogicalFileStore implements FileStore {
	private final LogicalFileStoreConfiguration logicalFileStoreConfiguration;

	/**
	 * Constructeur.
	 * @param logicalFileStoreConfiguration Configuration logique des stores physiques.
	 */
	public LogicalFileStore(final LogicalFileStoreConfiguration logicalFileStoreConfiguration) {
		Assertion.checkNotNull(logicalFileStoreConfiguration);
		//---------------------------------------------------------------------
		this.logicalFileStoreConfiguration = logicalFileStoreConfiguration;
	}

	private static FileInfoDefinition getFileInfoDefinition(final URI<FileInfo> uri) {
		return uri.getDefinition();
	}

	private FileStore getPhysicalStore(final FileInfoDefinition fileInfoDefinition) {
		return logicalFileStoreConfiguration.getPhysicalStore(fileInfoDefinition);
	}

	/** {@inheritDoc} */
	public FileInfo load(final URI<FileInfo> uri) {
		final FileInfoDefinition fileInfoDefinition = getFileInfoDefinition(uri);
		return getPhysicalStore(fileInfoDefinition).load(uri);
	}

	/** {@inheritDoc} */
	public void put(final FileInfo fileInfo) {
		getPhysicalStore(fileInfo.getDefinition()).put(fileInfo);
	}

	/** {@inheritDoc} */
	public void remove(final URI<FileInfo> uri) {
		final FileInfoDefinition fileInfoDefinition = getFileInfoDefinition(uri);
		getPhysicalStore(fileInfoDefinition).remove(uri);
	}
}
