package io.vertigo.dynamo.impl.file.model;

import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

/**
 * Class générique de définition d'un fichier.
 * @author npiedeloup
 * @version $Id: AbstractFileInfo.java,v 1.7 2014/01/20 17:46:41 pchretien Exp $
 */
public abstract class AbstractFileInfo implements FileInfo {
	private static final long serialVersionUID = 1L;
	private final KFile kFile;
	private final DefinitionReference<FileInfoDefinition> fileInfoDefinition;
	private URI<FileInfo> uri;

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileInfoDefinition Definition du FileInfo
	 * @param kFile Données du fichier
	*/
	protected AbstractFileInfo(final FileInfoDefinition fileInfoDefinition, final KFile kFile) {
		Assertion.checkNotNull(fileInfoDefinition);
		Assertion.checkNotNull(kFile);
		//---------------------------------------------------------------------
		this.fileInfoDefinition = new DefinitionReference<>(fileInfoDefinition);
		this.kFile = kFile;
	}

	/** {@inheritDoc} */
	public final URI<FileInfo> getURI() {
		return uri;
	}

	/** {@inheritDoc} */
	public final void setURIStored(final URI<FileInfo> storedUri) {
		Assertion.checkNotNull(storedUri);
		Assertion.checkState(uri == null, "Impossible de setter deux fois l'uri de stockage");
		Assertion.checkArgument(getDefinition().getName().equals(storedUri.<FileInfoDefinition> getDefinition().getName()), "L''URI ({0}) n''est pas compatible avec ce FileInfo ({1})", storedUri, fileInfoDefinition);
		//----------------------------------------------------------------------
		uri = storedUri;
	}

	/** {@inheritDoc} */
	public final FileInfoDefinition getDefinition() {
		return fileInfoDefinition.get();
	}

	/** {@inheritDoc} */
	public final KFile getKFile() {
		return kFile;
	}
}
