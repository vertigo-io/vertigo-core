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
import io.vertigo.core.lang.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtField;

/**
 * Définition d'une colonne à exporter.
 *
 * @author pchretien, npiedeloup
 */
public class ExportField {
	private final DtField dtField;
	private final MessageText label;

	/**
	 * Constructeur.
	 * @param dtField DtField
	 */
	public ExportField(final DtField dtField, final MessageText label) {
		Assertion.checkNotNull(dtField);
		//label may be null
		//---------------------------------------------------------------------
		this.dtField = dtField;
		this.label = label;
	}

	/**
	 * @return DtField
	 */
	public final DtField getDtField() {
		return dtField;
	}

	/**
	 * @return Label du dtField
	 */
	public final MessageText getLabel() {
		//Selon que le label est surchargé ou non
		return label != null ? label : dtField.getLabel();
	}
}
