package io.vertigo.dynamo.plugins.environment.registries.facet;

import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.kernel.Home;

/**
 * @author pchretien
 * @version $Id: FacetDynamicRegistryPlugin.java,v 1.2 2013/10/22 12:34:28 pchretien Exp $
 */
public final class FacetDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin<FacetGrammar> {
	/**
	 * Constructeur.
	 */
	public FacetDynamicRegistryPlugin() {
		super(new FacetGrammar());
		Home.getDefinitionSpace().register(FacetDefinition.class);
		Home.getDefinitionSpace().register(FacetedQueryDefinition.class);
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
