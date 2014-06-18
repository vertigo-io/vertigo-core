package io.vertigo.dynamo.file.util;

import java.io.File;
import java.io.IOException;

/**
 * Fichier temporaire supprimé automatiquement après utilisation.
 * @author npiedeloup
 */
public final class TempFile extends File {

	private static final long serialVersionUID = 1947509935178818002L;

	/**
	 * Crée un fichier temporaire qui sera supprimé lorsqu'il ne sera plus référencé.
	 * @param prefix Prefix du nom de fichier
	 * @param suffix Suffix du nom de fichier
	 * @param directory Répertoire des fichiers temporaires (null = répertoire temporaire de l'OS)
	 * @throws IOException Exception IO
	 */
	public TempFile(final String prefix, final String suffix, final File directory) throws IOException {
		super(File.createTempFile(prefix, suffix, directory).getAbsolutePath());
		deleteOnExit();
	}

	/**
	 * Crée un fichier temporaire qui sera supprimé lorsqu'il ne sera plus référencé.
	 * @param prefix Prefix du nom de fichier
	 * @param suffix Suffix du nom de fichier
	 * @throws IOException Exception IO
	 */
	public TempFile(final String prefix, final String suffix) throws IOException {
		this(prefix, suffix, null);
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		if (exists() && !delete()) {
			deleteOnExit();
		}
		super.finalize();
	}
}
