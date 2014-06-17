package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;

/**
 * Chainage de filtre de liste.
 * Les filtres sont evalués dans l'ordre, il est préférable de mettre les plus discriminant en premier.
 * @author pchretien
 * @param <D> Type d'objet
 */
public final class DtListChainFilter<D extends DtObject> implements DtListFilter<D>, Serializable {
	private static final long serialVersionUID = -81683701282488344L;
	private final DtListFilter<D>[] filters;

	/**
	 * Constructeur.
	 * @param filters Liste des filtres.
	 */
	public DtListChainFilter(final DtListFilter<D>... filters) {
		Assertion.checkNotNull(filters);
		Assertion.checkArgument(filters.length > 0, "Il faut au moins un filter");
		//-----------------------------------------------------------------
		this.filters = filters;
	}

	/** {@inheritDoc} */
	public boolean accept(final D dto) {
		for (final DtListFilter<D> filter : filters) {
			if (!filter.accept(dto)) {
				return false;
			}
		}
		return true;
	}
}
