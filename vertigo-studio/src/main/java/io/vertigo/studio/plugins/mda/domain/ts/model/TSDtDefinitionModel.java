/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.domain.ts.model;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.lang.Assertion;

/**
 * Model used by FreeMarker.
 *
 * @author pchretien
 */
public final class TSDtDefinitionModel {
	private final DtDefinition dtDefinition;
	private final List<TSDtFieldModel> dtFieldModels;

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet Ã  gÃ©nÃ©rer
	 */
	public TSDtDefinitionModel(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;

		dtFieldModels = dtDefinition.getFields().stream()
				.filter(dtField -> FieldType.COMPUTED != dtField.getType())
				.map(TSDtFieldModel::new)
				.collect(Collectors.toList());
	}

	/**
	 * @return DT dÃ©finition
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implÃ©mentation du DtObject
	 */
	public String getClassSimpleName() {
		return dtDefinition.getClassSimpleName();
	}

	/**
	 * @return true si au moins un champ est de type primitif.
	 */
	public boolean isContainsPrimitiveField() {
		return dtDefinition.getFields()
				.stream()
				.anyMatch(dtField -> dtField.getDomain().getDataType().isPrimitive());
	}

	/**
	 * @return true si au moins un champ est de type DtList.
	 */
	public boolean isContainsListField() {
		return getFields()
				.stream()
				.anyMatch(dtField -> dtField.isList());
	}

	/**
	 * @return true si au moins un champ est de type DtObject.
	 */
	public Boolean isContainsObjectField() {
		return dtDefinition.getFields()
				.stream()
				.anyMatch(dtField -> dtField.getDomain().getDataType() == DataType.DtObject);
	}

	/**
	 * @return Nom du fichier de la classe normalisÃ© (AAA_BBB_CCC => aaa-bbb-ccc).
	 */
	public String getJsClassFileName() {
		return dtDefinition.getLocalName().toLowerCase(Locale.ENGLISH).replaceAll("_", "-");
	}

	/**
	 * @return Nom du package
	 */
	public String getFunctionnalPackageName() {
		final String[] splittedPackage = dtDefinition.getPackageName().split("\\.");
		if (splittedPackage.length > 1) {
			return splittedPackage[splittedPackage.length - 1];
		}
		return dtDefinition.getPackageName();

	}

	/**
	 * @return Liste de champs
	 */
	public List<TSDtFieldModel> getFields() {
		return dtFieldModels;
	}
}
