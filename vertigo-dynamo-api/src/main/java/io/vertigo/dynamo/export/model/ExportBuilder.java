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
package io.vertigo.dynamo.export.model;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.export.model.Export.Orientation;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder des données à exporter.
 * @author pchretien, npiedeloup
 */
public final class ExportBuilder implements Builder<Export> {
	private final List<ExportSheet> sheets = new ArrayList<>();

	private final ExportFormat format;
	private final String fileName;

	// Variables à affecter par des SETTERS
	private String myTitle;
	private String myAuthor;
	private Export.Orientation myOrientation = Orientation.Portait;

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
	 * @param title Titre du document (Facultatif)
	 */
	public ExportBuilder withTitle(final String title) {
		myTitle = title;
		return this;
	}

	/**
	 * @param author Auteur du document (Facultatif)
	 */
	public ExportBuilder withAuthor(final String author) {
		myAuthor = author;
		return this;
	}

	/**
	 * @param orientation Orientation du document (Facultatif, mode portrait par défaut)
	 */
	public ExportBuilder withOrientation(final Orientation orientation) {
		myOrientation = orientation;
		return this;
	}

	/**
	 * @param dto DTO à exporter
	 * @param title Titre de l'objet
	 * @return Parametre d'export pour une donnée de type DtObject
	 */
	public ExportSheetBuilder beginSheet(final DtObject dto, final String title) {
		return new ExportSheetBuilder(this, dto, title);
	}

	/**
	 * @param dtc DTC à exporter
	 * @param title Titre de la liste
	 * @return Parametre d'export pour une donnée de type DtList
	 */
	public ExportSheetBuilder beginSheet(final DtList<?> dtc, final String title) {
		return new ExportSheetBuilder(this, dtc, title);
	}

	/**
	 * @param sheet parametre de données(DTO ou DTC) à ajouter à ce document.
	 */
	ExportBuilder withSheet(final ExportSheet sheet) {
		Assertion.checkNotNull(sheet);
		//---------------------------------------------------------------------
		sheets.add(sheet);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Export build() {
		return new Export(format, fileName, myTitle, myAuthor, myOrientation, sheets);
	}
}
