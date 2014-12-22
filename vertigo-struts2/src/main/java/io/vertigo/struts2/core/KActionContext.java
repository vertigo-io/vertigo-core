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
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Liste des couples (clé, object) enregistrés.
 * @author npiedeloup
 */
public final class KActionContext extends HashMap<String, Serializable> {
	/** Clée de l'id de context dans le context. */
	public static final String CTX = "CTX";
	private static final long serialVersionUID = 2850788652438173312L;

	//Index UiObject et DtObject vers clé de context.
	private final Map<Serializable, String> reverseUiObjectIndex = new HashMap<>();
	//Index UiList et DtList vers clé de context. //identity HashMap because two empty list aren't the same
	private final Map<UiList<?>, String> reverseUiListIndex = new IdentityHashMap<>();
	private boolean unmodifiable; //initialisé à false

	/**
	 * Constructeur.
	 */
	public KActionContext() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final Object key) {
		Assertion.checkNotNull(key);
		//-----
		final Serializable o = super.get(key);
		Assertion.checkNotNull(o, "Objet :{0} non trouvé! Vérifier que l objet est bien enregistré avec la clé. Cl�s disponibles {1}", key, keySet());
		return o;
	}

	/**
	 * @param key Clé de context
	 * @return UiObject du context
	 */
	public <O extends DtObject> UiObject<O> getUiObject(final String key) {
		return (UiObject<O>) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return UiList du context
	 */
	public <O extends DtObject> UiList<O> getUiList(final String key) {
		return (UiList<O>) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return UiListModifiable du context
	 */
	public <O extends DtObject> UiListModifiable<O> getUiListModifiable(final String key) {
		return (UiListModifiable<O>) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return String du context
	 */
	public String getString(final String key) {
		final Object value = get(key);
		if (value instanceof String[] && ((String[]) value).length > 0) {
			//Struts set des String[] au lieu des String
			//on prend le premier
			return ((String[]) value)[0];
		}
		return (String) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return Long du context
	 */
	public Long getLong(final String key) {
		return (Long) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return Integer du context
	 */
	public Integer getInteger(final String key) {
		return (Integer) get(key);
	}

	/**
	 * @param key Clé de context
	 * @return Boolean du context
	 */
	public Boolean getBoolean(final String key) {
		return (Boolean) get(key);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final Object key) {
		Assertion.checkNotNull(key);
		//-----
		return super.containsKey(key);
	}

	/**
	 * @param uiObject UiObject recherché
	 * @return Clé de context de l'élément (null si non trouvé)
	 */
	public String findKey(final UiObject<?> uiObject) {
		Assertion.checkNotNull(uiObject);
		//-----
		final String contextKey = reverseUiObjectIndex.get(uiObject);
		if (contextKey != null) {
			return contextKey;
		}
		for (final Map.Entry<UiList<?>, String> entry : reverseUiListIndex.entrySet()) {
			final int index = entry.getKey().indexOf(uiObject);
			if (index >= 0) {
				return entry.getValue() + ".get(" + index + ")";
			}
		}
		return null;
	}

	/**
	 * @param dtObject DtObject recherché
	 * @return Clé de context de l'élément (null si non trouvé)
	 */
	public String findKey(final DtObject dtObject) {
		Assertion.checkNotNull(dtObject);
		//-----
		final String contextKey = reverseUiObjectIndex.get(dtObject);
		if (contextKey != null) {
			return contextKey;
		}
		for (final Map.Entry<UiList<?>, String> entry : reverseUiListIndex.entrySet()) {
			final int index = entry.getKey().indexOf(dtObject);
			if (index >= 0) {
				return entry.getValue() + ".get(" + index + ")";
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Serializable put(final String key, final Serializable value) {
		Assertion.checkState(!unmodifiable, "Ce context ({0}) a été fig� et n'est plus modifiable.", super.get(CTX));
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(value, "la valeur doit être renseignée pour {0}", key);
		Assertion.checkArgument(!(value instanceof DtObject), "Vous devez poser des uiObject dans le context pas des objets métiers ({0})", key);
		Assertion.checkArgument(!(value instanceof DtList), "Vous devez poser des uiList dans le context pas des listes d'objets métiers ({0})", key);
		//-----
		if (CTX.equals(key)) { //struts tente de mettre a jour la clé lors de la reception de la request
			return value;
		}
		if (value instanceof UiObject) {
			reverseUiObjectIndex.put(value, key);
			reverseUiObjectIndex.put(((UiObject<?>) value).getInnerObject(), key);
		} else if (value instanceof UiList) {
			reverseUiListIndex.put((UiList<?>) value, key);
		}

		return super.put(key, value);
	}

	/**
	 * @return Clé de ce context
	 */
	public String getId() {
		return getString(CTX);
	}

	/**
	 * G�n�re un nouvel Id et passe le context en modifiable.
	 */
	public void makeModifiable() {
		unmodifiable = false;
		super.remove(CTX);
	}

	/**
	 * passe le context en non-modifiable.
	 */
	public void makeUnmodifiable() {
		super.put(CTX, UUID.randomUUID().toString());
		unmodifiable = true;
	}
}
