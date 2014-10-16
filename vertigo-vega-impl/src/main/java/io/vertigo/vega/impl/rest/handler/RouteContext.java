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
package io.vertigo.vega.impl.rest.handler;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.model.DtListDelta;
import io.vertigo.vega.rest.validation.UiContextResolver;
import io.vertigo.vega.rest.validation.UiMessageStack;

import java.util.Map;

import spark.Request;

/**
* @author npiedeloup 
*/
public final class RouteContext {
	private static final String UI_MESSAGE_STACK = "UiMessageStack";

	private final Request request;
	private final UiContextResolver uiContextResolver;

	RouteContext(final Request request) {
		this.request = request;
		uiContextResolver = new UiContextResolver();
		request.attribute(UI_MESSAGE_STACK, new UiMessageStack(uiContextResolver));
	}

	public UiMessageStack getUiMessageStack() {
		return (UiMessageStack) request.attribute(UI_MESSAGE_STACK);
	}

	public void setParamValue(final EndPointParam endPointParam, final Object value) {
		request.attribute(endPointParam.getFullName(), value);
	}

	public Object getParamValue(final EndPointParam endPointParam) {
		return request.attribute(endPointParam.getFullName());
	}

	public void registerUiObject(final EndPointParam endPointParam, final UiObject uiObject) {
		request.attribute(endPointParam.getFullName(), uiObject);
	}

	public void registerUpdatedDto(final EndPointParam endPointParam, final String contextKey, final DtObject updatedDto) {
		uiContextResolver.register(contextKey, updatedDto);
		request.attribute(endPointParam.getFullName() + "-input", request.attribute(endPointParam.getFullName()));
		request.attribute(endPointParam.getFullName(), updatedDto);
	}

	public void registerUiListDelta(final EndPointParam endPointParam, final UiObject uiObject) {
		request.attribute(endPointParam.getFullName(), uiObject);
	}

	public void registerUpdatedDtListDelta(final EndPointParam endPointParam, final DtListDelta dtListDelta, final Map<String, DtObject> contextKeyMap) {
		final UiListDelta<?> uiListDelta = (UiListDelta<?>) request.attribute(endPointParam.getFullName());
		for (final Map.Entry<String, DtObject> entry : contextKeyMap.entrySet()) {
			uiContextResolver.register(entry.getKey(), entry.getValue());
		}
		request.attribute(endPointParam.getFullName() + "-input", uiListDelta);
		request.attribute(endPointParam.getFullName(), dtListDelta);
	}
}
