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
package io.vertigo.dynamo.task.metamodel;

import io.vertigo.dynamo.domain.metamodel.ConstraintException;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Attribut d'une tache.
 * Il s'agit soit :
 *  - d'un type primitif
 *  - d'un type complexe : DTO ou DTC
 * Dans tous les cas il s'agit d'un {@link io.vertigo.dynamo.domain.metamodel.Domain}.
 *
 * Le paramètre peut être :
 *
 *  - en entrée, ou en sortie
 *  - obligatoire ou facultatif
 *
 * @author  fconstantin, pchretien
 */
public final class TaskAttribute {
	/** Name of the attribute. */
	private final String name;

	/**
	 * true : input 
	 * false: output
	 */
	private final boolean in;

	private final Domain domain;

	/** if the attribute is required. */
	private final boolean required;

	/**
	 * Constructor.
	 *
	 * @param attributeName the name of the attribute
	 * @param domain the domain of the attribute
	 * @param required if the attribute is required
	 * @param in if the attribute is an input (else output)
	 */
	TaskAttribute(final String attributeName, final Domain domain, final boolean required, final boolean in) {
		Assertion.checkNotNull(attributeName);
		Assertion.checkNotNull(domain);
		//-----
		name = attributeName;
		this.domain = domain;
		this.required = required;
		this.in = in;
	}

	/**
	 * @return the name of the attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns if the attribute is required in input or in output.
	 *
	 * @return if the attributre is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Returns true if the attribute is an input.
	 * 
	 * Conformément à java, les objets complexes peuvent être modifiés par la tache.
	 * Par exemple, tel DTO se verra doté d'une clé primaire lors de son premier
	 * enregistrement en base de données.
	 * @return If the attribute is an input.
	 */
	public boolean isIn() {
		return in;
	}

	/**
	 * @return Domain the domain
	 */
	public Domain getDomain() {
		return domain;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "{ name : " + name + ", domain :" + domain + ", in :" + in + ", required :" + required + "]";
	}

	/**
	 * Vérifie la cohérence des arguments d'un Attribute
	 * Vérifie que l'objet est cohérent avec le type défini sur l'attribut.
	 * @param value Valeur (Object primitif ou DtObject ou bien DtList)
	 */
	public void checkAttribute(final Object value) {
		if (isRequired()) {
			Assertion.checkNotNull(value, "Attribut task {0} ne doit pas etre null (cf. paramétrage task)", getName());
		}
		try {
			getDomain().checkValue(value);
		} catch (final ConstraintException e) {
			//On retransforme en Runtime pour conserver une API sur les getters et setters.
			throw new WrappedException(e);
		}
	}
}
