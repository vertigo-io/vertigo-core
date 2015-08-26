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

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.engine.UiContext;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.lang.reflect.Type;
import java.util.Arrays;

import javax.inject.Inject;

public final class DtObjectJsonConverter implements JsonConverter {
	private final JsonEngine jsonReaderEngine;

	/**
	 * @param jsonReaderEngine jsonReaderEngine
	 */
	@Inject
	public DtObjectJsonConverter(final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return DtObject.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc}*/
	@Override
	public void populateRouteContext(final Object input, final EndPointParam endPointParam, final RouteContext routeContext) throws VSecurityException {
		final Class<?> paramClass = endPointParam.getType();
		Assertion.checkArgument(DtObject.class.isAssignableFrom(paramClass), "This JsonConverter can't read the asked type {0}. Only {1} is supported", paramClass.getSimpleName(), DtObject.class.getSimpleName());
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input) || getSupportedInputs()[1].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Type paramGenericType = endPointParam.getGenericType();
		final String objectPath;
		final UiObject<DtObject> uiObject;
		if (input instanceof String) {
			uiObject = jsonReaderEngine.<DtObject> uiObjectFromJson((String) input, paramGenericType);
			objectPath = "";
		} else if (input instanceof UiContext) {
			uiObject = (UiObject<DtObject>) ((UiContext) input).get(endPointParam.getName());
			Assertion.checkNotNull(uiObject, "InnerParam not found : {0}", endPointParam);
			objectPath = endPointParam.getName();
		} else {
			throw new IllegalArgumentException(String.format("This JsonConverter can't read the asked type %s. Only %s is supported", paramClass.getSimpleName(), UiListDelta.class.getSimpleName()));
		}
		//-----
		UiObjectUtil.postReadUiObject(uiObject, objectPath, endPointParam);
		routeContext.setParamValue(endPointParam, uiObject);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { String.class, UiContext.class };
	}
}
