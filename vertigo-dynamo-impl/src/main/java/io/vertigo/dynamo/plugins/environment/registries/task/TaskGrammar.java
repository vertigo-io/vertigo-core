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

import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * @author pchretien
 */
public final class TaskGrammar {
	public static final String TASK_ATTRIBUTE = "attribute";

	/**Définition d'un attribut de tache.*/
	public static final Entity TASK_ATTRIBUTE_DEFINITION_ENTITY;
	/**Définition de tache.*/
	public static final Entity TASK_DEFINITION_ENTITY;
	public static final Grammar GRAMMAR;

	static {
		TASK_ATTRIBUTE_DEFINITION_ENTITY = new EntityBuilder("Attribute")
				.addProperty(KspProperty.NOT_NULL, true)
				.addProperty(KspProperty.IN_OUT, true)
				.addAttribute("domain", DomainGrammar.DOMAIN_ENTITY, true)
				.build();

		TASK_DEFINITION_ENTITY = new EntityBuilder("Task")
				.addProperty(KspProperty.REQUEST, true)
				.addProperty(KspProperty.CLASS_NAME, true)
				.addAttributes(TASK_ATTRIBUTE, TASK_ATTRIBUTE_DEFINITION_ENTITY, false)
				.build();

		GRAMMAR = new Grammar(TASK_DEFINITION_ENTITY, TASK_ATTRIBUTE_DEFINITION_ENTITY);

	}
}
