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
package io.vertigo.dynamo.impl.database.statementhandler;

import java.io.Serializable;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.lang.Assertion;

/**
 * Classe interne décrivant les champs d'une définition.
 * Permet de serialiser une DT qui par nature n'est pas sérialisable.
 * @author pchretien
 */
final class SerializableDtField implements Serializable {
	private static final long serialVersionUID = 7086269816597674149L;
	private final String name;
	private final String label;
	private final DataType dataType;

	/**
	 * @param fieldName Field name
	 * @param fieldLabel Field label
	 * @param dataType Datatype
	 */
	SerializableDtField(final String fieldName, final String fieldLabel, final DataType dataType) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(fieldLabel);
		Assertion.checkNotNull(dataType);
		//-----
		name = fieldName;
		label = fieldLabel;
		this.dataType = dataType;
	}

	/**
	 * @return Name
	 */
	String getName() {
		return name;
	}

	/**
	 *  @return Label
	 */
	String getLabel() {
		return label;
	}

	/**
	 * @return Datatype
	 */
	DataType getDataType() {
		return dataType;
	}
}
