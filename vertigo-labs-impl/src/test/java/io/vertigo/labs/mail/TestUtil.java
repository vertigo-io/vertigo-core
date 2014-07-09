package io.vertigo.labs.mail;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilitaire pour construire des cas de tests.
 *
 * @author npiedeloup
 * @version $Id: TestUtil.java,v 1.8 2014/02/27 10:26:39 pchretien Exp $
 */
public final class TestUtil {
	/**
	 * Constructeur priv� pour class utilitaire
	 *
	 */
	private TestUtil() {
		super();
	}

	/**
	 * Cr�e un KFile relativement d'un class de base.
	 * @param fileName Nom/path du fichier
	 * @param baseClass Class de base pour le chemin relatif 
	 * @return KFile
	 */
	public static KFile createKFile(final FileManager fileManager, final String fileName, final Class<?> baseClass) {
		try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
			Assertion.checkNotNull(in, "fichier non trouv� : {0}", fileName);
			final File file = new TempFile("tmp", '.' + FileUtil.getFileExtension(fileName));
			FileUtil.copy(in, file);
			return fileManager.createFile(file);
		} catch (final IOException e) {
			throw new VRuntimeException(e);
		}
	}

}
