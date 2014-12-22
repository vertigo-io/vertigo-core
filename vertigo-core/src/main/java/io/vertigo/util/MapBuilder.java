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
package io.vertigo.util;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pchretien
 */
public final class MapBuilder<K, V> implements Builder<Map<K, V>> {
	private Map<K, V> map = new HashMap<>();

	public MapBuilder<K, V> put(final K key, final V value) {
		Assertion.checkNotNull(key);
		Assertion.checkNotNull(value);
		//-----
		map.put(key, value);
		return this;
	}

	public MapBuilder<K, V> unmodifiable() {
		this.map = Collections.unmodifiableMap(map);
		return this;
	}

	@Override
	public Map<K, V> build() {
		return map;
	}
}
