package io.vertigo.dynamo.search;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * Class de résolution du nom des champs de l'index, à partir du nom du DtIndex.
 * @author npiedeloup
 * @version $Id: IndexFieldNameResolver.java,v 1.4 2014/01/20 17:48:56 pchretien Exp $
 */
public final class IndexFieldNameResolver {

	private final Map<String, String> indexFieldMap;
	private final Map<String, String> replaceIndexFieldMap;

	/**
	 * Constructeur.
	 * @param indexFieldMap Map des noms de champs du DtIndex vers le nom dans l'index
	 */
	public IndexFieldNameResolver(final Map<String, String> indexFieldMap) {
		Assertion.checkNotNull(indexFieldMap);
		//---------------------------------------------------------------------
		this.indexFieldMap = new HashMap<>(indexFieldMap);
		replaceIndexFieldMap = new HashMap<>(indexFieldMap.size());
		for (final Map.Entry<String, String> entry : indexFieldMap.entrySet()) {
			if (!entry.getKey().equals(entry.getValue())) {
				final String regex = "([^\\w])" + entry.getKey() + ":";
				replaceIndexFieldMap.put(regex, "$1" + entry.getValue() + ":");
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
