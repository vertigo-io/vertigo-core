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
package io.vertigo.vega.impl.security;

import io.vertigo.kernel.component.Plugin;

import java.io.Serializable;

/**
 * Cache plugin.
 * 
 * @author pchretien, npiedeloup
 */
public interface UiSecurityTokenCachePlugin extends Plugin {

	/**
	 * Add Object in cache.
	 * If key is already in cache, object is replaced.
	 * @param key data key
	 * @param data value
	 */
	void put(String key, final Serializable data);

	/**
	 * Get object from cache by its key.
	 * Return null, if not found or expired.
	 *
	 * @param key data key
	 * @return data value
	 */
	Serializable get(String key);

	/**
	 * Get and remove object from cache by its key.
	 * Return null, if not found or expired.
	 * Support concurrent calls.
	 * 
	 * @param key data key
	 * @return data value
	 */
	Serializable getAndRemove(String key);
}
