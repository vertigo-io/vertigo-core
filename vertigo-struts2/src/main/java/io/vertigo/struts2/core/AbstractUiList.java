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

import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.StringUtil;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
public abstract class AbstractUiList<D extends DtObject> extends AbstractList<UiObject<D>> implements Serializable {
	private static final long serialVersionUID = 5475819598230056558L;

	private static final int NB_MAX_ELEMENTS = 1000; //Max nb elements in list. Must be kept under 1000 to ensure good performances.

	/**
	 * Accès au storeManager.
	 */
	protected final ComponentRef<StoreManager> storeManager = ComponentRef.makeLazyRef(StoreManager.class);
	/**
	 * Accès au transactionManager.
	 */
	protected final ComponentRef<VTransactionManager> transactionManager = ComponentRef.makeLazyRef(VTransactionManager.class);

	private final Map<Integer, UiObject<D>> uiObjectByIndex = new HashMap<>();
	private final Map<String, Map<String, UiObject<D>>> uiObjectByFieldValue = new HashMap<>();

	//==========================================================================
	private final DefinitionReference<DtDefinition> dtDefinitionRef;
	private final String camelIdFieldName; //nullable (Option n'est pas serializable)

	/**
	 * Constructeur.
	 * @param dtDefinition DtDefinition
	 */
	AbstractUiList(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		dtDefinitionRef = new DefinitionReference<>(dtDefinition);
		final Option<DtField> dtIdField = getDtDefinition().getIdField();
		if (dtIdField.isDefined()) {
			camelIdFieldName = StringUtil.constToLowerCamelCase(dtIdField.get().name());
		} else {
			camelIdFieldName = null;
		}
	}

	/**
	 * Initialize l'index des UiObjects par Id.
	 * Attention : nécessite la DtList (appel obtainDtList).
	 */
	protected final void initUiObjectByIdIndex() {
		if (camelIdFieldName != null) {
			initUiObjectByKeyIndex(camelIdFieldName);
		}
	}

	/**
	 * Initialize l'index des UiObjects par Clé.
	 * Attention : nécessite la DtList (appel obtainDtList).
	 * @param keyFieldName Nom du champs à indexer
	 */
	public final void initUiObjectByKeyIndex(final String keyFieldName) {
		final Map<String, UiObject<D>> uiObjectById = obtainUiObjectByIdMap(keyFieldName);
		for (final UiObject<D> uiObject : this) {
			uiObjectById.put((String) uiObject.get(keyFieldName), uiObject);
		}
	}

	/**
	 * Récupère la liste des elements.
	 * Peut-être appelé souvant : doit assurer un cache local (transient au besoin) si chargement.
	 * @return Liste des éléments
	 */
	protected abstract DtList<D> obtainDtList();

	/**
	 * @return DtDefinition de l'objet métier
	 */
	public final DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public final UiObject<D> get(final int index) {
		UiObject<D> element = uiObjectByIndex.get(index);
		if (element == null) {
			element = new UiObject<>(obtainDtList().get(index));
			uiObjectByIndex.put(index, element);
			Assertion.checkState(uiObjectByIndex.size() < 1000, "Trop d'élément dans le buffer uiObjectByIndex de la liste de {0}", getDtDefinition().getName());
		}
		return element;
	}

	/** {@inheritDoc} */
	@Override
	public final int size() {
		return obtainDtList().size();
	}

	/** {@inheritDoc} */
	@Override
	public int indexOf(final Object o) {
		if (o instanceof DtObject) {
			return indexOf((DtObject) o);
		} else if (o instanceof UiObject) {
			return indexOf((UiObject<D>) o);
		}
		return super.indexOf(o);
	}

	/**
	 * @param UiObject UiObject recherché
	 * @return index de l'objet dans la liste
	 */
	private int indexOf(final UiObject<D> UiObject) {
		Assertion.checkNotNull(UiObject);
		//-----
		return obtainDtList().indexOf(UiObject.getInnerObject());
	}

	/**
	 * @param dtObject DtObject recherché
	 * @return index de l'objet dans la liste
	 */
	private int indexOf(final DtObject dtObject) {
		Assertion.checkNotNull(dtObject);
		//-----
		return obtainDtList().indexOf(dtObject);
	}

	/**
	 * Récupère un objet par la valeur de son identifiant.
	 * Utilisé par les select, radio et autocomplete en mode ReadOnly.
	 * @param keyFieldName Nom du champ identifiant
	 * @param keyValueAsString Valeur de l'identifiant
	 * @return UiObject
	 * @throws FormatterException Format error
	 */
	public UiObject<D> getById(final String keyFieldName, final String keyValueAsString) throws FormatterException {
		final Map<String, UiObject<D>> uiObjectById = obtainUiObjectByIdMap(keyFieldName);
		UiObject<D> uiObject = uiObjectById.get(keyValueAsString);
		if (uiObject == null) {
			final DtField dtField = getDtDefinition().getField(StringUtil.camelToConstCase(keyFieldName));
			Assertion.checkArgument(dtField.getType() == FieldType.PRIMARY_KEY, "La clé {0} de la liste doit être la PK", keyFieldName);

			final Object key = dtField.getDomain().getFormatter().stringToValue(keyValueAsString, dtField.getDomain().getDataType());
			final D dto = loadDto(key);
			uiObject = new UiObject<>(dto);
			uiObjectById.put(keyValueAsString, uiObject);
			Assertion.checkState(uiObjectById.size() < NB_MAX_ELEMENTS, "Trop d'élément dans le buffer uiObjectById de la liste de {0}", getDtDefinition().getName());
		}
		return uiObject;
	}

	private D loadDto(final Object key) {
		//-- Transaction BEGIN
		try (final VTransactionWritable transaction = transactionManager.get().createCurrentTransaction()) {
			return storeManager.get().getDataStore().<D> get(new URI<D>(getDtDefinition(), key));
		}
	}

	/**
	 * Récupère l'index des UiObjects par Id.
	 * @param keyFieldName Nom du champ identifiant
	 * @return Index des UiObjects par Id
	 */
	protected final Map<String, UiObject<D>> obtainUiObjectByIdMap(final String keyFieldName) {
		Map<String, UiObject<D>> uiObjectById = uiObjectByFieldValue.get(keyFieldName);
		if (uiObjectById == null) {
			uiObjectById = new HashMap<>();
			uiObjectByFieldValue.put(keyFieldName, uiObjectById);
		}
		return uiObjectById;
	}

	/**
	 * @return Liste des uiObjects bufferisés (potentiellement modifiés).
	 */
	protected final Collection<UiObject<D>> getUiObjectBuffer() {
		return uiObjectByIndex.values();
	}

	/**
	 * Vide le buffer des UiObjects (potentiellement modifiés).
	 */
	protected final void clearUiObjectBuffer() {
		uiObjectByIndex.clear();
	}

}
