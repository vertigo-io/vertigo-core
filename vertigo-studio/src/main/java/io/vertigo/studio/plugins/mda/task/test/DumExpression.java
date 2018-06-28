/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.task.test;

import java.util.Arrays;
import java.util.List;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;

/**
 * Représente une expression de valeur factice.
 * @author sezratty
 */
public class DumExpression {

	private final List<String> imports;
	private final String rawValue;
	private final boolean isRequired;

	/**
	 * Créé une expression factice.
	 * @param domain Domaine.
	 * @param isRequired Si champ obligatoire.
	 * @return Expression factice.
	 */
	public static DumExpression create(final Domain domain, final boolean isRequired) {
		if (domain.getScope().isPrimitive()) {
			switch (domain.getDataType()) {
				case Boolean:
					return new DumExpression("dum().booleen()", isRequired);
				case Long:
					return new DumExpression("dum().id()", isRequired);
				case Integer:
					return new DumExpression("dum().entier()", isRequired);
				case BigDecimal:
					return new DumExpression("dum().decimal()", isRequired);
				case Double:
					return new DumExpression("dum().montant()", isRequired);
				case String:
					return new DumExpression("dum().code()", isRequired);
				case Date:
					return new DumExpression("dum().date()", isRequired);
				case DataStream:
					return new DumExpression("null", false); // TODO dummy stream
				default:
					return null;
			}

		}

		if (domain.getScope().isDataObject() || domain.getScope().isValueObject()) {
			if (domain.isMultiple()) {
				final DtDefinition dtcDef = domain.getDtDefinition();
				return new DumExpression(
						"dum().dumList(" + dtcDef.getClassSimpleName() + ".class)",
						isRequired,
						dtcDef.getClassCanonicalName(),
						DtList.class.getCanonicalName());
			}
			final DtDefinition dtoDef = domain.getDtDefinition();
			return new DumExpression(
					"dum().dum(" + dtoDef.getClassSimpleName() + ".class)",
					isRequired,
					dtoDef.getClassCanonicalName());
		}

		return null;
	}

	private DumExpression(final String rawValue, final boolean isRequired, final String... imports) {
		this.rawValue = rawValue;
		this.isRequired = isRequired;
		this.imports = Arrays.asList(imports);
	}

	/**
	 * @return Expression de la valeur factice.
	 */
	public String getValue() {
		return isRequired ? rawValue : "Optional.of(" + rawValue + ")";
	}

	/**
	 * @return Liste des imports pour l'expression.
	 */
	public List<String> getImports() {
		return imports;
	}
}
