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
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;

/**
 * Wrapper d'affichage des listes d'objets métier.
 * @author npiedeloup
 * @param <D> Type d'objet
 */
final class UiMdList<D extends DtObject> extends AbstractUiList<D> implements UiList<D> {
	private static final long serialVersionUID = 5475819598230056558L;

	private final DtListURI dtListUri;
	private transient DtList<D> lazyDtList;

	/**
	 * Constructeur.
	 *
	 * @param dtListUri Uri de la Liste à encapsuler
	 */
	public UiMdList(final DtListURI dtListUri) {
		super(dtListUri.getDtDefinition());
		Assertion.checkArgument(storeManager.get().getMasterDataConfig().containsMasterData(dtListUri.getDtDefinition()), "UiMdList can't be use with {0}, it's not a MasterDataList.", dtListUri.getDtDefinition().getName());
		// -------------------------------------------------------------------------
		this.dtListUri = dtListUri;
	}

	// ==========================================================================

	/**
	 * @return Liste des données
	 */
	@Override
	public DtList<D> obtainDtList() {
		if (lazyDtList == null) {
			try (final VTransactionWritable transaction = transactionManager.get().createCurrentTransaction()) {
				lazyDtList = storeManager.get().getDataStore().<D> getList(dtListUri);
			}
		}
		return lazyDtList;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "uiMdList(" + dtListUri.toString() + (lazyDtList != null ? ", loaded:" + lazyDtList.size() : "") + " )";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		//on surcharge equals pour eviter un appel à super.equals non d�sir� et qui forcerai le chargement de la liste
		return (o instanceof UiMdList) && dtListUri.equals(((UiMdList<?>) o).dtListUri);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		//on surcharge hashCode pour eviter un appel à super.hashCode non d�sir� et qui forcerai le chargement de la liste
		return dtListUri.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		return obtainDtList();
	}

	/** {@inheritDoc} */
	@Override
	public void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> flush() {
		return obtainDtList();
	}
}
