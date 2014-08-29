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
package io.vertigo.dynamo.impl.export.core;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.ExportDtParameters;
import io.vertigo.dynamo.export.ExportDtParametersBuilder;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation standard de ExportDtParameters.
 *
 * @author pchretien, npiedeloup
 */
public final class ExportDtParametersBuilderImpl implements ExportDtParametersBuilder{
	/**
	 * List des champs à exporter
	 */
	private final List<ExportField> exportFields = new ArrayList<>();

	/**
	 * Objet à exporter.
	 * dto XOR dtc est renseigné.
	 */
	private final DtObject dto;
	private final DtList<?> dtc;
	private final DtDefinition dtDefinition;

	private String title;

	/**
	 * Constructeur.
	 * @param dto DTO à exporter
	 */
	public ExportDtParametersBuilderImpl(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		this.dto = dto;
		dtc = null;
		dtDefinition = DtObjectUtil.findDtDefinition(dto);
	}

	/**
	 * Constructeur.
	 * @param dtc DTC à exporter
	 */
	public ExportDtParametersBuilderImpl(final DtList<?> dtc) {
		Assertion.checkNotNull(dtc);
		//---------------------------------------------------------------------
		this.dtc = dtc;
		dto = null;
		dtDefinition = dtc.getDefinition();
	}

	/** {@inheritDoc} */
	public ExportDtParametersBuilder addExportField(final DtField exportfield) {
		addExportField(exportfield, null);
		return this;
	}

	/** {@inheritDoc} */
	public ExportDtParametersBuilder addExportDenormField(final DtField exportfield, final DtList<?> list, final DtField displayfield) {
		addExportDenormField(exportfield, list, displayfield, null);
		return this;
	}

	/** {@inheritDoc} */
	public ExportDtParametersBuilder addExportField(final DtField exportfield, final MessageText overridedLabel) {
		Assertion.checkNotNull(exportfield);
		// On vérifie que la colonne est bien dans la définition de la DTC
		Assertion.checkArgument(dtDefinition.getFields().contains(exportfield), "Le champ " + exportfield.getName() + " n'est pas dans la liste à exporter");
		// On ne vérifie pas que les champs ne sont placés qu'une fois
		// car pour des raisons diverses ils peuvent l'être plusieurs fois.
		// ----------------------------------------------------------------------
		final ExportField exportField = new ExportField(exportfield);
		if (overridedLabel != null) { // si on surcharge le label
			exportField.setLabel(overridedLabel);
		}
		exportFields.add(exportField);
		return this;
	}

	/** {@inheritDoc} */
	public ExportDtParametersBuilder addExportDenormField(final DtField exportfield, final DtList<?> list, final DtField displayfield, final MessageText overridedLabel) {
		Assertion.checkNotNull(exportfield);
		// On vérifie que la colonne est bien dans la définition de la DTC
		Assertion.checkArgument(dtDefinition.getFields().contains(exportfield), "Le champ " + exportfield.getName() + " n'est pas dans la liste à exporter");
		// On ne vérifie pas que les champs ne sont placés qu'une fois
		// car pour des raisons diverses ils peuvent l'être plusieurs fois.
		// ----------------------------------------------------------------------
		final ExportDenormField exportField = new ExportDenormField(exportfield, list, displayfield);
		if (overridedLabel != null) { // si on surcharge le label
			exportField.setLabel(overridedLabel);
		}
		exportFields.add(exportField);
		return this;
	}

	/**
	 * @param title Titre de cet objet/liste
	 */
	public void setTitle(final String title) {
		Assertion.checkState(title == null, "Titre deja renseigné");
		Assertion.checkArgNotEmpty(title, "Titre doit être non vide");
		// ---------------------------------------------------------------------
		this.title = title;
	}

	/** {@inheritDoc} */
	public ExportDtParameters build(){
		return new ExportDtParameters() {

			// NULL
			/** {@inheritDoc} */
			public String getTitle() {
				return title;
			}

			/** {@inheritDoc} */
			public boolean hasDtObject() {
				return dto != null;
			}

			/** {@inheritDoc} */
			public DtObject getDtObject() {
				Assertion.checkNotNull(dto);
				//---------------------------------------------------------------------
				return dto;
			}

			/** {@inheritDoc} */
			public DtList<?> getDtList() {
				Assertion.checkNotNull(dtc);
				//---------------------------------------------------------------------
				return dtc;
			}

			/** {@inheritDoc} */
			public List<ExportField> getExportFields() {
				if (exportFields.isEmpty()) {
					// si la liste des colonnes est vide alors par convention on les prend toutes.
					final Collection<DtField> fields= dtDefinition.getFields();
					final List<ExportField> defaultExportFieldList = new ArrayList<>(fields.size());
					for (final DtField dtField : fields) {
						defaultExportFieldList.add(new ExportField(dtField));
					}
					exportFields.addAll(defaultExportFieldList);
				}
				return java.util.Collections.unmodifiableList(exportFields);
			}

		};
	}
}
