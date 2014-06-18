package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Critère de recherche par champs.
 * Les champs de l'objet recherché sont filtrés par la valeur associée au champ.
 * - Soit de type égalité.
 * - Soit de type préfixe (Commence par).
 *
 * @author npiedeloup
 * @param <D> Type de l'objet
 */
public final class FilterCriteriaBuilder<D extends DtObject> implements Builder<FilterCriteria<D>> {
	private final Map<String, Object> mapFilter = new HashMap<>();
	private final Map<String, String> mapPrefix = new HashMap<>();

	/**
	 * Ajout un critère de filtre.
	 * Filtre en égalité stricte.
	 * @param fieldName Nom du champs à filtrer
	 * @param value Valeur du champs.
	 * @return Builder
	 */
	public FilterCriteriaBuilder<D> withFilter(final String fieldName, final Object value) {
		Assertion.checkNotNull(value);
		check(fieldName);
		//----------------------------------------------------------------------
		mapFilter.put(fieldName, value);
		return this;
	}

	/**
	 * Ajout un critère de type préfixe.
	 * Filtre "commence par" le préfixe.
	 * @param fieldName Nom du champs à filtrer.
	 * @param prefix Préfix du champs.
	 * @return Builder
	 */
	public FilterCriteriaBuilder<D> withPrefix(final String fieldName, final String prefix) {
		Assertion.checkNotNull(prefix);
		check(fieldName);
		//----------------------------------------------------------------------
		mapPrefix.put(fieldName, prefix);
		return this;
	}

	private void check(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkArgument(!mapFilter.containsKey(fieldName), "Ce champs est déjà filtré {0}", fieldName);
		Assertion.checkArgument(!mapPrefix.containsKey(fieldName), "Ce champs est déjà préfixé {0}", fieldName);
	}

	/** {@inheritDoc} */
	public FilterCriteria<D> build() {
		return new FilterCriteria<>(mapFilter, mapPrefix);
	}

}
