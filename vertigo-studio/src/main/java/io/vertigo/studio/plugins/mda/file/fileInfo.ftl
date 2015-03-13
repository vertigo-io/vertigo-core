package ${packageName};

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.file.model.AbstractFileInfo;

/**
 * Attention cette classe est générée automatiquement !
 * Objet représentant un fichier persistant ${fiDefinition.classSimpleName}
 */
public final class ${fiDefinition.classSimpleName} extends AbstractFileInfo {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur par défaut.
	 * @param vFile Données du fichier
	 */
	public ${fiDefinition.classSimpleName}(final VFile vFile) {
		super(FileInfoDefinition.findFileInfoDefinition(${fiDefinition.classSimpleName}.class), vFile);
	}
}

