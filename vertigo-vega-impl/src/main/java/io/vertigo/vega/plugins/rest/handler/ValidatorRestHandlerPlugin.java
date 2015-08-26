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
import io.vertigo.util.ClassUtil;
import io.vertigo.vega.impl.rest.RestHandlerPlugin;
import io.vertigo.vega.rest.engine.UiList;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.model.DtListDelta;
import io.vertigo.vega.rest.model.ExtendedObject;
import io.vertigo.vega.rest.validation.DtObjectValidator;
import io.vertigo.vega.rest.validation.UiMessageStack;
import io.vertigo.vega.rest.validation.ValidationUserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;

/**
 * Params handler. Extract and Json convert.
 * @author npiedeloup
 */
public final class ValidatorRestHandlerPlugin implements RestHandlerPlugin {

	/** {@inheritDoc} */
	@Override
	public boolean accept(final EndPointDefinition endPointDefinition) {
		return true;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		final EndPointDefinition endPointDefinition = routeContext.getEndPointDefinition();
		final UiMessageStack uiMessageStack = routeContext.getUiMessageStack();
		for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
			final Object value = routeContext.getParamValue(endPointParam);
			validateParam(value, uiMessageStack, endPointParam, routeContext);
		}
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
		return chain.handle(request, response, routeContext);
	}

	private void validateParam(final Object value, final UiMessageStack uiMessageStack, final EndPointParam endPointParam, final RouteContext routeContext) {
		if (value instanceof UiObject) {
			final UiObject<DtObject> uiObject = (UiObject<DtObject>) value;
			final List<DtObjectValidator<DtObject>> dtObjectValidators = obtainDtObjectValidators(endPointParam);
			//Only authorized fields have already been checked (JsonConverterHandler)
			final DtObject updatedDto = uiObject.mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			routeContext.registerUpdatedDto(endPointParam, uiObject.getInputKey(), updatedDto);
		} else if (value instanceof UiListDelta) {
			final UiListDelta<DtObject> uiListDelta = (UiListDelta<DtObject>) value;
			final List<DtObjectValidator<DtObject>> dtObjectValidators = obtainDtObjectValidators(endPointParam);
			final Map<String, DtObject> contextKeyMap = new HashMap<>();

			//Only authorized fields have already been checked (JsonConverterHandler)
			final DtList<DtObject> dtListCreates = mergeAndCheckInput(uiListDelta.getObjectType(), uiListDelta.getCreatesMap(), dtObjectValidators, uiMessageStack, contextKeyMap);
			final DtList<DtObject> dtListUpdates = mergeAndCheckInput(uiListDelta.getObjectType(), uiListDelta.getUpdatesMap(), dtObjectValidators, uiMessageStack, contextKeyMap);
			final DtList<DtObject> dtListDeletes = mergeAndCheckInput(uiListDelta.getObjectType(), uiListDelta.getDeletesMap(), dtObjectValidators, uiMessageStack, contextKeyMap);
			final DtListDelta<DtObject> dtListDelta = new DtListDelta<>(dtListCreates, dtListUpdates, dtListDeletes);
			routeContext.registerUpdatedDtListDelta(endPointParam, dtListDelta, contextKeyMap);
		} else if (value instanceof UiList) {
			final UiList<DtObject> uiList = (UiList<DtObject>) value;
			final List<DtObjectValidator<DtObject>> dtObjectValidators = obtainDtObjectValidators(endPointParam);
			final Map<String, DtObject> contextKeyMap = new HashMap<>();

			//Only authorized fields have already been checked (JsonConverterHandler)
			final DtList<DtObject> dtList = mergeAndCheckInput(uiList.getObjectType(), uiList, dtObjectValidators, uiMessageStack, contextKeyMap);
			routeContext.registerUpdatedDtList(endPointParam, dtList, contextKeyMap);
		} else if (value instanceof ExtendedObject) {
			final ExtendedObject<?> extendedObject = (ExtendedObject) value;
			validateParam(extendedObject.getInnerObject(), uiMessageStack, endPointParam, routeContext);
			final Object updatedValue = routeContext.getParamValue(endPointParam);
			final ExtendedObject<?> updatedExtendedObject = new ExtendedObject(updatedValue);
			updatedExtendedObject.putAll(extendedObject);
			routeContext.setParamValue(endPointParam, updatedExtendedObject);
		}
	}

	private static List<DtObjectValidator<DtObject>> obtainDtObjectValidators(final EndPointParam endPointParam) {
		final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses = endPointParam.getDtObjectValidatorClasses();
		final List<DtObjectValidator<DtObject>> dtObjectValidators = new ArrayList<>(dtObjectValidatorClasses.size());
		for (final Class<? extends DtObjectValidator> dtObjectValidatorClass : dtObjectValidatorClasses) {
			dtObjectValidators.add(ClassUtil.newInstance(dtObjectValidatorClass));
		}
		return dtObjectValidators;
	}

	private static <D extends DtObject> DtList<D> mergeAndCheckInput(final Class<D> objectType, final Map<String, UiObject<D>> uiObjectMap, final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack, final Map<String, DtObject> contextKeyMap) {
		final DtList<D> dtList = new DtList<>(objectType);
		for (final Map.Entry<String, UiObject<D>> entry : uiObjectMap.entrySet()) {
			final D dto = entry.getValue().mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
			contextKeyMap.put(entry.getValue().getInputKey(), dto);
		}
		return dtList;
	}

	private static <D extends DtObject> DtList<D> mergeAndCheckInput(final Class<DtObject> objectType, final UiList<D> uiList, final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack, final Map<String, DtObject> contextKeyMap) {
		final DtList<D> dtList = new DtList<>(objectType);
		for (final UiObject<D> element : uiList) {
			final D dto = element.mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
			contextKeyMap.put(element.getInputKey(), dto);
		}
		return dtList;
	}
}
