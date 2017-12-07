/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.search.elasticsearch_2_4;

import java.util.Locale;
import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.lang.Assertion;

final class IndexType {
	private static final String INDEX_STORED = "stored";
	private static final String INDEX_NOT_STORED = "notStored";

	private final Optional<String> indexAnalyzer;
	private final String indexDataType;
	private final boolean indexStored;

	// par convention l'indexType du domain => l'analyzer de l'index
	// L'indexType peut-être compléter pour préciser le type si différente de string avec le séparateur :

	static IndexType readIndexType(final Domain domain) {
		final String indexType = domain.getProperties().getValue(DtProperty.INDEX_TYPE);
		if (indexType == null) {
			return new IndexType(null, domain);
		}
		return new IndexType(indexType, domain);
	}

	private static String obtainDefaultIndexDataType(final Domain domain) {
		// On peut préciser pour chaque domaine le type d'indexation
		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean:
			case Double:
			case Integer:
			case Long:
				return domain.getDataType().name().toLowerCase(Locale.ROOT);
			case String:
				return "text";
			case Date:
			case LocalDate:
			case Instant:
				return "date";
			case BigDecimal:
				return "double";
			case DataStream:
			default:
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");
		}
	}

	private IndexType(final String indexType, final Domain domain) {
		Assertion.checkNotNull(domain);
		//-----
		checkIndexType(indexType, domain);
		if (indexType == null) {
			//si pas d'indexType on précise juste le dataType pour rester triable
			indexAnalyzer = Optional.empty();
			indexDataType = obtainDefaultIndexDataType(domain);
			indexStored = true;
		} else {
			// par convention l'indexType du domain => l'analyzer de l'index
			// L'indexType peut-être compléter pour préciser le type si différente de string avec le séparateur :
			final String[] indexTypeArray = indexType.split(":", 3);
			indexAnalyzer = Optional.ofNullable(!indexTypeArray[0].isEmpty() ? indexTypeArray[0] : null); //le premier est toujours l'analyzer
			//le deuxième est optionnel et soit indexDataType, soit le indexStored
			final String defaultIndexType = domain.getDataType().name().toLowerCase(Locale.ENGLISH);
			final String secondParam = indexTypeArray.length >= 2 ? indexTypeArray[1] : defaultIndexType;
			if (INDEX_STORED.equals(secondParam) || INDEX_NOT_STORED.equals(secondParam)) {
				indexDataType = "string";
				indexStored = INDEX_STORED.equals(secondParam);
			} else {
				indexDataType = secondParam;
				//le troisième est optionnel et est le indexStored
				final String thirdParam = indexTypeArray.length == 3 ? indexTypeArray[2] : INDEX_STORED;
				Assertion.checkArgument(thirdParam.equals(INDEX_STORED) || thirdParam.equals(INDEX_NOT_STORED),
						"indexType ({0}) should respect this usage : indexType : \"myAnalyzer{:myDataType}{:stored|notStored\"}", indexType);
				indexStored = INDEX_STORED.equals(thirdParam);
			}
		}
	}

	private static void checkIndexType(final String indexType, final Domain domain) {
		// On peut préciser pour chaque domaine le type d'indexation
		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean:
			case Date:
			case LocalDate:
			case Instant:
			case Double:
			case Integer:
			case Long:
			case BigDecimal:
				// All these types are native
				break;
			case String:
				if (indexType == null) {
					throw new IllegalArgumentException("Précisez la valeur \"indexType\" dans le domain [" + domain + "].");
				}
				break;
			case DataStream:
			default:
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");
		}
	}

	public Optional<String> getIndexAnalyzer() {
		return indexAnalyzer;
	}

	public String getIndexDataType() {
		return indexDataType;
	}

	public boolean isIndexStored() {
		return indexStored;
	}
}
