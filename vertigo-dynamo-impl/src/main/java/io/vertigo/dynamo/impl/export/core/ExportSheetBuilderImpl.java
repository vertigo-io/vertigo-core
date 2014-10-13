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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.dynamo.export.ExportSheet;
import io.vertigo.dynamo.export.ExportSheetBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation standard de ExportDtParameters.
 * 
 * @author pchretien, npiedeloup
 */
public final class ExportSheetBuilderImpl implements ExportSheetBuilder {
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

	/**
	 * Constructeur.
	 * 
	 * @param dto
	 *            DTO à exporter
	 */
	public ExportSheetBuilderImpl(final DtObject dto, final String title) {
		Assertion.checkNotNull(dto);
		// title may be null
		// ---------------------------------------------------------------------
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
	public ExportSheetBuilderImpl(final DtList<?> dtc, final String title) {
		Assertion.checkNotNull(dtc);
		// title may be null
		// ---------------------------------------------------------------------
		this.dtc = dtc;
		dto = null;
		this.title = title;
		dtDefinition = dtc.getDefinition();
	}

	/** {@inheritDoc} */
	public ExportSheetBuilder withField(final DtField exportfield) {
		withField(exportfield, null);
		return this;
	}

	/** {@inheritDoc} */
	public ExportSheetBuilder withField(final DtField exportfield, final DtList<?> list, final DtField displayfield) {
		withField(exportfield, list, displayfield, null);
		return this;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
}
