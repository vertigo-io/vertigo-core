package io.vertigo.dynamo.plugins.environment.registries.facet;

import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.kernel.Home;

/**
 * @author pchretien
 */
public final class FacetDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin<FacetGrammar> {
	/**
	 * Constructeur.
	 */
	public FacetDynamicRegistryPlugin() {
		super(new FacetGrammar());
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		//		if (getGrammar().fileInfoDefinition.equals(xdefinition.getEntity())) {
		//			//Seuls les taches sont gérées.
		//			final FileInfoDefinition definition = createFileDefinition(xdefinition);
		//			Home.getNameSpace().registerDefinition(definition, FileInfoDefinition.class);
		//		}
	}
}
