package io.vertigo.dynamo.plugins.environment.registries.search;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * @author pchretien
 * @version $Id: SearchGrammar.java,v 1.3 2014/02/03 17:29:01 pchretien Exp $
 */
final class SearchGrammar extends GrammarProvider {
	final Entity indexDefinition;

	/**
	 * Initialisation des métadonnées permet tant de décrire le métamodèle de dynamo.
	 */
	SearchGrammar() {
		indexDefinition = createIndexDefinitionEntity(DomainGrammar.INSTANCE.getDtDefinitionEntity());
		//---------------------------------------------------------------------	
		getGrammar().registerEntity(indexDefinition);
	}

	private static Entity createIndexDefinitionEntity(final Entity dtDefinition) {
		return new EntityBuilder("IndexDefinition")//
				.withAttribute("dtIndex", dtDefinition, false, true)//
				.withAttribute("dtResult", dtDefinition, false, true)//s
				.build();
	}
}
