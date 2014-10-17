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
package io.vertigo.struts2.impl.servlet;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Container Vertigo wrapper de parametres HttpRequest.
 * @author npiedeloup
 */
public final class RequestContainerWrapper implements Container {
	private final HttpServletRequest request;

	/**
	 * Constructeur.
	 * @param request Request
	 */
	public RequestContainerWrapper(final HttpServletRequest request) {
		this.request = request;
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		return request.getParameterMap().containsKey(id);
	}

	/** {@inheritDoc} */
	@Override
	public <T> T resolve(final String id, final Class<T> componentClass) {
		final String[] values = request.getParameterValues(id);
		Assertion.checkNotNull(values, "Le paramètre {0} est obligatoire.", id);
		final String firstValue = values.length > 0 ? values[0] : null;

		if (String[].class.equals(componentClass)) {
			return componentClass.cast(values);
		}
		if (String.class.equals(componentClass)) {
			return componentClass.cast(firstValue);
		} else if (Long.class.equals(componentClass)) {
			return firstValue == null || firstValue.isEmpty() ? null : componentClass.cast(Long.valueOf(firstValue));
		} else if (Integer.class.equals(componentClass)) {
			return firstValue == null || firstValue.isEmpty() ? null : componentClass.cast(Integer.valueOf(firstValue));
		}
		throw new IllegalArgumentException("Le type du paramètre " + id + " (" + componentClass.getName() + ") n'est pas support� (String[], String, Long, Integer)");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return request.getParameterMap().keySet();
	}

}
