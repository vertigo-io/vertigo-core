/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.domain.metamodel.DataType;

/**
 * Représentation objet d'un paramètre d'un statement.
 * @author pchretien
 */
final class SqlParameter {
	private final DataType dataType;
	private final boolean in;
	private Object value;

	/**
	 * Constructeur.
	 * @param dataType KDataType
	 * @param in boolean
	 */
	SqlParameter(final DataType dataType, final boolean in) {
		this.dataType = dataType;
		this.in = in;
	}

	/**
	 * Sauvegarde des valeurs des paramètres de la Requêtes
	 * @param value Valeur du paramètre
	 */
	void setValue(final Object value) {
		this.value = value;
	}

	/**
	 * @return Si paramètre IN
	 */
	boolean isIn() {
		return in;
	}

	/**
	 * @return Si paramètre OUT
	 */
	boolean isOut() {
		return !in;
	}

	/**
	 * @return Type du paramètre
	 */
	DataType getDataType() {
		return dataType;
	}

	/**
	 * @return Valeur du paramètre
	 */
	Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		if (isIn()) {
			s.append("in");
		}
		if (isOut()) {
			s.append("out");
		}
		s.append('=');
		if (getValue() != null) {
			s.append(getValue());
		} else {
			s.append("null");
		}
		return s.toString();
	}
}
