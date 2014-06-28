package io.vertigo.quarto.publisher.impl;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.PublisherManager;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Impl�mentation standard du manager des �ditions.
 *
 * @author pchretien, npiedeloup
 * @version $Id: PublisherManagerImpl.java,v 1.8 2014/01/28 18:53:45 pchretien Exp $
 */
public final class PublisherManagerImpl implements PublisherManager {
	@Inject
	private MergerPlugin mergerPlugin;
	private final FileManager fileManager;
	private final WorkManager workManager;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 * @param scriptManager Manager des scripts
	 * @param fileManager Manager des fichiers
	 */
	@Inject
	public PublisherManagerImpl(final WorkManager workManager, final ScriptManager scriptManager, final FileManager fileManager) {
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(scriptManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	public void publishASync(final String fileName, final URL modelFileURL, final PublisherData data, final WorkResultHandler<KFile> workResultHandler) {
		Assertion.checkNotNull(fileName);
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		workManager.async(new Callable<KFile>() {
			public KFile call() {
				return publish(fileName, modelFileURL, data);
			}
		}, workResultHandler);
	}

	/** {@inheritDoc} */
	public KFile publish(final String fileName, final URL modelFileURL, final PublisherData data) {
		Assertion.checkNotNull(fileName);
		Assertion.checkNotNull(modelFileURL);
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		try {
			return generateFile(fileName, modelFileURL, data);
		} catch (final IOException e) {
			final String msg = "La g�n�ration du fichier a �chou�.<!-- " + e.getMessage() + "--> pour le fichier " + fileName;
			throw new VRuntimeException(msg, e);
		}
	}

	private KFile generateFile(final String fileName, final URL modelFileURL, final PublisherData data) throws IOException {
		// attention : pour ce generateFile le File retourn� n'a pas le nom de fichier donn� dans
		// mergeParameter.getOuputFileName() car on utilise cette m�thode notamment dans send
		// ci-dessus pour plusieurs utilisateurs simultan�ment avec probablement le m�me
		// mergeParameter.getOuputFileName()
		//----------------------------------------------------------------------
		final File fileToExport = mergerPlugin.execute(modelFileURL, data);
		return fileManager.createFile(fileName, mergerPlugin.getPublisherFormat().getMimeType(), fileToExport);
	}
}
