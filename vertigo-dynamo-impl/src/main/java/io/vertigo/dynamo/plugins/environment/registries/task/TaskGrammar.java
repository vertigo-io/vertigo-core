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

import static io.vertigo.core.dsl.entity.EntityPropertyType.Boolean;
import static io.vertigo.core.dsl.entity.EntityPropertyType.String;
import static io.vertigo.dynamo.plugins.environment.KspProperty.CLASS_NAME;
import static io.vertigo.dynamo.plugins.environment.KspProperty.IN_OUT;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NOT_NULL;
import static io.vertigo.dynamo.plugins.environment.KspProperty.REQUEST;
import io.vertigo.core.dsl.entity.Entity;
import io.vertigo.core.dsl.entity.EntityBuilder;
import io.vertigo.core.dsl.entity.EntityGrammar;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * @author pchretien
 */
final class TaskGrammar {
	/** Attribute name. */
	public static final String TASK_ATTRIBUTE = "attribute";

	/**Définition d'un attribut de tache.*/
	private static final Entity TASK_ATTRIBUTE_DEFINITION_ENTITY;
	/**Définition de tache.*/
	public static final Entity TASK_DEFINITION_ENTITY;
	/** Task Grammar instance. */
	public static final EntityGrammar GRAMMAR;

	static {
		TASK_ATTRIBUTE_DEFINITION_ENTITY = new EntityBuilder("Attribute")
				.addField(NOT_NULL, Boolean, true)
				.addField(IN_OUT, String, true)
				.addField("domain", DomainGrammar.DOMAIN_ENTITY, true)
				.build();

		TASK_DEFINITION_ENTITY = new EntityBuilder("Task")
				.addField(REQUEST, String, true)
				.addField(CLASS_NAME, String, true)
				.addFields(TASK_ATTRIBUTE, TASK_ATTRIBUTE_DEFINITION_ENTITY, false)
				.build();

		GRAMMAR = new EntityGrammar(TASK_DEFINITION_ENTITY, TASK_ATTRIBUTE_DEFINITION_ENTITY);
	}

	private TaskGrammar() {
		//private
	}
}
