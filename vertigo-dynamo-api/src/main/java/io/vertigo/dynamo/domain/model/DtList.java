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
package io.vertigo.dynamo.domain.model;

import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classe de stockage des listes.
 * Une dtList est une liste constituée avec un seul type d'objet.
 * Les objets null ne sont pas autorisés.
 *
 * @author  fconstantin
 * @param <D> Type du DtObject
 */
public final class DtList<D extends DtObject> extends AbstractList<D> implements Serializable {
	private static final long serialVersionUID = -8059200549636099190L;

	private DtListURI uri;

	/** Reéférence vers la Définition. */
	private DefinitionReference<DtDefinition> dtDefinitionRef;

	/** List des dto contenus. */
	private final List<D> dtObjects = new ArrayList<>();

	/** List des dto contenus. */
	private final Map<String, Serializable> metaDatas = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 * @param dtDefinition Définition de DT
	 */
	public DtList(final DtDefinition dtDefinition) {
		this.dtDefinitionRef = new DefinitionReference<>(dtDefinition);
	}

	/**
	 * Constructeur.
	 * @param dtObjectClass Type d'objet
	 */
	public DtList(final Class<? extends DtObject> dtObjectClass) {
		this(DtObjectUtil.findDtDefinition(dtObjectClass));
	}

	/** {@inheritDoc} */
	@Override
	public D get(final int row) {
		return dtObjects.get(row);
	}

	/** {@inheritDoc} */
	@Override
	public D set(final int row, final D object) {
		//Implementation de set, pour que la collection soit modifiable 
		//Et donc pour que le Collections.sort(List<?> ) fonctionne
		return dtObjects.set(row, object);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return dtObjects.size();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("(def=").append(getDefinition()).append(", size=").append(dtObjects.size());
		if (dtObjects.size() > 50) {
			buf.append(" show only the 50 firsts");
		}
		buf.append(")\n");
		for (int i = 0; i < Math.min(dtObjects.size(), 50); i++) { //pas plus de 50 elements dans le toString
			buf.append("\tRow #").append(i).append(" : ");
			buf.append(get(i)).append('\n');
		}

		return buf.toString();
	}

	//==========================================================================

	/** {@inheritDoc} */
	@Override
	public boolean add(final D dto) {
		Assertion.checkNotNull(dto);
		final DtDefinition foundDtDefinition = DtObjectUtil.findDtDefinition(dto);
		Assertion.checkArgument(getDefinition().equals(foundDtDefinition), "Ne peut pas inserer un dto '{0}' dans une collection '{1}'", foundDtDefinition, getDefinition());
		//---------------------------------------------------------------------
		return dtObjects.add(dto);
	}

	/** {@inheritDoc} */
	@Override
	public D remove(final int row) {
		return dtObjects.remove(row);
	}

	/** {@inheritDoc} */
	@Override
	public List<D> subList(final int start, final int end) {
		throw new UnsupportedOperationException();
	}

	//==========================================================================
	//================================ Méthodes supplémentaires=================
	//==========================================================================

	/**
	 * @return Définition de la liste.
	 */
	public DtDefinition getDefinition() {
		return dtDefinitionRef.get();
	}

	/**
	 * @return URI de la ressource
	 */
	public DtListURI getURI() {
		/*
		 * if (uri == null) {
		 * uri = broker.Helper.createURI(this);
		 * }
		 */
		return uri;
	}

	/**
	 * Définit l'uri de la collection.
	 * @param newUri DtListURI
	 */
	public void setURI(final DtListURI newUri) {
		if (this.uri == null) {
			this.uri = newUri;
		} else {
			throw new IllegalAccessError("URI déjà fixée");
		}
	}

	//==========================================================================
	//================================ Metadatas management ====================
	//==========================================================================

	// There is no all MetaData with values getter. 
	// Developers should always knows which metadata they needs. It's intended use.
	//

	/**
	 * @return MetaData names (only not null ones)
	 */
	public Set<String> getMetaDataNames() {
		return Collections.unmodifiableSet(metaDatas.keySet());
	}

	/**
	 * @param metaDataName MetaData name
	 * @return if this metadata is known and not null
	 */
	public boolean containsMetaData(final String metaDataName) {
		return metaDatas.containsKey(metaDataName);
	}

	/**
	 * @param metaDataName MetaData name
	 * @param metaDataClass MetaData value class
	 * @param <O> MetaData value type
	 * @return MetaData value
	 */
	public <O extends Serializable> Option<O> getMetaData(final String metaDataName, final Class<O> metaDataClass) {
		Assertion.checkArgNotEmpty(metaDataName);
		//---------------------------------------------------------------------
		final Object value = metaDatas.get(metaDataName);
		if (value == null) {
			return Option.none();
		}
		return Option.some(metaDataClass.cast(value));
	}

	/**
	 * Set a metaData on this list. If value is null, the metadata is remove.
	 * <b>WARN</b>
	 * <b>Developers must ensure</b> this metaData keep coherent with current list datas, <b>all the time</b>.
	 * <b>WARN</b>
	 * @param metaDataName MetaData name
	 * @param value MetaData value
	 */
	public void setMetaData(final String metaDataName, final Serializable value) {
		Assertion.checkArgNotEmpty(metaDataName);
		//---------------------------------------------------------------------
		if (value == null) {
			metaDatas.remove(metaDataName);
		}
		metaDatas.put(metaDataName, value);
	}
}
