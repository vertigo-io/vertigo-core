package io.vertigo.dynamo.impl.export;

import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.kernel.component.Plugin;

import java.io.OutputStream;

/**
 * Plugin de l'export.
 * Les param�tres qui lui sont associ�s permettent d'agir sur le resultat de l'export.
 * Le plugin accepte toutes les exceptions afin de centraliser leur gestion en un seul endroit.
 *
 * @author pchretien, npiedeloup
 * @version $Id: ExporterPlugin.java,v 1.2 2013/10/22 12:08:28 pchretien Exp $
 */
public interface ExporterPlugin extends Plugin {
	/**
	 * R�alise l'export des donn�es de contenu.
	 * @param metaParameters param�tres de cet export
	 * @param out Le flux d'�criture des donn�es export�es.
	 * @throws Exception Exception g�n�rique en cas d'erreur lors de l'export
	 */
	void exportData(final Export metaParameters, final OutputStream out) throws Exception;

	/**
	 * Type de Format accept� � l'export
	 * @param exportFormat
	 * @return si le format pr�cis� est pris en compte par le plugin
	 */
	boolean accept(ExportFormat exportFormat);

}
