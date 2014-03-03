package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;

/**
 * Handler qui permet de créer des définitions statiques à partir d'une définition dynamique.
 * @author pchretien
 * @version $Id: DynamicRegistry.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface DynamicRegistry {
	/**
	 * @return Grammaire
	 */
	Grammar getGrammar();

	/**
	 * Enregistrement d'une définition.
	 * @param definition Définition
	 */
	void onDefinition(DynamicDefinition definition);

	/**
	 * Ajout d'une définition.
	 * Utilisé pour créer des définitions à partir d'autres Definitions.
	 * Exemple : création des domaines à partir d'un DT.
	 * 
	 * @param xdefinition DynamicDefinition
	 * @param dynamicModelrepository DynamicModelRepository
	 */
	void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository);
}
