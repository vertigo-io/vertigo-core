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
package io.vertigo.core.definition.dsl.entity;

import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Une grammaire est composée d'entités et de propriétés.
 * Les entités sont une composition d'entités et de propriétés.
 *
 * Il est possible de composer une grammaire à partir de grammaires.
 *
 * @author pchretien
 */
public final class EntityGrammar {
	private final List<Entity> entities;

	/**
	 * Ajout d'une grammaire.
	 */
	public EntityGrammar(final Entity... entities) {
		this(Arrays.asList(entities));
	}

	/**
	 * Ajout d'une grammaire.
	 */
	public EntityGrammar(final List<Entity> entities) {
		Assertion.checkNotNull(entities);
		//-----
		this.entities = Collections.unmodifiableList(new ArrayList<>(entities));
	}

	/**
	 * @return Liste des entités.
	 */
	public List<Entity> getEntities() {
		return entities;
	}
}
