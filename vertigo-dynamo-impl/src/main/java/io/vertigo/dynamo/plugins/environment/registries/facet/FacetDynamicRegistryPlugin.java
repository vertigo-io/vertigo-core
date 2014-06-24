package io.vertigo.dynamo.plugins.environment.registries.facet;

import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;

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
