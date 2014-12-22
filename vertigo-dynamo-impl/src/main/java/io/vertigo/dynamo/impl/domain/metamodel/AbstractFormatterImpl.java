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
package io.vertigo.dynamo.impl.domain.metamodel;

import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.lang.Assertion;

/**
 * Implémentation standard des formatters.
 * Un formatter est un objet partagé, par nature il est non modifiable.
 *
 * @author pchretien
 */
public abstract class AbstractFormatterImpl implements Formatter {
	/**
	 * Nom du formatteur.
	 */
	private final String name;

	/**
	 * Constructeur.
	 */
	protected AbstractFormatterImpl(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		this.name = name;
	}

	/**
	 * Initialisation du Formatter par des arguments passés en chaine de caractères.
	 * @param args Paramétrage du Formatter
	 */
	public abstract void initParameters(String args);

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}
}
