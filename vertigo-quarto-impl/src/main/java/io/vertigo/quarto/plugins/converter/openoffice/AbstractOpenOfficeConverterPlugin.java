package io.vertigo.quarto.plugins.converter.openoffice;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.impl.converter.ConverterPlugin;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

/**
 * Conversion des fichiers � partir de OpenOffice.
 * @author npiedeloup
 * @version $Id: AbstractOpenOfficeConverterPlugin.java,v 1.8 2014/02/27 10:24:53 pchretien Exp $
 */
abstract class AbstractOpenOfficeConverterPlugin implements ConverterPlugin {
	/** Le port par d�faut pour acc�der � OpenOffice est 8100. */
	public static final int DEFAULT_UNO_PORT = 8100;

	private static final Logger LOGGER = Logger.getLogger(AbstractOpenOfficeConverterPlugin.class);

	private final FileManager fileManager;
	private final String unoHost;
	private final int unoPort;

	/**
	 * Constructeur.
	 * @param fileManager Manager de gestion des fichiers
	 * @param unoHost Hote du serveur OpenOffice
	 * @param unoPort Port de connexion au serveur OpenOffice
	 */
	protected AbstractOpenOfficeConverterPlugin(final FileManager fileManager, final String unoHost, final String unoPort) {
		super();
		Assertion.checkNotNull(fileManager);
		Assertion.checkArgNotEmpty(unoHost);
		//---------------------------------------------------------------------
		this.fileManager = fileManager;
		this.unoHost = unoHost;
		this.unoPort = Integer.valueOf(unoPort);
	}

	/** {@inheritDoc} */
	public final KFile convertToFormat(final KFile file, final String targetFormat) {
		Assertion.checkArgNotEmpty(targetFormat);
		// ---------------------------------------------------------------------
		return convertToFormat(file, ConverterFormat.find(targetFormat));
	}

	private KFile convertToFormat(final KFile file, final ConverterFormat targetFormat) {
		Assertion.checkNotNull(file);
		Assertion.checkNotNull(targetFormat);
		// si le format de sortie est celui d'entr�e la convertion est inutile
		Assertion.checkArgument(!targetFormat.getTypeMime().equals(file.getMimeType()), "Le format de sortie est identique � celui d'entr�e ; la conversion est inutile");
		// ---------------------------------------------------------------------
		final File inputFile = fileManager.obtainReadOnlyFile(file);
		final File targetFile;
		try {
			targetFile = doConvertToFormat(inputFile, targetFormat);
		} catch (final Exception e) {
			throw new VRuntimeException("Erreur de conversion du document au format " + targetFormat.name(), e);
		}
		return fileManager.createFile(targetFile);
	}

	// On synchronize sur le plugin car OpenOffice supporte mal les acc�s concurrents.
	private synchronized File doConvertToFormat(final File inputFile, final ConverterFormat targetFormat) throws Exception {
		final OpenOfficeConnection openOfficeConnection = connectOpenOffice();
		try {
			Assertion.checkArgument(inputFile.exists(), "Le document � convertir n''existe pas : {0}", inputFile.getAbsolutePath());
			final XComponent xDoc = loadDocument(inputFile, openOfficeConnection);
			try {
				refreshDocument(xDoc);
				LOGGER.debug("Document source charg�");

				final File targetFile = new TempFile("edition", '.' + targetFormat.name());
				storeDocument(targetFile, xDoc, targetFormat, openOfficeConnection);
				LOGGER.debug("Conversion r�ussie");
				return targetFile;
			} finally {
				xDoc.dispose();
			}
		} finally {
			openOfficeConnection.disconnect();
		}
	}

	/**
	 * Ecriture du document.
	 * @param outputFile Fichier de sortie
	 * @param xDoc Document OpenOffice source
	 * @param targetFormat Format de sortie
	 * @param openOfficeConnection Connection � OpenOffice
	 * @throws Exception Erreur de traitement
	 */
	protected abstract void storeDocument(final File outputFile, final XComponent xDoc, final ConverterFormat targetFormat, final OpenOfficeConnection openOfficeConnection) throws Exception;

	/**
	 * Lecture d'un docuement.
	 * @param inputFile Fichier source
	 * @param openOfficeConnection  Connection � OpenOffice
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
			openOfficeConnection.connect(); //Attention d�j� observ� : connection ne s'�tablissant pas et pas de timeout
		} catch (final ConnectException connectException) {
			//On pr�cise les causes possibles de l'erreur.
			final StringBuilder sb = new StringBuilder();
			sb.append("Dans le fichier OOoBasePath\\Basis\\share\\registry\\data\\org\\openoffice\\Setup.xcu.\n");
			sb.append("Juste apr�s cette ligne-ci : <node oor:name=\"Office\">\n");
			sb.append("Il faut ajouter les lignes suivantes :\n");
			sb.append("<prop oor:name=\"ooSetupConnectionURL\" oor:type=\"xs:string\">\n");
			sb.append("<value>socket,host=localhost,port=").append(unoPort).append(";urp;</value>\n");
			sb.append("</prop>\n");
			sb.append("Ensuite, il faut lancer OpenOffice... et l'agent OpenOffice si il tourne.");

			throw new IOException("Impossible de se connecter � OpenOffice, v�rifier qu'il est bien en �coute sur le port " + unoPort + ".\n\n" + sb.toString(), connectException);
		}
		return openOfficeConnection;
	}

	private void refreshDocument(final XComponent document) {
		final XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
		if (refreshable != null) {
			refreshable.refresh();
		}
	}

	private PropertyValue[] getFileProperties(final ConverterFormat docType, final XOutputStream outputStream, final XInputStream inputStream) {
		Assertion.checkNotNull(docType, "Le type du format de sortie est obligatoire");
		Assertion.checkArgument(outputStream == null || inputStream == null, "Les properties pointent soit un fichier local, soit un flux d'entr�e, soit un flux de sortie");
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
	protected final PropertyValue[] getFileProperties(final ConverterFormat docType) {
		return getFileProperties(docType, null, null);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion et un flux d'ecriture.
	 * @param docType docType format
	 * @param outputStream Flux d'ecriture
	 * @return Proterties
	 */
	protected final PropertyValue[] getFileProperties(final ConverterFormat docType, final XOutputStream outputStream) {
		return getFileProperties(docType, outputStream, null);
	}

	/**
	 * Fournit les proterties de fichier pour un format de conversion et un flux de lecture.
	 * @param docType docType format
	 * @param inputStream Flux de lecture
	 * @return Proterties
	 */
	protected final PropertyValue[] getFileProperties(final ConverterFormat docType, final XInputStream inputStream) {
		return getFileProperties(docType, null, inputStream);
	}

	/**
	 * @param docType Format de conversion
	 * @return filterName g�r� par OpenOffice pour lui pr�ciser le format de conversion
	 */
	protected final String getFilterNameFromExtension(final ConverterFormat docType) {
		//Liste des filterName g�r� par OpenOffice.
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
				throw new InvalidParameterException("Type de document non g�r� : " + docType);
		}
	}

	//	protected void main(final String[] args) {
	//		final ConverterOpenOffice converter = new ConverterOpenOffice("PDF");
	//		String dirPath = System.getProperty("java.io.tmpdir");
	//		dirPath += File.separator;
	//		final File tempDir = new File(dirPath);
	//		File inputFile;
	//		File outputFile;
	//		final FilenameFilter filter = new FilenameFilter() {
	//			public boolean accept(final File dir, final String name) {
	//				return name.endsWith(".odt");
	//			}
	//		};
	//		println("nb ODT files in " + dirPath + " : " + tempDir.list(filter).length);
	//		for (final String fileName : tempDir.list(filter)) {
	//			inputFile = new File(dirPath + fileName);
	//			println("converting : " + inputFile.getAbsolutePath());
	//			try {
	//				outputFile = converter.conversionVersFormat(inputFile);
	//				println("output: " + outputFile.getAbsolutePath());
	//			} catch (final KSystemException e) {
	//				println("ERREUR de conversion sur : " + dirPath + fileName);
	//				e.printStackTrace();
	//			}
	//		}
	//		System.exit(0);
	//	}

}
