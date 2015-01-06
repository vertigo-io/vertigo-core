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
package io.vertigo.quarto.plugins.converter.openoffice;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.quarto.impl.converter.ConverterPlugin;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

/**
 * Conversion des fichiers à partir de OpenOffice.
 * @author npiedeloup
 */
abstract class AbstractOpenOfficeConverterPlugin implements ConverterPlugin, Activeable {
	/** Le port par défaut pour accéder à OpenOffice est 8100. */
	public static final int DEFAULT_UNO_PORT = 8100;

	private static final Logger LOGGER = Logger.getLogger(AbstractOpenOfficeConverterPlugin.class);

	//private final Timer checkTimeoutTimer = new Timer("OpenOfficeConverterTimeoutCheck", true);
	private final ExecutorService executors = Executors.newFixedThreadPool(1);

	private final FileManager fileManager;
	private final String unoHost;
	private final int unoPort;
	private final int convertTimeoutSeconds;

	/**
	 * Constructeur.
	 * @param fileManager Manager de gestion des fichiers
	 * @param unoHost Hote du serveur OpenOffice
	 * @param unoPort Port de connexion au serveur OpenOffice
	 */
	protected AbstractOpenOfficeConverterPlugin(final FileManager fileManager, final String unoHost, final String unoPort, final int convertTimeoutSeconds) {
		super();
		Assertion.checkNotNull(fileManager);
		Assertion.checkArgNotEmpty(unoHost);
		Assertion.checkArgument(convertTimeoutSeconds >= 1 && convertTimeoutSeconds <= 900, "Le timeout de conversion est exprimé en seconde et doit-être compris entre 1s et 15min (900s)");
		//-----
		this.fileManager = fileManager;
		this.unoHost = unoHost;
		this.unoPort = Integer.valueOf(unoPort);
		this.convertTimeoutSeconds = convertTimeoutSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//nothing
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		executors.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	public final KFile convertToFormat(final KFile file, final String targetFormat) {
		Assertion.checkArgNotEmpty(targetFormat);
		//-----
		return convertToFormat(file, ConverterFormat.find(targetFormat));
	}

	private KFile convertToFormat(final KFile file, final ConverterFormat targetFormat) {
		Assertion.checkNotNull(file);
		Assertion.checkNotNull(targetFormat);
		// si le format de sortie est celui d'entrée la convertion est inutile
		Assertion.checkArgument(!targetFormat.getTypeMime().equals(file.getMimeType()), "Le format de sortie est identique à celui d'entrée ; la conversion est inutile");
		//-----
		final File inputFile = fileManager.obtainReadOnlyFile(file);
		final Callable<File> convertTask = new Callable<File>() {
			@Override
			public File call() throws Exception {
				return doConvertToFormat(inputFile, targetFormat);
			}
		};
		final File targetFile;
		try {
			final Future<File> targetFileFuture = executors.submit(convertTask);
			targetFile = targetFileFuture.get(convertTimeoutSeconds, TimeUnit.SECONDS);
		} catch (final Exception e) {
			throw new RuntimeException("Erreur de conversion du document au format " + targetFormat.name(), e);
		}
		return fileManager.createFile(targetFile);
	}

	// On synchronize sur le plugin car OpenOffice supporte mal les accès concurrents.
	/**
	 * @param inputFile Fichier source
	 * @param targetFormat Format de destination
	 * @return Fichier resultat
	 * @throws Exception Exception
	 */
	synchronized File doConvertToFormat(final File inputFile, final ConverterFormat targetFormat) throws Exception {
		try (final OpenOfficeConnection openOfficeConnection = connectOpenOffice()) {
			Assertion.checkArgument(inputFile.exists(), "Le document à convertir n''existe pas : {0}", inputFile.getAbsolutePath());
			final XComponent xDoc = loadDocument(inputFile, openOfficeConnection);
			try {
				refreshDocument(xDoc);
				LOGGER.debug("Document source chargé");

				final File targetFile = new TempFile("edition", '.' + targetFormat.name());
				storeDocument(targetFile, xDoc, targetFormat, openOfficeConnection);
				LOGGER.debug("Conversion réussie");
				return targetFile;
			} finally {
				xDoc.dispose();
			}
		}
	}

	/**
	 * Ecriture du document.
	 * @param outputFile Fichier de sortie
	 * @param xDoc Document OpenOffice source
	 * @param targetFormat Format de sortie
	 * @param openOfficeConnection Connection à OpenOffice
	 * @throws Exception Erreur de traitement
	 */
	protected abstract void storeDocument(final File outputFile, final XComponent xDoc, final ConverterFormat targetFormat, final OpenOfficeConnection openOfficeConnection) throws Exception;

	/**
	 * Lecture d'un docuement.
	 * @param inputFile Fichier source
	 * @param openOfficeConnection  Connection à OpenOffice
	 * @return Document OpenOffice
	 * @throws Exception Erreur de traitement
	 */
	protected abstract XComponent loadDocument(final File inputFile, final OpenOfficeConnection openOfficeConnection) throws Exception;

	private OpenOfficeConnection connectOpenOffice() throws IOException {
		final OpenOfficeConnection openOfficeConnection = new SocketOpenOfficeConnection(unoHost, unoPort);
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("connecting to OpenOffice.org on  " + unoHost + ":" + unoPort);
			}
			openOfficeConnection.connect(); //Attention déjà observé : connection ne s'établissant pas et pas de timeout
		} catch (final ConnectException connectException) {
			//On précise les causes possibles de l'erreur.
			final String msg = new StringBuilder()
					.append("Dans le fichier OOoBasePath\\Basis\\share\\registry\\data\\org\\openoffice\\Setup.xcu.\n")
					.append("Juste après cette ligne-ci : <node oor:name=\"Office\">\n")
					.append("Il faut ajouter les lignes suivantes :\n")
					.append("<prop oor:name=\"ooSetupConnectionURL\" oor:type=\"xs:string\">\n")
					.append("<value>socket,host=localhost,port=").append(unoPort).append(";urp;</value>\n")
					.append("</prop>\n")
					.append("Ensuite, il faut lancer OpenOffice... et l'agent OpenOffice si il tourne.")
					.toString();

			throw new IOException("Impossible de se connecter à OpenOffice, vérifier qu'il est bien en écoute sur " + unoHost + ":" + unoPort + ".\n\n" + msg, connectException);
		}
		return openOfficeConnection;
	}

	private static void refreshDocument(final XComponent document) {
		final XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
		if (refreshable != null) {
			refreshable.refresh();
		}
	}

	private static PropertyValue[] getFileProperties(final ConverterFormat docType, final XOutputStream outputStream, final XInputStream inputStream) {
		Assertion.checkNotNull(docType, "Le type du format de sortie est obligatoire");
		Assertion.checkArgument(outputStream == null || inputStream == null, "Les properties pointent soit un fichier local, soit un flux d'entrée, soit un flux de sortie");
		final List<PropertyValue> fileProps = new ArrayList<>(3);

		PropertyValue fileProp = new PropertyValue();
		fileProp.Name = "Hidden";
		fileProp.Value = Boolean.TRUE;
		fileProps.add(fileProp);

		if (outputStream != null) {
			fileProp = new PropertyValue();
			fileProp.Name = "OutputStream";
			fileProp.Value = outputStream;
			fileProps.add(fileProp);
		} else if (inputStream != null) {
			fileProp = new PropertyValue();
			fileProp.Name = "InputStream";
			fileProp.Value = inputStream;
			fileProps.add(fileProp);
		}
		if (docType != ConverterFormat.ODT) {
			fileProp = new PropertyValue();
			fileProp.Name = "FilterName";
			fileProp.Value = getFilterNameFromExtension(docType);
			fileProps.add(fileProp);
		}
		return fileProps.toArray(new PropertyValue[fileProps.size()]);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion.
	 * @param docType format
	 * @return Proterties
	 */
	protected static final PropertyValue[] getFileProperties(final ConverterFormat docType) {
		return getFileProperties(docType, null, null);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion et un flux d'ecriture.
	 * @param docType docType format
	 * @param outputStream Flux d'ecriture
	 * @return Proterties
	 */
	protected static final PropertyValue[] getFileProperties(final ConverterFormat docType, final XOutputStream outputStream) {
		return getFileProperties(docType, outputStream, null);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion et un flux de lecture.
	 * @param docType docType format
	 * @param inputStream Flux de lecture
	 * @return Proterties
	 */
	protected static final PropertyValue[] getFileProperties(final ConverterFormat docType, final XInputStream inputStream) {
		return getFileProperties(docType, null, inputStream);
	}

	/**
	 * @param docType Format de conversion
	 * @return filterName géré par OpenOffice pour lui préciser le format de conversion
	 */
	protected static final String getFilterNameFromExtension(final ConverterFormat docType) {
		//Liste des filterName géré par OpenOffice.
		//la liste est dans :
		//OO 3.3 "OpenOffice.org 3\Basis\share\registry\modules\org\openoffice\TypeDetection\Filter\fcfg_writer_filters.xcu"
		//OO 3.4 "OpenOffice.org 3\Basis\share\registry\writer.xcd"
		switch (docType) {
			case PDF:
				return "writer_pdf_Export";
			case RTF:
				return "Rich Text Format";
			case DOC:
				return "MS Word 97";
			case ODT:
				return "Open Document Format";
			case TXT:
				return "Text";
				//			case DOCX:
				//				return "MS Word 2007 XML";
				//			case CSV:
				//				return "Text - txt - csv (StarCalc)";
			default:
				throw new InvalidParameterException("Type de document non géré : " + docType);
		}
	}
}
