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
package io.vertigo.vega.plugins.rest.handler;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.rest.engine.UiList;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.model.DtListDelta;
import io.vertigo.vega.rest.validation.UiContextResolver;
import io.vertigo.vega.rest.validation.UiMessageStack;

import java.util.Map;

import spark.Request;
import spark.Response;

/**
* @author npiedeloup
*/
public final class RouteContext {
	private static final String UI_MESSAGE_STACK = "UiMessageStack";
	private final EndPointDefinition endPointDefinition;
	private final Request request;
	private final Response response;
	private final UiContextResolver uiContextResolver;

	/**
	 * Constructor.
	 * @param request Request
	 * @param endPointDefinition EndPointDefinition
	 */
	public RouteContext(final Request request, final Response response, final EndPointDefinition endPointDefinition) {
		this.request = request;
		this.response = response;
		this.endPointDefinition = endPointDefinition;
		uiContextResolver = new UiContextResolver();
		request.attribute(UI_MESSAGE_STACK, new UiMessageStack(uiContextResolver));
	}

	/**
	 * @return EndPointDefinition
	 */
	public EndPointDefinition getEndPointDefinition() {
		return endPointDefinition;
	}

	/**
	 * @return UiMessageStack
	 */
	public UiMessageStack getUiMessageStack() {
		return (UiMessageStack) request.attribute(UI_MESSAGE_STACK);
	}

	/**
	 * @return Request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @return Response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * Set param of an endpoint.
	 * @param endPointParam param name
	 * @param value param value
	 */
	public void setParamValue(final EndPointParam endPointParam, final Object value) {
		request.attribute(endPointParam.getFullName(), value);
	}

	/**
	 * Get param of an endpoint.
	 * @param endPointParam param name
	 * @return param value
	 */
	public Object getParamValue(final EndPointParam endPointParam) {
		return request.attribute(endPointParam.getFullName());
	}

	/**
	 * Register UiObject.
	 * @param endPointParam param name
	 * @param uiObject param value
	 */
	public void registerUiObject(final EndPointParam endPointParam, final UiObject uiObject) {
		request.attribute(endPointParam.getFullName(), uiObject);
	}

	/**
	 * Register Updated Dto.
	 * @param endPointParam param name
	 * @param contextKey Context key of this dto in request
	 * @param updatedDto param value
	 */
	public void registerUpdatedDto(final EndPointParam endPointParam, final String contextKey, final DtObject updatedDto) {
		uiContextResolver.register(contextKey, updatedDto);
		request.attribute(endPointParam.getFullName() + "-input", request.attribute(endPointParam.getFullName()));
		request.attribute(endPointParam.getFullName(), updatedDto);
	}

	/**
	 * Register Updated DtListDelta.
	 * @param endPointParam param name
	 * @param dtListDelta param value
	 * @param contextKeyMap Map of elements contextKey
	 */
	public void registerUpdatedDtListDelta(final EndPointParam endPointParam, final DtListDelta dtListDelta, final Map<String, DtObject> contextKeyMap) {
		final UiListDelta<?> uiListDelta = (UiListDelta<?>) request.attribute(endPointParam.getFullName());
		for (final Map.Entry<String, DtObject> entry : contextKeyMap.entrySet()) {
			uiContextResolver.register(entry.getKey(), entry.getValue());
		}
		request.attribute(endPointParam.getFullName() + "-input", uiListDelta);
		request.attribute(endPointParam.getFullName(), dtListDelta);
	}

	/**
	 * Register Updated DtList.
	 * @param endPointParam param name
	 * @param dtList param value
	 * @param contextKeyMap Map of elements contextKey
	 */
	public void registerUpdatedDtList(final EndPointParam endPointParam, final DtList dtList, final Map<String, DtObject> contextKeyMap) {
		final UiList<?> uiList = (UiList<?>) request.attribute(endPointParam.getFullName());
		for (final Map.Entry<String, DtObject> entry : contextKeyMap.entrySet()) {
			uiContextResolver.register(entry.getKey(), entry.getValue());
		}
		request.attribute(endPointParam.getFullName() + "-input", uiList);
		request.attribute(endPointParam.getFullName(), dtList);
	}
}
