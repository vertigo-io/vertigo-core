package io.vertigo.dynamo.export;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Donn�es � exporter.
 * @author pchretien
 * @version $Id: Export.java,v 1.5 2014/02/27 10:23:30 pchretien Exp $
 */
public final class Export {
	/**
	 * Orientation des documents.
	 */
	public enum Orientation {
		/**Paysage.*/
		Landscape,
		/**Portait.*/
		Portait;
	}

	private final List<ExportDtParametersReadable> exportDtParameters;

	private final ExportFormat format;
	private final String fileName;
	private final String title;
	private final String author;
	private final Export.Orientation orientation;

	/**
	 * Constructeur.
	 * @param format type du format de sortie. Ceci configurera le Handler de traitement de l'edition
	 * @param fileName nom du fichier de sortie.
	 */
	Export(final ExportFormat format, final String fileName, final String title, final String author, final Export.Orientation orientation, final List<ExportDtParametersReadable> exportDtParameters) {
		Assertion.checkNotNull(format);
		Assertion.checkArgNotEmpty(fileName, "FileName doit �tre non vide");
		//Assertion.notNull(title);
		//Assertion.notNull(author);
		Assertion.checkNotNull(orientation);
		Assertion.checkNotNull(exportDtParameters);
		// ----------------------------------------------------------------------
		this.format = format;
		this.fileName = fileName;
		this.title = title;
		this.orientation = orientation;
		this.author = author;
		this.exportDtParameters = new ArrayList<>(exportDtParameters);
	}

	/**
	 * @return format de sortie du document
	 */
	public ExportFormat getFormat() {
		return format;
	}

	/**
	 * @return Nom du fichier
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return Titre du document (Facultatif)
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Auteur du document (Facultatif)
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return Orientation du document (mode portrait si non renseign�)
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * @return Liste des param�tres de donn�es � exporter
	 */
	public List<ExportDtParametersReadable> getReportDataParameters() {
		return exportDtParameters;
	}
}
