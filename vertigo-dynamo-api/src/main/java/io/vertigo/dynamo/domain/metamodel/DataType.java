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
package io.vertigo.dynamo.domain.metamodel;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.DataStream;

/**
 * Primitives types.
 *
 * @author  pchretien
 */
public enum DataType {
	/** Integer. */
	Integer(Integer.class),
	/** Double. */
	Double(Double.class),
	/** Boolean. */
	Boolean(Boolean.class),
	/** String. */
	String(String.class),
	/** Date. */
	@Deprecated
	Date(Date.class),
	/** LocalDate. */
	LocalDate(LocalDate.class),
	/** ZonedDateTime. */
	ZonedDateTime(ZonedDateTime.class),
	/** BigDecimal. */
	BigDecimal(java.math.BigDecimal.class),
	/** Long. */
	Long(Long.class),
	/** DataStream. */
	DataStream(DataStream.class);

	/**
	 * Classe java que le Type encapsule.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructor.
	 * @param javaClass the java class
	 */
	DataType(final Class<?> javaClass) {
		Assertion.checkNotNull(javaClass);
		//-----
		this.javaClass = javaClass;
	}

	/**
	 * Teste si la valeur passée en paramétre est est conforme au type.
	 * Lance une exception avec message adequat si pb.
	 * @param value Valeur é tester
	 */
	void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Value " + value + " doesn't match :" + this);
		}
	}

	public boolean isAboutDate() {
		return this == DataType.Date
				|| this == DataType.LocalDate
				|| this == DataType.ZonedDateTime;
	}

	public boolean isNumber() {
		return this == DataType.Double
				|| this == DataType.BigDecimal
				|| this == DataType.Long
				|| this == DataType.Integer;
	}

	/**
	 * @return Classe java encapsulé/wrappée par le type
	 */
	Class<?> getJavaClass() {
		return javaClass;
	}
}
