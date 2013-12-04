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
package io.vertigo.kernel.component;

import io.vertigo.kernel.lang.Assertion;

import java.util.Date;

/**
 * Conteneur d'informations d'un composant.
 *
 * Contient une combinaison de : 
 *  - un titre
 *  - une valeur 
 *
 * @author npiedeloup
 */
public final class ComponentInfo {
	private Object value;
	private String title;

	/**
	 * @param value Valeur chaine ou num�rique
	 */
	private ComponentInfo(final String title, final Object value, boolean dummy) {
		Assertion.checkArgNotEmpty(title);
		//-----------------------------
		this.title = title;
		this.value = value;
	}

	public ComponentInfo(final String title, final boolean value) {
		this(title, value, false);
	}

	public ComponentInfo(final String title, final String value) {
		this(title, value, false);
	}

	public ComponentInfo(final String title, final Long value) {
		this(title, value, false);
	}

	public ComponentInfo(final String title, final Integer value) {
		this(title, value, false);
	}

	public ComponentInfo(final String title, final Double value) {
		this(title, value, false);
	}

	public ComponentInfo(final String title, final Date value) {
		this(title, value, false);
	}

	/**
	 * @return Titre ou unit� de la valeur
	 */
	public String getTitle() {
		return title;
	}

	public Object getValue() {
		return value;
	}
	//	/**
	//	 * @return Valeur convertie en HTML
	//	 */
	//	public String getFormattedValue() {
	//		return value;
	//	}
	//
	//	private static String formatValue(final Object value) {
	//		final String formattedValue;
	//		if (value instanceof Boolean) {
	//			formattedValue = String.valueOf(value);
	//		} else if (value instanceof String) {
	//			formattedValue = String.valueOf(value);
	//		} else if (value instanceof Long || value instanceof Integer) {
	//			formattedValue = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.FRENCH)).format(value);
	//		} else if (value instanceof Double) {
	//			formattedValue = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.FRENCH)).format(value);
	//		} else {
	//			throw new IllegalArgumentException("Format non impl�ment�");
	//		}
	//		return formattedValue;
	//	}
}
