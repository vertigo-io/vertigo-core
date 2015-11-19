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
package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.app.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper.
 *
 * @author emangin
 */
public final class DomainUtil {

	/**
	 * Constructeur privé pour classe utilitaire.
	 */
	private DomainUtil() {
		//RAS
	}

	/**
	 * Construite le type java (sous forme de chaine de caractère) correspondant
	 * à un Domaine.
	 * @param domain DtDomain
	 * @return String
	 */
	public static String buildJavaType(final Domain domain) {
		final DataType dataType = domain.getDataType();
		if (dataType.isPrimitive()) {
			String javaType = dataType.getJavaClass().getName();

			//On simplifie l'écriture des types primitifs
			//java.lang.String => String
			if (javaType.startsWith("java.lang.")) {
				javaType = javaType.substring("java.lang.".length());
			}
			return javaType;
		}

		//Cas des DTO et DTC
		/* Il existe deux cas :
		 *  - Soit le domaine correspond à un objet précis (DT)
		 *  - Soit le domaine est un dTO ou DTC générique.
		 */
		final String dtoClassCanonicalName;
		if (domain.hasDtDefinition()) {
			//on récupère le DT correspondant au nom passé en paramètre
			final DtDefinition dtDefinition = domain.getDtDefinition();
			dtoClassCanonicalName = dtDefinition.getClassCanonicalName();
		} else {
			dtoClassCanonicalName = io.vertigo.dynamo.domain.model.DtObject.class.getCanonicalName();
		}
		switch (dataType) {
			case DtObject:
				return dtoClassCanonicalName;
			case DtList:
				return io.vertigo.dynamo.domain.model.DtList.class.getCanonicalName() + '<' + dtoClassCanonicalName + '>';
			case BigDecimal:
			case Boolean:
			case DataStream:
			case Date:
			case Double:
			case Integer:
			case Long:
			case String:
				throw new IllegalArgumentException("Type unsupported : " + dataType);
			default:
				throw new IllegalArgumentException("Type unknown : " + dataType);
		}
	}

	static Collection<DtDefinition> getDtDefinitions() {
		return sortDefinitionCollection(Home.getApp().getDefinitionSpace().getAll(DtDefinition.class));
	}

	static Map<String, Collection<DtDefinition>> getDtDefinitionCollectionMap() {
		return getDefinitionCollectionMap(getDtDefinitions());
	}

	static Collection<AssociationSimpleDefinition> getSimpleAssociations() {
		return sortAssociationsCollection(Home.getApp().getDefinitionSpace().getAll(AssociationSimpleDefinition.class));
	}

	static Collection<AssociationNNDefinition> getNNAssociations() {
		return sortAssociationsCollection(Home.getApp().getDefinitionSpace().getAll(AssociationNNDefinition.class));
	}

	/**
	 * trie de la collection.
	 * @param definitionCollection collection à trier
	 * @return collection triée
	 */
	static Collection<DtDefinition> sortDefinitionCollection(final Collection<DtDefinition> definitionCollection) {
		final List<DtDefinition> list = new ArrayList<>(definitionCollection);
		java.util.Collections.sort(list, new DefinitionComparator<DtDefinition>());
		return list;
	}

	/**
	 * @param definitionCollection collection à traiter
	 * @return map ayant le package name en clef
	 */
	private static Map<String, Collection<DtDefinition>> getDefinitionCollectionMap(final Collection<DtDefinition> definitionCollection) {
		final Map<String, Collection<DtDefinition>> map = new LinkedHashMap<>();

		for (final DtDefinition definition : definitionCollection) {
			Collection<DtDefinition> dtDefinitions = map.get(definition.getPackageName());
			if (dtDefinitions == null) {
				dtDefinitions = new ArrayList<>();
				map.put(definition.getPackageName(), dtDefinitions);
			}
			dtDefinitions.add(definition);
		}
		return map;
	}

	private static <A extends AssociationDefinition> Collection<A> sortAssociationsCollection(final Collection<A> associationCollection) {
		final List<A> list = new ArrayList<>(associationCollection);
		java.util.Collections.sort(list, new DefinitionComparator<AssociationDefinition>());
		return list;
	}

	private static final class DefinitionComparator<D extends Definition> implements Comparator<D>, Serializable {
		private static final long serialVersionUID = 2382100262536812147L;

		DefinitionComparator() {
			//rien
		}

		/** {@inheritDoc} */
		@Override
		public int compare(final D definition1, final D definition2) {
			return definition1.getName().compareTo(definition2.getName());
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object obj) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}
	}
}
