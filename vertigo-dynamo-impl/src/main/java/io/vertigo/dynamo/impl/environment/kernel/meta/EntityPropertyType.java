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
package io.vertigo.dynamo.impl.environment.kernel.meta;

import io.vertigo.lang.Assertion;

/**
 * @author  pchretien
 */
public enum EntityPropertyType {
	/** Integer. */
	Integer(Integer.class),
	/** Double. */
	Double(Double.class),
	/** Boolean. */
	Boolean(Boolean.class),
	/** String. */
	String(String.class);

	/**
	 * Classe java que le Type encapsule.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructeur.
	 *
	 * @param javaClass Classe java encapsulée
	 */
	private EntityPropertyType(final Class<?> javaClass) {
		Assertion.checkNotNull(javaClass);
		//-----
		this.javaClass = javaClass;
	}

	public void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Valeur " + value + " ne correspond pas au type :" + this);
		}
	}

	public Object cast(final String stringValue) {
		final String sValue = stringValue == null ? null : stringValue.trim();
		if (sValue == null || sValue.length() == 0) {
			return null;
		}
		switch (this) {
			case Integer:
				return java.lang.Integer.valueOf(sValue);
			case Double:
				return java.lang.Double.valueOf(sValue);
			case String:
				return sValue;
			case Boolean:
				return java.lang.Boolean.valueOf(sValue);
			default:
				throw new IllegalArgumentException("cast de la propriété '" + javaClass + "' non implémenté");
		}
	}
}
