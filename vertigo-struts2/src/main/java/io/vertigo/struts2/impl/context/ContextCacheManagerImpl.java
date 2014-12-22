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
package io.vertigo.struts2.impl.context;

import io.vertigo.lang.Assertion;
import io.vertigo.struts2.context.ContextCacheManager;
import io.vertigo.struts2.core.KActionContext;

import javax.inject.Inject;

/**
 * Manager de gestion du cache.
 *
 * @author pchretien
 */
public final class ContextCacheManagerImpl implements ContextCacheManager {
	private final ContextCachePlugin contextCachePlugin;

	/**
	 * Constructeur.
	 * @param contextCachePlugin Plugin de gestion du cache
	 */
	@Inject
	public ContextCacheManagerImpl(final ContextCachePlugin contextCachePlugin) {
		Assertion.checkNotNull(contextCachePlugin);
		//-----
		this.contextCachePlugin = contextCachePlugin;
	}

	//=========================================================================
	//==================Gestion du rendu et des interactions===================
	//=========================================================================

	/** {@inheritDoc} */
	@Override
	public void put(final KActionContext context) {
		contextCachePlugin.put(context);
	}

	/** {@inheritDoc} */
	@Override
	public KActionContext get(final String key) {
		return contextCachePlugin.get(key);
	}
}
