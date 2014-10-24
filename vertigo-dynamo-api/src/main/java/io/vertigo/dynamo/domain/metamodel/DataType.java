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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

import java.util.Date;

/**
 * Types.
 * On distingue :
 * - les types primitifs,
 * - types complexes.
 *
 * Les types complexes permettent de créer des objets composites.
 * Ils unifient le socle autour de la notion clé de domaine.
 *
 * @author  pchretien
 */
public enum DataType {
	/** Integer. */
	Integer(Integer.class, true),
	/** Double. */
	Double(Double.class, true),
	/** Boolean. */
	Boolean(Boolean.class, true),
	/** String. */
	String(String.class, true),
	/** Date. */
	Date(Date.class, true),
	/** BigDecimal. */
	BigDecimal(java.math.BigDecimal.class, true),
	/** Long. */
	Long(Long.class, true),
	/** DataStream. */
	DataStream(DataStream.class, true),
	/** DtObject. */
	DtObject(DtObject.class, false),
	/** DtList. */
	DtList(DtList.class, false);

	/**
	 * S'agit-il d'un type primitif
	 */
	private final boolean primitive;

	/**
	 * Classe java que le Type encapsule.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructeur.
	 *
	 * @param javaClass Classe java encapsulée
	 * @param primitive Si il s'agit d'un type primitif (sinon composite)
	 */
	private DataType(final Class<?> javaClass, final boolean primitive) {
		Assertion.checkNotNull(javaClass);
		//----------------------------------------------------------------------
		//Le nom est égal au type sous forme de String
		this.javaClass = javaClass;
		this.primitive = primitive;
	}

	/**
	 * Teste si la valeur passée en paramétre est est conforme au type.
	 * Lance une exception avec message adequat si pb.
	 * @param value Valeur é tester
	 */
	public void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Valeur " + value + " ne correspond pas au type :" + this);
		}
	}

	/**
	 * @return Classe java encapsulé/wrappée par le type
	 */
	public Class<?> getJavaClass() {
		return javaClass;
	}

	/**
	 * Il extiste deux types de types primitifs
	 * - les types simples ou primitifs (Integer, Long, String...),
	 * - les types composites (DtObject et DtList).
	 * @return boolean S'il s'agit d'un type primitif de la grammaire
	 */
	public boolean isPrimitive() {
		return primitive;
	}
}
