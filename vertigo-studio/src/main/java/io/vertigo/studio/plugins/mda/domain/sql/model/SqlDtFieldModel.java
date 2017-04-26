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
package io.vertigo.studio.plugins.mda.domain.sql.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.util.DomainUtil;
import io.vertigo.util.StringUtil;

/**
 * Model used to define a DtField.
 *
 * @author pchretien
 */
public final class SqlDtFieldModel {
	private final DtDefinition dtDefinition;
	private final DtField dtField;

	/***
	 * Constructeur.
	 * @param dtField Champ à générer
	 */
	SqlDtFieldModel(final DtDefinition dtDefinition, final DtField dtField) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtField);
		//-----
		this.dtDefinition = dtDefinition;
		this.dtField = dtField;
	}

	/**
	 * Nom du champ en majuscules séparés par des _.
	 * @return UN_NOM
	 */
	public String getName() {
		return dtField.getName();
	}

	/**
	 * @return DtDefinition
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	public boolean isId() {
		return dtField.getType() == FieldType.ID;
	}

	/**
	 * @return DtField
	 */
	public DtField getSource() {
		return dtField;
	}

	public String getLabel() {
		return dtField.getLabel().getDisplay();
	}

	/**
	 * Nom du champ en CamelCase.
	 * La premiére lettre est en majuscule
	 * si besoin la première lettre en miniscule avec FreeMarker : ${dtField.nameLowerCase?uncap_first}
	 * @return UnNom
	 */
	public String getNameLowerCase() {
		return StringUtil.constToUpperCamelCase(dtField.getName());
	}

	public String getNameCamelCase() {
		return StringUtil.constToLowerCamelCase(dtField.getName());
	}

	/**
	 * @return Type du champ (
	 */
	public String getType() {
		return dtField.getType().name();
	}

	/**
	 * @return Type java du champ
	 */
	public String getJavaType() {
		return DomainUtil.buildJavaType(dtField.getDomain());
	}

	/**
	 * @return Label du champ
	 */
	public String getDisplay() {
		return dtField.getLabel().getDisplay();
	}

	/**
	 * @return Si la propriété est requise
	 */
	public boolean isRequired() {
		return dtField.isRequired();
	}

	public boolean isPersistent() {
		return dtField.isPersistent();
	}

	/**
	 * @return Code java correspondant à l'expression de ce champ calculé
	 */
	public String getJavaCode() {
		return dtField.getComputedExpression().getJavaCode();
	}

	public boolean isForeignKey() {
		return dtField.getType() == FieldType.FOREIGN_KEY;
	}
}
