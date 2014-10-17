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
package io.vertigo.dynamo.search;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * Class de résolution du nom des champs de l'index, à partir du nom du DtIndex.
 * @author npiedeloup
 */
public final class IndexFieldNameResolver {

	private final Map<String, String> indexFieldMap;
	private final Map<String, String> dtFieldMap;
	private final Map<String, String> replaceIndexFieldMap;

	/**
	 * Constructeur.
	 * @param indexFieldMap Map des noms de champs du DtIndex vers le nom dans l'index
	 */
	public IndexFieldNameResolver(final Map<String, String> indexFieldMap) {
		Assertion.checkNotNull(indexFieldMap);
		//---------------------------------------------------------------------
		this.indexFieldMap = new HashMap<>(indexFieldMap);
		dtFieldMap = new HashMap<>(indexFieldMap.size());
		replaceIndexFieldMap = new HashMap<>(indexFieldMap.size());
		for (final Map.Entry<String, String> entry : indexFieldMap.entrySet()) {
			if (!entry.getKey().equals(entry.getValue())) {
				final String regex = "([^\\w])" + entry.getKey() + ":";
				replaceIndexFieldMap.put(regex, "$1" + entry.getValue() + ":");
				dtFieldMap.put(entry.getValue(), entry.getKey());
			}
		}
	}

	/**
	 * Retourne le nom du champs de l'index à partir du champs du Dt de l'index.
	 * @param dtField Champs du DT index
	 * @return Nom du champ de l'index
	 */
	public String obtainIndexFieldName(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//---------------------------------------------------------------------
		final String fieldName = dtField.getName();
		final String indexFieldName = indexFieldMap.get(fieldName);
		return indexFieldName != null ? indexFieldName : fieldName;
	}

	/**
	 * Retourne le nom du champs du dt à partir du champs de l'index.
	 * @param Nom du champ de l'index
	 * @return Champs du DT index
	 */
	public String obtainDtFieldName(final String indexField) {
		Assertion.checkArgNotEmpty(indexField);
		//---------------------------------------------------------------------
		final String dtFieldName = dtFieldMap.get(indexField);
		return dtFieldName != null ? dtFieldName : indexField;
	}

	/**
	 * Remplace les noms des champs de la requete (nom du DtIndex) par les noms utilisés dans l'index.
	 * @param stringQuery Chaine de recherche
	 * @return Nouvelle requete transformée
	 */
	public String replaceAllIndexFieldNames(final String stringQuery) {
		String result = stringQuery;
		for (final Map.Entry<String, String> entry : replaceIndexFieldMap.entrySet()) {
			result = result.replaceAll(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
