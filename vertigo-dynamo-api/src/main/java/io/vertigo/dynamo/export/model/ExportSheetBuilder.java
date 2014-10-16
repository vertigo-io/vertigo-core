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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.lang.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parametre d'export pour les données de type DT.
 * La particularité est que l'on fournit la liste des colonnes du DT a exporter,
 * avec éventuellement des paramètres d'affichage particulier pour une colonne.
 * @author pchretien, npiedeloup
 */
public final class ExportSheetBuilder implements Builder<ExportSheet> {
	/**
	 * List des champs à exporter
	 */
	private final List<ExportField> exportFields = new ArrayList<>();

	/**
	 * Objet à exporter. dto XOR dtc est renseigné.
	 */
	private final DtObject dto;
	private final DtList<?> dtc;
	private final DtDefinition dtDefinition;

	private final String title;
	private final ExportBuilder exportBuilder;

	/**
	 * Constructeur.
	 * 
	 * @param dto
	 *            DTO à exporter
	 */
	ExportSheetBuilder(final ExportBuilder exportBuilder, final DtObject dto, final String title) {
		Assertion.checkNotNull(exportBuilder);
		Assertion.checkNotNull(dto);
		// title may be null
		// ---------------------------------------------------------------------
		this.exportBuilder = exportBuilder;
		this.dto = dto;
		dtc = null;
		this.title = title;
		dtDefinition = DtObjectUtil.findDtDefinition(dto);
	}

	/**
	 * Constructeur.
	 * 
	 * @param dtc
	 *            DTC à exporter
	 */
	ExportSheetBuilder(final ExportBuilder exportBuilder, final DtList<?> dtc, final String title) {
		Assertion.checkNotNull(exportBuilder);
		Assertion.checkNotNull(dtc);
		// title may be null
		// ---------------------------------------------------------------------
		this.exportBuilder = exportBuilder;
		this.dtc = dtc;
		dto = null;
		this.title = title;
		dtDefinition = dtc.getDefinition();
	}

	/**
	 * Ajoute un champs du Dt dans l'export, le label de la colonne sera celui indiqué dans le DT pour ce champs.
	 * @param exportfield ajout d'un champs du Dt à exporter
	 */
	public ExportSheetBuilder withField(final DtField exportfield) {
		withField(exportfield, null);
		return this;
	}

	/**
	 * Ajoute un champs du Dt dans l'export, le label de la colonne sera celui indiqué dans le DT pour ce champs.
	 * @param exportfield ajout d'un champs du Dt à exporter
	 * @param list Liste des éléments dénormés
	 * @param displayfield Field du libellé à utiliser.
	 */
	public ExportSheetBuilder withField(final DtField exportfield, final DtList<?> list, final DtField displayfield) {
		withField(exportfield, list, displayfield, null);
		return this;
	}

	/**
	 * @param exportfield ajout d'un champs du Dt à exporter
	 * @param label nom spécifique à utiliser dans l'export, null si l'on souhaite utiliser celui indiqué dans le DT pour ce champs
	 */
	public ExportSheetBuilder withField(final DtField exportfield, final MessageText overridedLabel) {
		Assertion.checkNotNull(exportfield);
		// On vérifie que la colonne est bien dans la définition de la DTC
		Assertion.checkArgument(dtDefinition.getFields().contains(exportfield), "Le champ " + exportfield.getName() + " n'est pas dans la liste à exporter");
		// On ne vérifie pas que les champs ne sont placés qu'une fois
		// car pour des raisons diverses ils peuvent l'être plusieurs fois.
		// ----------------------------------------------------------------------
		final ExportField exportField = new ExportField(exportfield, overridedLabel);
		exportFields.add(exportField);
		return this;
	}

	/**
	 * @param exportfield ajout d'un champs du Dt à exporter
	 * @param list Liste des éléments dénormés
	 * @param displayfield Field du libellé à utiliser.
	 * @param label nom spécifique à utiliser dans l'export, null si l'on souhaite utiliser celui indiqué dans le DT pour ce champs
	 */
	public ExportSheetBuilder withField(final DtField exportfield, final DtList<?> list, final DtField displayfield, final MessageText overridedLabel) {
		Assertion.checkNotNull(exportfield);
		// On vérifie que la colonne est bien dans la définition de la DTC
		Assertion.checkArgument(dtDefinition.getFields().contains(exportfield), "Le champ " + exportfield.getName() + " n'est pas dans la liste à exporter");
		// On ne vérifie pas que les champs ne sont placés qu'une fois
		// car pour des raisons diverses ils peuvent l'être plusieurs fois.
		// ----------------------------------------------------------------------
		final ExportField exportField = new ExportDenormField(exportfield, overridedLabel, list, displayfield);
		exportFields.add(exportField);
		return this;
	}

	/** {@inheritDoc} */
	public ExportSheet build() {
		if (exportFields.isEmpty()) {
			// si la liste des colonnes est vide alors par convention on les
			// prend toutes.
			final Collection<DtField> fields = dtDefinition.getFields();
			for (final DtField dtField : fields) {
				exportFields.add(new ExportField(dtField, null));
			}
		}
		return new ExportSheet(title, exportFields, dto, dtc);
	}

	public ExportBuilder endSheet() {
		return exportBuilder.withSheet(this.build());
	}
}
