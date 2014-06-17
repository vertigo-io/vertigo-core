package io.vertigo.dynamo.search;

import io.vertigo.kernel.component.Manager;

/**
 * Gestionnaire des indexes de recherche.
 * 
 * @author dchallas
 */
public interface SearchManager extends Manager {
	/**
	 * @return Services de recherche.
	 */
	SearchServicesPlugin getSearchServices();
}
