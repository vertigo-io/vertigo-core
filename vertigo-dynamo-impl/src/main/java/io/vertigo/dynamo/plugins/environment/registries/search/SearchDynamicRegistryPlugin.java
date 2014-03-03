package io.vertigo.dynamo.plugins.environment.registries.search;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.kernel.Home;

import javax.inject.Inject;

/**
 * @author pchretien
 * @version $Id: SearchDynamicRegistryPlugin.java,v 1.6 2014/02/03 17:29:01 pchretien Exp $
 */
public final class SearchDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin<SearchGrammar> {
	@Inject
	public SearchDynamicRegistryPlugin() {
		super(new SearchGrammar());
		Home.getDefinitionSpace().register(IndexDefinition.class);
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		if (getGrammarProvider().indexDefinition.equals(xdefinition.getEntity())) {
			// Seuls les controllers sont gérés.
			final IndexDefinition indexDefinition = createIndexDefinition(xdefinition);
			Home.getDefinitionSpace().put(indexDefinition, IndexDefinition.class);
		}
	}

	private static IndexDefinition createIndexDefinition(final DynamicDefinition xsearchObjet) {
		final DtDefinition indexDtDefinition = Home.getDefinitionSpace().resolve(xsearchObjet.getDefinitionKey("dtIndex").getName(), DtDefinition.class);
		final DtDefinition resultDtDefinition = Home.getDefinitionSpace().resolve(xsearchObjet.getDefinitionKey("dtResult").getName(), DtDefinition.class);
		//	final List<FacetDefinition> facetDefinitionList = Collections.emptyList();
		final String definitionName = xsearchObjet.getDefinitionKey().getName();

		final IndexDefinition indexDefinition = new IndexDefinition(definitionName, indexDtDefinition, resultDtDefinition);
		//indexDefinition.makeUnmodifiable();
		return indexDefinition;
	}
}
