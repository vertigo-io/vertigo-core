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
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.vega.rest.validation.UiMessageStack;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spark.Request;

public final class ImplicitJsonConverter implements JsonConverter {

	/** {@inheritDoc} */
	@Override
	public boolean canHandle(final Class<?> paramClass) {
		return UiMessageStack.class.isAssignableFrom(paramClass)
				|| HttpServletRequest.class.isAssignableFrom(paramClass)
				|| HttpServletResponse.class.isAssignableFrom(paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public void populateRouteContext(final Object input, final EndPointParam endPointParam, final RouteContext routeContext) {
		Assertion.checkArgument(getSupportedInputs()[0].isInstance(input), "This JsonConverter doesn't support this input type {0}. Only {1} is supported", input.getClass().getSimpleName(), Arrays.toString(getSupportedInputs()));
		//-----
		final Object value = readImplicitValue((Request) input, endPointParam, routeContext);
		routeContext.setParamValue(endPointParam, value);
	}

	/** {@inheritDoc} */
	@Override
	public Class[] getSupportedInputs() {
		return new Class[] { Request.class };
	}

	private Object readImplicitValue(final Request request, final EndPointParam endPointParam, final RouteContext routeContext) {
		switch (ImplicitParam.valueOf(endPointParam.getName())) {
			case UiMessageStack:
				return routeContext.getUiMessageStack();
			case Request:
				return request.raw();
			case Response:
				return routeContext.getResponse().raw();
			default:
				throw new IllegalArgumentException("ImplicitParam : " + endPointParam.getName());
		}
	}

}
