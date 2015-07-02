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
package io.vertigo.vega.plugins.rest.handler.converter;

import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.engine.UiContext;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;

public final class PrimitiveJsonConverter implements JsonConverter {

	private final JsonEngine jsonReaderEngine;

	/**
	 * @param jsonReaderEngine jsonReaderEngine
	 */
	@Inject
	public PrimitiveJsonConverter(final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return paramClass.isPrimitive()
				|| String.class.isAssignableFrom(paramClass)
				|| Integer.class.isAssignableFrom(paramClass)
				|| Long.class.isAssignableFrom(paramClass)
				|| Float.class.isAssignableFrom(paramClass)
				|| Double.class.isAssignableFrom(paramClass)
				|| Date.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateRouteContext(final Object input, final EndPointParam endPointParam, final RouteContext routeContext) {
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input) || getSupportedInputs()[1].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Class<?> paramClass = endPointParam.getType();
		final Object value;
		if (input instanceof String) {
			value = readPrimitiveValue((String) input, paramClass);
		} else if (input instanceof UiContext) {
			value = ((UiContext) input).get(endPointParam.getName());
		} else {
			throw new IllegalArgumentException(String.format("This JsonConverter can't read the asked type %s. Only %s is supported", paramClass.getSimpleName(), UiListDelta.class.getSimpleName()));
		}
		routeContext.setParamValue(endPointParam, value);

	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { String.class, UiContext.class };
	}

	private <D> D readPrimitiveValue(final String json, final Class<D> paramClass) {
		if (json == null) {
			return null;
		} else if (paramClass.isPrimitive()) {
			return jsonReaderEngine.fromJson(json, paramClass);
		} else if (String.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(json);
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(Integer.valueOf(json));
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(Long.valueOf(json));
		} else if (Float.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(Float.valueOf(json));
		} else if (Double.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(Double.valueOf(json));
		} else if (Date.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(jsonReaderEngine.fromJson(json, paramClass));
		} else {
			throw new IllegalArgumentException("Unsupported type " + paramClass.getSimpleName());
		}
	}

}
