package io.vertigo.dynamo.impl.search;

import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.SearchServicesPlugin;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import javax.inject.Inject;

/**
 * Implémentation standard du gestionnaire des indexes de recherche.
 * @author dchallas
 * @version $Id: SearchManagerImpl.java,v 1.4 2013/10/22 10:58:34 pchretien Exp $
 */
public final class SearchManagerImpl implements SearchManager {
	@Inject
	private Option<SearchServicesPlugin> searchServicesPlugin;

	/** {@inheritDoc} */
	public SearchServicesPlugin getSearchServices() {
		Assertion.checkArgument(searchServicesPlugin.isDefined(), "Aucun plugin de recherche déclaré");
		//---------------------------------------------------------------------
		return searchServicesPlugin.get();
	}
}
