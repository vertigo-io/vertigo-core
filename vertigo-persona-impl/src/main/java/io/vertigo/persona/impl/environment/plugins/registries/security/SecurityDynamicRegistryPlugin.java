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

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.kernel.Home;
import io.vertigo.persona.security.model.Operation;
import io.vertigo.persona.security.model.Permission;
import io.vertigo.persona.security.model.Resource;
import io.vertigo.persona.security.model.Role;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author pchretien
 * @version $Id: SecurityDynamicRegistryPlugin.java,v 1.4 2014/02/03 17:29:01 pchretien Exp $
 */
public final class SecurityDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin<SecurityGrammar> {
	/**	
	 * Constructeur
	 * @param resourceManager Resource manager
	 * @param url Url du fichier XML de configuration
	 */
	@Inject
	public SecurityDynamicRegistryPlugin(final ResourceManager resourceManager, @Named("url") final String url) {
		super(SecurityGrammar.INSTANCE);
		Home.getDefinitionSpace().register(Role.class);
		Home.getDefinitionSpace().register(Permission.class);
		Home.getDefinitionSpace().register(Operation.class);
		Home.getDefinitionSpace().register(Resource.class);
		final XmlSecurityLoader xmlSecurityLoader = new XmlSecurityLoader(resourceManager, url);
		xmlSecurityLoader.load();
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		//				final Entity metaDefinition = xdefinition.getEntity();
		//				if (metaDefinition.equals(getGrammar().getRoleEntity())) {
		//					//On enregistre les roles			
		//					Home.getNameSpace().registerDefinition(createRole(xdefinition.getDefinitionKey().getName()), Role.class);
		//				} else {
		//					throw new IllegalArgumentException("Type de d�finition non g�r�e: " + xdefinition.getDefinitionKey().getName());
		//				}
	}

	//	private static Role createRole(final String name) {
	//		//
	//		//Pourquoi pas de permissions ??????
	//		return new Role(name, name, Collections.<Permission> emptyList());
	//	}

}
