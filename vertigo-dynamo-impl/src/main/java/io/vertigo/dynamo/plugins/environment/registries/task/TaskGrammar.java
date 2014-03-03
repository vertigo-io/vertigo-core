package io.vertigo.dynamo.plugins.environment.registries.task;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * @author pchretien
 * @version $Id: TaskGrammar.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
final class TaskGrammar extends GrammarProvider {
	static final String TASK_ATTRIBUTE = "attribute";

	/**Définition de tache.*/
	final Entity taskDefinition;
	/**Définition d'un attribut de tache.*/
	final Entity attributeDefinition;

	/**
	 * Constructeur.
	 * Initialisation des métadonnées permettant de décrire le métamodèle de Dynamo.
	 */
	TaskGrammar() {
		attributeDefinition = createAttributeEntity(DomainGrammar.INSTANCE.getDomainEntity());
		taskDefinition = createTaskDefinitionEntity(attributeDefinition);
		//---------------------------------------------------------------------
		getGrammar().registerEntity(taskDefinition);
		getGrammar().registerEntity(attributeDefinition);
	}

	private static Entity createAttributeEntity(final Entity domainEntity) {
		return new EntityBuilder("Attribute")//
				.withProperty(KspProperty.NOT_NULL, true)//
				.withProperty(KspProperty.IN_OUT, true)//
				.withAttribute("domain", domainEntity, false, true)//
				.build();
	}

	private static Entity createTaskDefinitionEntity(final Entity attributeEntity) {
		return new EntityBuilder("Task")//
				.withProperty(KspProperty.REQUEST, true)//
				.withProperty(KspProperty.CLASS_NAME, true)//
				.withAttribute(TASK_ATTRIBUTE, attributeEntity, true, false)//
				.build();
	}
}
