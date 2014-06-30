package io.vertigo.quarto.plugins.converter.openoffice;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.plugins.converter.openoffice.stream.OOoFileInputStream;
import io.vertigo.quarto.plugins.converter.openoffice.stream.OOoFileOutputStream;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;

/**
 * Conversion des fichiers � partir de OpenOffice.
 * @author npiedeloup
 * @version $Id: OpenOfficeRemoteConverterPlugin.java,v 1.4 2014/01/28 18:49:24 pchretien Exp $
 */
public final class OpenOfficeRemoteConverterPlugin extends AbstractOpenOfficeConverterPlugin {
	private static final Logger LOGGER = Logger.getLogger(OpenOfficeRemoteConverterPlugin.class);

	/**
	 * Constructeur.
	 * @param fileManager Manager de gestion des fichiers
	 * @param unoHost Hote du serveur OpenOffice
	 * @param unoPort Port de connexion au serveur OpenOffice
	 */
	@Inject
	public OpenOfficeRemoteConverterPlugin(final FileManager fileManager, @Named("unohost") final String unoHost, @Named("unoport") final String unoPort) {
		super(fileManager, unoHost, unoPort);
	}

	/** {@inheritDoc} */
	@Override
	protected void storeDocument(final File outputFile, final XComponent xDoc, final ConverterFormat targetFormat, final OpenOfficeConnection openOfficeConnection)
			throws Exception {
		final XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, xDoc);
		final XOutputStream outputStream = new OOoFileOutputStream(outputFile);
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Storing to " + outputFile.getAbsolutePath());
			}
			final PropertyValue[] fileProps = getFileProperties(targetFormat, outputStream);
			xStorable.storeToURL("private:stream", fileProps);
		} finally {
			outputStream.closeOutput();
		}
	}

	/** {@inheritDoc} */
	@Override
	protected XComponent loadDocument(final File inputFile, final OpenOfficeConnection openOfficeConnection) throws Exception {
		final String inputUrl = inputFile.getAbsolutePath();

		final String inputExtensionStr = inputUrl.substring(inputUrl.lastIndexOf('.') + 1).toUpperCase();
		final ConverterFormat docType = ConverterFormat.valueOf(inputExtensionStr);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Openning document... " + inputUrl);
		}
		final XInputStream inputStream = new OOoFileInputStream(inputFile);
		try {
			final PropertyValue[] loadProps = getFileProperties(docType, inputStream);
			final XComponent xDoc = openOfficeConnection.getDesktop().loadComponentFromURL("private:stream", "_blank", 0, loadProps);

			//---------------------------------------------------------------------
			Assertion.checkNotNull(xDoc, "Le document n''a pas �t� charg� : {0}", inputUrl);
			return xDoc;
		} finally {
			inputStream.closeInput();
		}

	}

}
