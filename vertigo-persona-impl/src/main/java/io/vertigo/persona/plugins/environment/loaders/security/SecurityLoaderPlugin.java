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
package io.vertigo.persona.plugins.environment.loaders.security;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.persona.security.model.Permission;
import io.vertigo.persona.security.model.Role;

import javax.inject.Inject;

/**
 * @author pchretien
 * @version $Id: SecurityDynamicRegistryPlugin.java,v 1.4 2014/02/03 17:29:01 pchretien Exp $
 */
public final class SecurityLoaderPlugin implements LoaderPlugin {
	private final ResourceManager resourceManager;

	/**	
	 * Constructeur
	 */
	@Inject
	public SecurityLoaderPlugin(ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//---------------------------------------------------------------------
		//super(SecurityGrammar.INSTANCE);
		Home.getDefinitionSpace().register(Role.class);
		Home.getDefinitionSpace().register(Permission.class);
		this.resourceManager = resourceManager;

	}

	/** {@inheritDoc} */
	public void load(String resourcePath, DynamicDefinitionRepository dynamicModelRepository) {
		final XmlSecurityLoader xmlSecurityLoader = new XmlSecurityLoader(resourceManager, resourcePath);
		xmlSecurityLoader.load();
	}

	/** {@inheritDoc} */
	public String getType() {
		return "security";
	}
}
