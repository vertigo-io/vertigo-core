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

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Model used to define a DtField.
 *
 * @author pchretien
 */
public final class JSDtFieldModel {
	private final DtField dtField;

	/***
	 * Constructeur.
	 * @param dtField Champ Ã  gÃ©nÃ©rer
	 */
	JSDtFieldModel(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//-----
		this.dtField = dtField;
	}

	public String getCamelCaseName() {
		return StringUtil.constToLowerCamelCase(dtField.getName());
	}

	/**
	 * @return Type javascript du champ with cardinality
	 */
	public String getJavascriptType() {
		return buildJavascriptType(dtField.getDomain(), true);
	}

	/**
	 * @return Label du champ
	 */
	public String getLabel() {
		return dtField.getLabel().getDisplay();
	}

	/**
	 * @return Name of the domain
	 */
	public String getDomainName() {
		return dtField.getDomain().getName();
	}

	/**
	 * @return Local name of the domain
	 */
	public String getDomainDefinitionName() {
		return dtField.getDomain().getDtDefinition().getLocalName();
	}

	/**
	 * @return Simple TS type 
	 */
	public String getDomainTypeName() {
		return buildJavascriptType(dtField.getDomain(), false);
	}

	/**
	 * @return Si la propriÃ©tÃ© est requise
	 */
	public boolean isRequired() {
		return dtField.isRequired();
	}

	/**
	 * @return True si le type est une primitive.
	 */
	public boolean isPrimitive() {
		return dtField.getDomain().getDataType().isPrimitive();
	}

	/**
	 * @return True si le type est une liste.
	 */
	public boolean isList() {
		return dtField.getDomain().getDataType() == DataType.DtList;
	}

	/**
	 * Returns the javascript type.
	 * @param  domain DtDomain
	 * @return String
	 */
	private static String buildJavascriptType(final Domain domain, final boolean withArray) {
		final DataType dataType = domain.getDataType();
		if (dataType.isPrimitive()) {
			if (dataType.isNumber()) {
				return "number";
			} else if (dataType == DataType.Boolean) {
				return "boolean";
			}
			return "string";
		}
		switch (dataType) {
			case DtObject:
				return getSimpleName(domain);
			case DtList:
				return getSimpleName(domain) + (withArray ? "[]" : "");
			case BigDecimal:
			case Boolean:
			case DataStream:
			case Date:
			case LocalDate:
			case ZonedDateTime:
			case Double:
			case Integer:
			case Long:
			case String:
				throw new IllegalArgumentException("Type unsupported : " + dataType);
			default:
				throw new IllegalArgumentException("Type unknown : " + dataType);
		}
	}

	private static String getSimpleName(final Domain domain) {
		//on rÃ©cupÃ¨re le DT correspondant au nom passÃ© en paramÃ¨tre
		final DtDefinition dtDefinition = domain.getDtDefinition();
		return dtDefinition.getClassSimpleName();
	}

}
