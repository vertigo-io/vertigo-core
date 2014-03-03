package io.vertigo.dynamo.file.metamodel;

import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.metamodel.Prefix;
import io.vertigo.kernel.util.StringUtil;

/**
 * Définition d'un FileInfo.
 *
 * La définition n'est pas serializable.
 * Elle doit être invariante (non mutable) dans le temps.
 * Par défaut elle est chargée au (re)démarrage du serveur.
 *
 * @author  npiedeloup, pchretien
 * @version $Id: FileInfoDefinition.java,v 1.3 2013/10/22 12:33:54 pchretien Exp $
 */
@Prefix("FI")
public final class FileInfoDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;
	/**
	 * Racine des fichiers de ce type (utilisé par le store).
	 */
	private final String root;

	/**
	 * Nom du fileStorePlugin utilisé.
	 * On garde le nom et pas le plugin qui n'est porté que par le FileManager.
	 */
	private final String fileStoreName;

	/**
	 * Constructeur.
	 * @param root Racine des fichiers de ce type
	 * @param fileStoreName Nom du fileStorePlugin utilisé
	 */
	public FileInfoDefinition(final String name, final String root, final String fileStoreName) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(root);
		Assertion.checkNotNull(fileStoreName);
		//---------------------------------------------------------------------
		this.name = name;
		this.root = root;
		this.fileStoreName = fileStoreName;
	}

	/**
	 * @return Racine d'accès aux FI (utilisation depends du fileStorePlugin utilisé).
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @return Nom du fileStorePlugin utilisé pour cette definition.
	 */
	public String getFileStorePluginName() {
		return fileStoreName;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

	//=========================================================================
	//===========================STATIC========================================
	//=========================================================================
	public static FileInfoDefinition findFileInfoDefinition(final Class<? extends FileInfo> fileInfoClass) {
		Assertion.checkNotNull(fileInfoClass);
		//----------------------------------------------------------------------
		final String name = DefinitionUtil.getPrefix(FileInfoDefinition.class) + SEPARATOR + StringUtil.camelToConstCase(fileInfoClass.getSimpleName());
		return Home.getDefinitionSpace().resolve(name, FileInfoDefinition.class);
	}
}
