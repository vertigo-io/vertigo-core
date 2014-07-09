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
package io.vertigo.persona.impl.environment.plugins.registries.security;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;

/**
 * @author pchretien
 * @version $Id: SecurityGrammar.java,v 1.3 2014/02/03 17:29:01 pchretien Exp $
 */
public final class SecurityGrammar extends GrammarProvider {
	public static final SecurityGrammar INSTANCE = new SecurityGrammar();
	/**D�finition d'un role.*/
	private final Entity roleEntity;

	/**
	 * Initialisation des metadonnees permettant de decrire le metamodele.
	 */
	private SecurityGrammar() {
		roleEntity = new EntityBuilder("Role").build();
		//---------------------------------------------------------------------
		getGrammar().registerEntity(roleEntity);
	}

	/**
	 * @return D�finition d'un role.
	 */
	public Entity getRoleEntity() {
		return roleEntity;
	}
}
