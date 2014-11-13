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

import io.vertigo.dynamo.domain.metamodel.Constraint;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;

/**
 * Par nature une contrainte est une ressource partagée et non modifiable.
 *
 * @author pchretien
 * @param <J> Type java de la propriété associée à la contrainte
 * @param <D> Type java de la valeur à contréler
 */
public abstract class AbstractConstraintImpl<J, D> implements Constraint<J, D> {
	/**
	 * Nom de la contrainte.
	 * On n'utilise pas les génériques car problémes.
	 */
	private final String name;

	/**
	 * Message d'erreur.
	 */
	private MessageText msg;

	/**
	 * Constructeur.
	 */
	protected AbstractConstraintImpl(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		this.name = name;
	}

	/**
	 * Initialisation de la contrainte par des arguments passés en chaine de caractères.
	 * @param args Paramétrage de la contrainte
	 */
	public abstract void initParameters(String args);

	/**
	 * Initialisation du message d'erreur si celui-ci est précisé de façon externe.
	 * @param newMsg Message d'erreur (Nullable)
	 */
	public final void initMsg(final MessageText newMsg) {
		this.msg = newMsg;
	}

	/**
	 * @return Message d'erreur (Nullable)
	 */
	@Override
	public final MessageText getErrorMessage() {
		return msg != null ? msg : getDefaultMessage();
	}

	/**
	 * @return Message par défaut
	 */
	protected abstract MessageText getDefaultMessage();

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
