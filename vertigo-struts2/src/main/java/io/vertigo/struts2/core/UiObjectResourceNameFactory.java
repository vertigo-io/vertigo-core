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
package io.vertigo.struts2.core;

import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.ResourceNameFactory;

/**
 * ResourceNameFactory standard des UiObject s�curis�es.
 * @author npiedeloup
 */
public final class UiObjectResourceNameFactory implements ResourceNameFactory {
	private final ResourceNameFactory beanResourceNameFactory;

	/**
	 * Constructeur.
	 * Prend en entrée le pattern de la chaine de resource à produire.
	 * Il peut être paramétré avec des propriétés de l'objet avec la syntaxe : ${maPropriete}
	 * @param beanResourceNameFactory BeanResourceNameFactory de la resource.
	 */
	public UiObjectResourceNameFactory(final ResourceNameFactory beanResourceNameFactory) {
		this.beanResourceNameFactory = beanResourceNameFactory;
	}

	/** {@inheritDoc} */
	@Override
	public String toResourceName(final Object value) {
		Assertion.checkArgument(value instanceof UiObject, "La resource est un {0}, elle doit être un UiObject", value.getClass().getSimpleName());
		//-----
		return beanResourceNameFactory.toResourceName(((UiObject) value).getInnerObject());
	}
}
