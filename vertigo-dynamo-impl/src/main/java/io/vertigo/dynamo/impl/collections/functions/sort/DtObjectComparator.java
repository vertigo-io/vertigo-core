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
package io.vertigo.dynamo.impl.collections.functions.sort;

import io.vertigo.dynamo.domain.metamodel.DataAccessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.persistence.datastore.Broker;
import io.vertigo.lang.Assertion;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Comparateur des DtObject.
 * S'appuie sur SortState.
 * Si la colonne est un type primitif alors on effectue le tri sur ce type.
 * Si la colonne est une URI on délégue à l'URI.
 * @param <D> Type de l'objet
 *
 * @author pchretien
 */
final class DtObjectComparator<D extends DtObject> implements Comparator<D> {
	//On ne veut pas d'un comparateur sérializable !!!
	/**
	 * Comparateur du tri
	 */
	private final Comparator<Object> comparator;

	/**
	 * Field du tri
	 */
	private final DtField sortField;

	/**
	 * Constructeur.
	 * @param persistenceManager Manager de persistence
	 * @param dtDefinition DtDefinition des éléments à comparer
	 * @param sortState Etat du tri
	 */
	DtObjectComparator(final PersistenceManager persistenceManager, final DtDefinition dtDefinition, final SortState sortState) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(sortState);
		//-----
		//On recherche le comparateur associé au champ de la collection
		//Si il n'y a pas de comparateur alors on applique la comparaison standard.
		this.sortField = dtDefinition.getField(sortState.getFieldName());

		//On regarde si on est sur une ForeignKey et sur une MasterDataList
		if (sortField.getType() == FieldType.FOREIGN_KEY && persistenceManager.getMasterDataConfiguration().containsMasterData(sortField.getFkDtDefinition())) {
			//Il existe une Liste de référence associée
			//Dans le cas des liste de référence on délégue la comparaison
			final DtListURIForMasterData mdlUri = persistenceManager.getMasterDataConfiguration().getDtListURIForMasterData(sortField.getFkDtDefinition());
			this.comparator = createMasterDataComparator(sortState, persistenceManager, mdlUri, sortState);
		} else {
			//Cas par défaut
			this.comparator = createDefaultComparator(sortState);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final D dto1, final D dto2) {
		Assertion.checkNotNull(dto1);
		Assertion.checkNotNull(dto2);
		//Les DTC ne contiennent pas d'éléments null.
		//-----
		final DataAccessor dataAccessor = sortField.getDataAccessor();
		return comparator.compare(dataAccessor.getValue(dto1), dataAccessor.getValue(dto2));
	}

	/*
	 * Visibilité package car utilisé au sein d'une classe interne.
	 */
	static int compareValues(final SortState sortState, final Object fieldValue1, final Object fieldValue2) {
		final int result;
		if (fieldValue1 == null && fieldValue2 == null) {
			return 0;
		}

		if (fieldValue1 == null) {
			return sortState.isNullLast() ? 1 : -1;
		}

		if (fieldValue2 == null) {
			return sortState.isNullLast() ? -1 : 1;
		}

		//Objet1 et Objet2 sont désormais non null.
		if (sortState.isIgnoreCase() && fieldValue1 instanceof String) {
			// pour ignorer les accents
			final Collator compareOperator = Collator.getInstance(Locale.FRENCH);
			compareOperator.setStrength(Collator.PRIMARY);
			result = compareOperator.compare((String) fieldValue1, (String) fieldValue2);
		} else if (fieldValue1 instanceof Comparable<?>) {
			result = ((Comparable) fieldValue1).compareTo(fieldValue2);
		} else {
			result = fieldValue1.toString().compareTo(fieldValue2.toString());
		}
		return sortState.isDesc() ? -result : result;
	}

	/**
	 * Création d'un comparateur à partir de l'état de tri précisé.
	 * Il s'agit du comparateur par défaut.
	 * @return Comparator
	 */
	private static Comparator<Object> createDefaultComparator(final SortState sortState) {
		return new Comparator<Object>() {
			/** {@inheritDoc} */
			@Override
			public int compare(final Object v1, final Object v2) {
				return compareValues(sortState, v1, v2);
			}

		};
	}

	/**
	 * Fournit le comparateur à utiliser pour trier une colonne référenéant une MasterDataList.
	 * @param sortStateParam Etat du tri
	 * @return Comparator à utiliser pour trier la colonne.
	 */
	private static Comparator<Object> createMasterDataComparator(final SortState sortState, final PersistenceManager persistenceManager, final DtListURIForMasterData dtcURIForMasterData, final SortState sortStateParam) {
		Assertion.checkNotNull(persistenceManager);
		Assertion.checkNotNull(dtcURIForMasterData);
		Assertion.checkNotNull(sortStateParam);
		//-----
		final Broker broker = persistenceManager.getBroker();
		//		final Store store = getPhysicalStore(masterDataDefinition.getDtDefinition());
		final DtField mdFieldSort = dtcURIForMasterData.getDtDefinition().getSortField().get();
		return new Comparator<Object>() {
			private Object getSortValue(final Object o) {
				final URI<DtObject> uri = new URI(dtcURIForMasterData.getDtDefinition(), o);
				DtObject dto;
				try {
					dto = broker.get(uri);
				} catch (final Exception e) {
					//Il ne peut pas y avoir d'exception typée dans un comparateur.
					throw new RuntimeException(e);
				}
				return mdFieldSort.getDataAccessor().getValue(dto);
			}

			@Override
			public int compare(final Object o1, final Object o2) {
				if (o1 != null && o2 != null) {
					final Object lib1 = getSortValue(o1);
					final Object lib2 = getSortValue(o2);
					return compareValues(sortState, lib1, lib2);
				}
				return compareValues(sortState, o1, o2);//si l'un des deux est null on retombe sur la comparaison standard
			}
		};
	}
}
