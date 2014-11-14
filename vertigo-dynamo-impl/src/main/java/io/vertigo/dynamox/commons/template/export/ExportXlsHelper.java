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
package io.vertigo.dynamox.commons.template.export;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.model.ExportBuilder;
import io.vertigo.dynamo.export.model.ExportFormat;
import io.vertigo.dynamo.export.model.ExportSheetBuilder;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper pour les editions xls.
 * 
 * @author kleegroup
 * @param <R>
 *            Type d'objet pour la liste
 */
public class ExportXlsHelper<R extends DtObject> {
	private final ExportBuilder exportBuilder;

	/**
	 * Constructeur.
	 * 
	 * @param fileName
	 *            nom du fichier résultat de l'export
	 * @param title
	 *            titre de la feuille principale de l'export
	 */
	public ExportXlsHelper(final String fileName, final String title) {
		Assertion.checkNotNull(fileName);
		// ---------------------------------------------------------------------
		exportBuilder = new ExportBuilder(ExportFormat.XLS, fileName)
				.withTitle(title);
	}

	/**
	 * Prepare the export generation. If the screen allows 2 exports, then one
	 * must use 2 actions
	 * 
	 * @param dtcToExport
	 *            the objects collection to be exported
	 * @param collectionColumnNames
	 *            list of the columns taht must be exported in the collection
	 * @param criterion
	 *            search criterion if exists
	 * @param criterionExcludedColumnNames
	 *            list of the criteria that must be excluded for the export
	 * @param specificLabelMap
	 *            map of the column names to be used instead of the default
	 *            label associated with the field
	 */
	public final void prepareExport(final DtList<R> dtcToExport, final List<String> collectionColumnNames, final DtObject criterion, final List<String> criterionExcludedColumnNames, final Map<String, String> specificLabelMap) {

		addDtList(dtcToExport, collectionColumnNames, specificLabelMap);

		// We add a criteria page if exists
		if (criterion != null) {
			addDtObject(criterion, criterionExcludedColumnNames);
		}
	}

	/**
	 * Add a DTC to the export.
	 * 
	 * @param dtcToExport
	 *            collection to be exported
	 * @param collectionColumnNameList
	 *            names of the columns that must be exported
	 * @param specificLabelMap
	 *            map of the column names to be used instead of the default
	 *            label associated with the field
	 */
	public final void addDtList(final DtList<R> dtcToExport, final List<String> collectionColumnNameList, final Map<String, String> specificLabelMap) {
		Assertion.checkArgument(dtcToExport != null && dtcToExport.size() > 0, "The list of the objects to be exported must exist and not be empty");
		Assertion.checkArgument(collectionColumnNameList != null && !collectionColumnNameList.isEmpty(), "The list of the columns to be exported must exist and not be empty");

		// --------------------------------------------

		final ExportSheetBuilder exportSheetBuilder = exportBuilder.beginSheet(dtcToExport, null);

		// exportListParameters.setMetaData(PublisherMetaData.TITLE, tabName);
		for (final DtField dtField : getExportColumnList(dtcToExport, collectionColumnNameList)) {
			if (specificLabelMap == null) {
				exportSheetBuilder.withField(dtField);
			} else {
				// final String label = specificLabelMap.get(field.getName());
				// TODO exportListParameters.addExportField(field, label);
				exportSheetBuilder.withField(dtField, null);
			}
		}
		exportSheetBuilder.endSheet();
	}

	/**
	 * Add a criterion to the export.
	 * 
	 * @param criterion
	 *            criterion object to be exported
	 * @param criterionExcludedColumnNames
	 *            names of the columns to be excluded
	 */
	public final void addDtObject(final DtObject criterion, final List<String> criterionExcludedColumnNames) {
		Assertion.checkNotNull(criterion);
		Assertion.checkArgument(criterionExcludedColumnNames != null, "The list of the columns to be excluded must exist");

		// --------------------------------------------

		final ExportSheetBuilder exportSheetBuilder = exportBuilder.beginSheet(criterion, null);

		// exportObjectParameters.setMetaData(PublisherMetaData.TITLE, tabName);
		for (final DtField dtField : getExportCriterionFields(criterion, criterionExcludedColumnNames)) {
			exportSheetBuilder.withField(dtField);
		}

		exportSheetBuilder.endSheet();
	}

	/**
	 * Traduit la liste des champs à exporter en liste de DtField.
	 * 
	 * @param list
	 *            Liste à exporter
	 * @param collectionColumnNames
	 *            Liste des noms de champs à exporter
	 * @return Liste des DtField correspondant
	 */
	private List<DtField> getExportColumnList(final DtList<R> list, final List<String> collectionColumnNames) {
		final List<DtField> exportColumns = new ArrayList<>();

		for (final String field : collectionColumnNames) {
			exportColumns.add(list.getDefinition().getField(field));
		}
		return exportColumns;
	}

	/**
	 * Détermine la liste des champs du critère à exporter en liste de DtField.
	 * 
	 * @param dto
	 *            DtObject à exporter
	 * @param criterionExcludedColumnNameList
	 *            Liste des noms de champs à NE PAS exporter
	 * @return Liste des DtField à exporter
	 */
	private List<DtField> getExportCriterionFields(final DtObject dto, final List<String> criterionExcludedColumnNameList) {
		final List<DtField> exportColumns = new ArrayList<>();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		addFieldToExcludedExportColumnNameList(dtDefinition, criterionExcludedColumnNameList);

		for (final DtField dtField : dtDefinition.getFields()) {
			if (!criterionExcludedColumnNameList.contains(dtField.getName())) {
				exportColumns.add(dtField);
			}
		}
		return exportColumns;
	}

	private void addFieldToExcludedExportColumnNameList(final DtDefinition definition, final List<String> criterionExcludedColumnNameList) {
		if (definition.getIdField().isDefined()) {
			final DtField keyField = definition.getIdField().get();
			if ("DO_IDENTIFIER".equals(keyField.getDomain().getName())) {
				criterionExcludedColumnNameList.add(keyField.getName());
			}
		}
	}
}
