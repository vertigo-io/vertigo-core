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
package io.vertigo.dynamo.export;

import io.vertigo.dynamo.export.Export.Orientation;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder des données à exporter.
 * @author pchretien, npiedeloup
 * @version $Id: ExportBuilder.java,v 1.6 2014/02/27 10:23:30 pchretien Exp $
 */
public final class ExportBuilder implements Builder<Export> {
	private final List<ExportDtParametersReadable> exportDtParameters = new ArrayList<>();

	private final ExportFormat format;
	private final String fileName;

	// Variables à affecter par des SETTERS
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
		Assertion.checkArgNotEmpty(fileName, "FileName doit être non vide");
		// ----------------------------------------------------------------------
		this.format = format;
		this.fileName = fileName;
	}

	/**
	 * @param newTitle Titre du document (Facultatif)
	 */
	public ExportBuilder withTitle(final String newTitle) {
		Assertion.checkState(title == null, "Titre deja renseigné");
		Assertion.checkArgNotEmpty(newTitle, "Titre doit être non vide");
		// ---------------------------------------------------------------------
		title = newTitle;
		return this;
	}

	/**
	 * @param newAuthor Auteur du document (Facultatif)
	 */
	public ExportBuilder withAuthor(final String newAuthor) {
		Assertion.checkState(author == null, "Auteur deja renseigné");
		Assertion.checkArgNotEmpty(newAuthor, "Auteur doit être non vide");
		// ---------------------------------------------------------------------
		author = newAuthor;
		return this;
	}

	/**
	 * @param newOrientation Orientation du document (Facultatif, mode portrait par défaut)
	 */
	public ExportBuilder withOrientation(final Orientation newOrientation) {
		Assertion.checkNotNull(newOrientation);
		// ---------------------------------------------------------------------
		orientation = newOrientation;
		return this;
	}

	/**
	 * @param dtParameter parametre de données(DTO ou DTC) à ajouter à ce document.
	 */
	public ExportBuilder withExportDtParameters(final ExportDtParameters dtParameter) {
		Assertion.checkNotNull(dtParameter);
		Assertion.checkArgument(dtParameter instanceof ExportDtParametersReadable, "Le paramètre doit avoir une interface de consultation");
		//---------------------------------------------------------------------
		// On est obligatoirement dans une implémentation standard (homogénéité)
		exportDtParameters.add((ExportDtParametersReadable) dtParameter);
		return this;
	}

	/** {@inheritDoc} */
	public Export build() {
		return new Export(format, fileName, title, author, orientation, exportDtParameters);
	}
}
