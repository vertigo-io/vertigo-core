package io.vertigo.dynamo.collections.facet;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamock.facet.CarFacetInitializer;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.ComponentInitializer;

/**
 * Configuration du manager Search.
 * @author dchallas
 */
public final class CollectionsManagerInitializer implements ComponentInitializer<CollectionsManager> {
	/** {@inheritDoc} */
	public void init(final CollectionsManager collectionsManager) {
		//todo A déplacer
		Home.getDefinitionSpace().register(FacetedQueryDefinition.class);
		Home.getDefinitionSpace().register(FacetDefinition.class);

		CarFacetInitializer.initCarFacet();
	}
}
