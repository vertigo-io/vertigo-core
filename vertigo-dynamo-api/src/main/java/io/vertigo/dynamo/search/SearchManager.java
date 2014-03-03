package io.vertigo.dynamo.search;

import io.vertigo.kernel.component.Manager;

/**
 * Gestionnaire des indexes de recherche.
 * 
 * @author dchallas
 * @version $Id: SearchManager.java,v 1.2 2013/10/22 10:59:40 pchretien Exp $
 */
public interface SearchManager extends Manager {
	/**
	 * @return Services de recherche.
	 */
	SearchServicesPlugin getSearchServices();
}
