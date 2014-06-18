package io.vertigo.dynamo;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;

/**
 * Utilitaire pour construire des cas de tests.
 *
 * @author npiedeloup
 */
public final class TestUtil {
	/**
	 * Constructeur privé pour class utilitaire
	 *
	 */
	private TestUtil() {
		super();
	}

	/**
	 * Crée un KFile relativement d'un class de base.
	 * @param fileName Nom/path du fichier
	 * @param baseClass Class de base pour le chemin relatif 
	 * @return KFile
	 */
	public static KFile createKFile(final FileManager fileManager, final String fileName, final Class<?> baseClass) {
		try {
			try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
				Assertion.checkNotNull(in, "fichier non trouvé : {0}", fileName);
				final File file = new TempFile("tmp", '.' + FileUtil.getFileExtension(fileName));
				FileUtil.copy(in, file);
				return fileManager.createFile(file);
			}
		} catch (final IOException e) {
			throw new VRuntimeException(e);
		}
	}

	/**
	 * Récupère un File (pointeur de fichier) vers un fichier relativement à une class.
	 * @param fileName Nom/path du fichier
	 * @param baseClass Class de base pour le chemin relatif  
	 * @return File
	 */
	public static File getFile(final String fileName, final Class<?> baseClass) {
		final URL fileURL = baseClass.getResource(fileName);
		try {
			return new File(fileURL.toURI());
		} catch (final URISyntaxException e) {
			throw new VRuntimeException(e);
		}
	}

	/**
	 * Utilitaire.
	 * @param manager managerDescription
	 */
	public static final void testDescription(final Manager manager) {
		if (manager instanceof Describable) {
			final List<ComponentInfo> componentInfos = Describable.class.cast(manager).getInfos();
			for (final ComponentInfo componentInfo : componentInfos) {
				Assert.assertNotNull(componentInfo);
			}
		}
	}
}
