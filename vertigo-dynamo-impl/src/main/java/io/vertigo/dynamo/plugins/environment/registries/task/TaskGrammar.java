/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.environment.registries.task;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * @author pchretien
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
