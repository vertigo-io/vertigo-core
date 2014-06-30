package io.vertigo.dynamo.export;

import io.vertigo.dynamo.export.Export.Orientation;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder des donn�es � exporter.
 * @author pchretien, npiedeloup
 * @version $Id: ExportBuilder.java,v 1.6 2014/02/27 10:23:30 pchretien Exp $
 */
public final class ExportBuilder implements Builder<Export> {
	private final List<ExportDtParametersReadable> exportDtParameters = new ArrayList<>();

	private final ExportFormat format;
	private final String fileName;

	// Variables � affecter par des SETTERS
	private String title;
	private String author;
	private Export.Orientation orientation = Orientation.Portait;

	/**
	 * Constructeur.
	 * @param format type du format de sortie. Ceci configurera le Handler de traitement de l'edition
	 * @param fileName nom du fichier de sortie.
	 */
	public ExportBuilder(final ExportFormat format, final String fileName) {
		Assertion.checkNotNull(format);
		Assertion.checkArgNotEmpty(fileName, "FileName doit �tre non vide");
		// ----------------------------------------------------------------------
		this.format = format;
		this.fileName = fileName;
	}

	/**
	 * @param newTitle Titre du document (Facultatif)
	 */
	public ExportBuilder withTitle(final String newTitle) {
		Assertion.checkState(title == null, "Titre deja renseign�");
		Assertion.checkArgNotEmpty(newTitle, "Titre doit �tre non vide");
		// ---------------------------------------------------------------------
		title = newTitle;
		return this;
	}

	/**
	 * @param newAuthor Auteur du document (Facultatif)
	 */
	public ExportBuilder withAuthor(final String newAuthor) {
		Assertion.checkState(author == null, "Auteur deja renseign�");
		Assertion.checkArgNotEmpty(newAuthor, "Auteur doit �tre non vide");
		// ---------------------------------------------------------------------
		author = newAuthor;
		return this;
	}

	/**
	 * @param newOrientation Orientation du document (Facultatif, mode portrait par d�faut)
	 */
	public ExportBuilder withOrientation(final Orientation newOrientation) {
		Assertion.checkNotNull(newOrientation);
		// ---------------------------------------------------------------------
		orientation = newOrientation;
		return this;
	}

	/**
	 * @param dtParameter parametre de donn�es(DTO ou DTC) � ajouter � ce document.
	 */
	public ExportBuilder withExportDtParameters(final ExportDtParameters dtParameter) {
		Assertion.checkNotNull(dtParameter);
		Assertion.checkArgument(dtParameter instanceof ExportDtParametersReadable, "Le param�tre doit avoir une interface de consultation");
		//---------------------------------------------------------------------
		// On est obligatoirement dans une impl�mentation standard (homog�n�it�)
		exportDtParameters.add((ExportDtParametersReadable) dtParameter);
		return this;
	}

	/** {@inheritDoc} */
	public Export build() {
		return new Export(format, fileName, title, author, orientation, exportDtParameters);
	}
}
