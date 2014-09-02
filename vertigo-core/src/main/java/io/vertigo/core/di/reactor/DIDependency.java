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
package io.vertigo.core.di.reactor;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.DIAnnotationUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Une dépendance est identifiée par son id et son caractère optionnel.
 * Un composant possède une liste de dépendances.
 * @author pchretien
 */
final class DIDependency {
	//Id du composant déclarant la dépendance
	private final DIComponentInfo componentInfo;
	private final String id;
	private final boolean option;

	//Each component has a dependency on its plugins
	DIDependency(final DIComponentInfo componentInfo, final String id) {
		Assertion.checkNotNull(componentInfo);
		Assertion.checkArgNotEmpty(id);
		//---------------------------------------------------------------------
		this.componentInfo = componentInfo;
		this.id = id;
		option = false;
	}

	DIDependency(final DIComponentInfo componentInfo, final Field field) {
		Assertion.checkNotNull(componentInfo);
		Assertion.checkNotNull(field);
		//---------------------------------------------------------------------
		this.componentInfo = componentInfo;
		id = DIAnnotationUtil.buildId(field);
		option = DIAnnotationUtil.isOptional(field);
	}

	DIDependency(final DIComponentInfo componentInfo, final Constructor<?> constructor, final int i) {
		Assertion.checkNotNull(componentInfo);
		Assertion.checkNotNull(constructor);
		//---------------------------------------------------------------------
		this.componentInfo = componentInfo;
		id = DIAnnotationUtil.buildId(constructor, i);
		option = DIAnnotationUtil.isOptional(constructor, i);
	}

	String getId() {
		return id;
	}

	boolean isOptional() {
		return option;
	}

	@Override
	public String toString() {
		return id + " (referenced by " + componentInfo.getId() + ")";
	}
}
