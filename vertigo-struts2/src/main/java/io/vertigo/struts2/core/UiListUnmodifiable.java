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
package io.vertigo.struts2.core;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public final class UiListUnmodifiable<D extends DtObject> extends AbstractUiList<D> implements UiList<D> {
	private static final long serialVersionUID = 5475819598230056558L;

	private final DtList<D> dtList;

	/**
	 * Constructeur.
	 * @param dtList Liste à encapsuler
	 */
	public UiListUnmodifiable(final DtList<D> dtList) {
		super(dtList.getDefinition());
		//-----
		this.dtList = dtList;
		if (dtList.size() < 1000) {
			initUiObjectByIdIndex();
		}
	}

	// ==========================================================================

	/** {@inheritDoc} */
	@Override
	protected DtList<D> obtainDtList() {
		return dtList;
	}

	/**
	 * Vérifie les UiObjects de la liste, met à jour les objets métiers et retourne la liste.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Liste métier valid�e.
	 */
	@Override
	public DtList<D> validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		check(validator, uiMessageStack);
		return flush();
	}

	/**
	 * @param validator
	 * @param action
	 * @param contextKey
	 */
	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param validator Validateur à utilisé
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	@Override
	public void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> uiObject : getUiObjectBuffer()) {
			uiObject.check(validator, uiMessageStack);
		}
	}

	/**
	 * @return met à jour les objets métiers et retourne la liste.
	 */
	@Override
	public DtList<D> flush() {
		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> dtoInput : getUiObjectBuffer()) {
			dtoInput.flush();
		}
		clearUiObjectBuffer(); //on purge le buffer
		return dtList;
	}

	/** {@inheritDoc} */
	@Override
	public int indexOf(final Object o) {
		if (o instanceof DtObject) {
			throw new UnsupportedOperationException("This list contains UiObject only. If objects are modifiable please use UiListModifiable instead of this one.");
		}
		return super.indexOf(o);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("uiList(" + dtList.size() + " element(s)");
		for (int i = 0; i < Math.min(dtList.size(), 50); i++) {
			sb.append("; ");
			sb.append(get(i));
		}
		sb.append(")");
		return sb.toString();
	}
}
