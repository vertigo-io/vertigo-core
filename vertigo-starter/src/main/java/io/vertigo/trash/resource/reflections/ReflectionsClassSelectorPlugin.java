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
package vertigoimpl.plugins.commons.resource.reflections;

import io.vertigoimpl.commons.resource.ClassSelectorPlugin;

import java.lang.annotation.Annotation;
import java.util.Set;


import org.reflections.Reflections;

import vertigo.kernel.lang.Assertion;

/**
 * Plugin de selecteur de classes. 
 * 
 * @author pchretien
 * @version $Id: ReflectionsClassSelectorPlugin.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public final class ReflectionsClassSelectorPlugin implements ClassSelectorPlugin {
	private Reflections reflections;

	private synchronized Reflections getReflections() {
		if (reflections == null) {
			reflections = new Reflections();
		}
		return reflections;
	}

	/** {@inheritDoc} */
	public Set<Class<?>> getTypesAnnotatedWith(final Annotation annotation) {
		Assertion.notNull(annotation);
		//---------------------------------------------------------------------
		return getReflections().getTypesAnnotatedWith(annotation);
	}

	/** {@inheritDoc} */
	public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
		Assertion.notNull(annotation);
		//---------------------------------------------------------------------
		return getReflections().getTypesAnnotatedWith(annotation);
	}

	/** {@inheritDoc} */
	public <T> Set<Class<? extends T>> getSubTypesOf(final Class<T> type) {
		Assertion.notNull(type);
		//---------------------------------------------------------------------
		return getReflections().getSubTypesOf(type);
	}

}
