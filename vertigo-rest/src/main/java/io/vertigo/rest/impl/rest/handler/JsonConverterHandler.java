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
package io.vertigo.rest.impl.rest.handler;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.rest.engine.JsonEngine;
import io.vertigo.rest.rest.engine.UiContext;
import io.vertigo.rest.rest.engine.UiListState;
import io.vertigo.rest.rest.engine.UiObject;
import io.vertigo.rest.rest.exception.SessionException;
import io.vertigo.rest.rest.exception.VSecurityException;
import io.vertigo.rest.rest.metamodel.EndPointDefinition;
import io.vertigo.rest.rest.metamodel.EndPointParam;
import io.vertigo.rest.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.rest.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.rest.security.UiSecurityTokenManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

/**
 * Params handler. 
 * It's an handler barrier : bellow this handler anything is object, over this handler it's json.
 * Extract and Json convert.
 * @author npiedeloup
 */
final class JsonConverterHandler implements RouteHandler {
	private static final String SERVER_SIDE_MANDATORY = "ServerSideToken mandatory";
	private static final String FORBIDDEN_OPERATION_FIELD_MODIFICATION = "Can't modify field:";

	private final JsonEngine jsonWriterEngine;
	private final JsonEngine jsonReaderEngine;

	private final UiSecurityTokenManager uiSecurityTokenManager;
	private final EndPointDefinition endPointDefinition;

	JsonConverterHandler(final UiSecurityTokenManager uiSecurityTokenManager, final EndPointDefinition endPointDefinition, final JsonEngine jsonWriterEngine, final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(uiSecurityTokenManager);
		Assertion.checkNotNull(endPointDefinition);
		Assertion.checkNotNull(jsonWriterEngine);
		Assertion.checkNotNull(jsonReaderEngine);
		//---------------------------------------------------------------------
		this.uiSecurityTokenManager = uiSecurityTokenManager;
		this.endPointDefinition = endPointDefinition;
		this.jsonWriterEngine = jsonWriterEngine;
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc}  */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		final UiContext multiPartBodyParsed = readMultiPartValue(request.body(), endPointDefinition.getEndPointParams());
		for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
			final Object value;
			switch (endPointParam.getParamType()) {
				case Body:
					value = readValue(request.body(), endPointParam);
					break;
				case MultiPartBody:
					value = multiPartBodyParsed.get(endPointParam.getName());
					//value = readValue(request.body(), endPointParam.getName(), endPointParam, uiSecurityTokenManager);
					break;
				case Path:
					value = readPrimitiveValue(request.params(endPointParam.getName()), endPointParam);
					break;
				case Query:
					value = readPrimitiveValue(request.queryParams(endPointParam.getName()), endPointParam);
					break;
				case Implicit:
					switch (ImplicitParam.valueOf(endPointParam.getName())) {
						case UiMessageStack:
							value = routeContext.getUiMessageStack();
							break;
						case UiListState:
							value = readQueryValue(request.queryMap(), endPointParam, uiSecurityTokenManager);
							break;
						case Request:
							value = request;
							break;
						case Response:
							value = response;
							break;
						default:
							throw new IllegalArgumentException("ImplicitParam : " + endPointParam.getName());
					}
					break;
				default:
					throw new IllegalArgumentException("RestParamType : " + endPointParam.getParamType());
			}
			Assertion.checkNotNull(value, "RestParam not found : {0}", endPointParam);
			routeContext.setParamValue(endPointParam, value);
		}

		final Object result = chain.handle(request, response, routeContext);
		setHeadersFromResultType(result, response);
		if (result != null) {
			return writeValue(result);
		}
		return ""; //jetty understand null as 404 not found
	}

	private void setHeadersFromResultType(final Object result, final Response response) {
		response.type("application/json;charset=UTF-8");
		if (result != null) {
			if (result instanceof List) {
				response.header("x-total-count", String.valueOf(((List) result).size()));
			}
		} else {
			response.status(HttpServletResponse.SC_NO_CONTENT);
		}
	}

	private UiContext readMultiPartValue(final String jsonBody, final List<EndPointParam> endPointParams) throws VSecurityException {
		final List<EndPointParam> multiPartEndPointParams = new ArrayList<>();
		final Map<String, Class<?>> multiPartBodyParams = new HashMap<>();
		for (final EndPointParam endPointParam : endPointParams) {
			if (endPointParam.getParamType() == RestParamType.MultiPartBody || endPointParam.getParamType() == RestParamType.Implicit) {
				multiPartEndPointParams.add(endPointParam);
				multiPartBodyParams.put(endPointParam.getName(), endPointParam.getType());
			}
		}
		if (!multiPartBodyParams.isEmpty()) {
			final UiContext uiContext = jsonReaderEngine.uiContextFromJson(jsonBody, multiPartBodyParams);
			for (final EndPointParam endPointParam : multiPartEndPointParams) {
				final Serializable value = uiContext.get(endPointParam.getName());
				if (value instanceof UiObject) {
					postReadUiObject((UiObject<DtObject>) value, endPointParam.getName(), endPointParam, uiSecurityTokenManager);
				}
			}
			return uiContext;
		}
		return null;
	}

	private Object readPrimitiveValue(final String json, final EndPointParam endPointParam) {
		final Class<?> paramClass = endPointParam.getType();
		if (json == null) {
			return null;
		} else if (paramClass.isPrimitive()) {
			return jsonReaderEngine.fromJson(json, paramClass);
		} else if (String.class.isAssignableFrom(paramClass)) {
			return json;
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return Integer.valueOf(json);
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return Long.valueOf(json);
		} else if (Date.class.isAssignableFrom(paramClass)) {
			return jsonReaderEngine.fromJson(json, paramClass);
		} else {
			throw new RuntimeException("Unsupported type " + paramClass.getSimpleName());
		}
		//return jsonReaderEngine.fromJson(json, paramClass);
	}

	private <D> D readQueryValue(final QueryParamsMap queryMap, final EndPointParam endPointParam, final UiSecurityTokenManager uiSecurityTokenManager2) {
		final Class<D> paramClass = (Class<D>) endPointParam.getType();
		/* As it says : UnSafe.
		 * final D object = UnsafeAllocator.create().newInstance(paramClass);
		for(Field objectField : paramClass.getDeclaredFields()) {
			Object paramValue = parseQueryParam(queryMap, objectField.getName(), objectField.getType(), null);
			if(paramValue != null) {
				objectField.setAccessible(true);
				objectField.set(object, paramValue);
			}
		}*/
		if (UiListState.class.isAssignableFrom(paramClass)) {
			final int top = parseQueryParam(queryMap, "top", Integer.class, 20);
			final int skip = parseQueryParam(queryMap, "skip", Integer.class, 0);
			final String sortFieldName = parseQueryParam(queryMap, "sortFieldName", String.class, null);
			final boolean sortDesc = parseQueryParam(queryMap, "sortDesc", Boolean.class, true);
			final D uiListState = (D) new UiListState(top, skip, sortFieldName, sortDesc);
			return uiListState;
		}
		return null;
	}

	private static <D> D parseQueryParam(final QueryParamsMap queryMap, final String paramName, final Class<D> paramType, final D defaultValue) {
		final QueryParamsMap value = queryMap.get(paramName);
		if (value.hasValue()) {
			final Object result;
			if (Boolean.class.equals(paramType)) {
				result = value.booleanValue();
			} else if (Double.class.equals(paramType)) {
				result = value.doubleValue();
			} else if (Float.class.equals(paramType)) {
				result = value.floatValue();
			} else if (Integer.class.equals(paramType)) {
				result = value.integerValue();
			} else if (Long.class.equals(paramType)) {
				result = value.longValue();
			} else if (String.class.equals(paramType)) {
				result = value.value();
			} else {
				throw new IllegalArgumentException("property type not supported in query : " + paramType.getSimpleName() + " (" + paramName + ")");
			}
			return (D) result;
		}
		return defaultValue;
	}

	private Object readValue(final String json, final EndPointParam endPointParam) throws VSecurityException {
		final Class<?> paramClass = endPointParam.getType();
		if (json == null) {
			return null;
		} else if (String.class.isAssignableFrom(paramClass)) {
			return json;
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return Integer.valueOf(json);
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return Long.valueOf(json);
		} else if (DtObject.class.isAssignableFrom(paramClass)) {
			final UiObject<DtObject> uiObject = jsonReaderEngine.<DtObject> uiObjectFromJson(json, (Class<DtObject>) paramClass);
			if (uiObject != null) {
				postReadUiObject(uiObject, "", endPointParam, uiSecurityTokenManager);
			}
			return uiObject;
		} else if (UiContext.class.isAssignableFrom(paramClass)) {
			throw new RuntimeException("Unsupported type UiContext (use @InnerBodyParams instead).");
		} else {
			return jsonReaderEngine.fromJson(json, paramClass);
		}
	}

	private static void postReadUiObject(final UiObject<DtObject> uiObject, final String inputKey, final EndPointParam endPointParam, final UiSecurityTokenManager uiSecurityTokenManager) throws VSecurityException {
		uiObject.setInputKey(inputKey);
		checkUnauthorizedFieldModifications(uiObject, endPointParam);

		if (endPointParam.isNeedServerSideToken()) {
			final String accessToken = uiObject.getServerSideToken();
			if (accessToken == null) {
				throw new VSecurityException(SERVER_SIDE_MANDATORY); //same message for no ServerSideToken or bad ServerSideToken
			}
			final Serializable serverSideObject;
			if (endPointParam.isConsumeServerSideToken()) {
				serverSideObject = uiSecurityTokenManager.getAndRemove(accessToken); //TODO if exception : token is consume ?
			} else {
				serverSideObject = uiSecurityTokenManager.get(accessToken);
			}
			if (serverSideObject == null) {
				throw new VSecurityException(SERVER_SIDE_MANDATORY); //same message for no ServerSideToken or bad ServerSideToken
			}
			uiObject.setServerSideObject((DtObject) serverSideObject);
		}
	}

	private static void checkUnauthorizedFieldModifications(final UiObject<DtObject> uiObject, final EndPointParam endPointParam) throws VSecurityException {
		for (final String excludedField : endPointParam.getExcludedFields()) {
			if (uiObject.isModified(excludedField)) {
				throw new VSecurityException(FORBIDDEN_OPERATION_FIELD_MODIFICATION + excludedField);
			}
		}
		final Set<String> includedFields = endPointParam.getIncludedFields();
		if (!includedFields.isEmpty()) {
			for (final String modifiedField : uiObject.getModifiedFields()) {
				if (!includedFields.contains(modifiedField)) {
					throw new VSecurityException(FORBIDDEN_OPERATION_FIELD_MODIFICATION + modifiedField);
				}
			}
		}
	}

	private String writeValue(final Object value) {
		Assertion.checkNotNull(value);
		//---------------------------------------------------------------------
		if (endPointDefinition.isServerSideSave()) {
			if (UiContext.class.isInstance(value)) {
				//TODO build json in jsonWriterEngine 
				final StringBuilder sb = new StringBuilder();
				sb.append("{");
				String sep = "";
				for (final Map.Entry<String, Serializable> entry : ((UiContext) value).entrySet()) {
					sb.append(sep);
					String encodedValue;
					if (entry.getValue() instanceof DtList || entry.getValue() instanceof DtObject) {
						encodedValue = writeValue(entry.getValue());
					} else {
						encodedValue = jsonWriterEngine.toJson(entry.getValue());
					}
					sb.append("\"").append(entry.getKey()).append("\":").append(encodedValue).append("");
					sep = ", ";
				}
				sb.append("}");
				return sb.toString();
			} else if (DtList.class.isInstance(value)) {
				final String tokenId = uiSecurityTokenManager.put((DtList) value);
				return jsonWriterEngine.toJsonWithTokenId(value, tokenId, endPointDefinition.getIncludedFields(), endPointDefinition.getExcludedFields());
			} else if (DtObject.class.isInstance(value)) {
				final String tokenId = uiSecurityTokenManager.put((DtObject) value);
				return jsonWriterEngine.toJsonWithTokenId(value, tokenId, endPointDefinition.getIncludedFields(), endPointDefinition.getExcludedFields());
			} else {
				throw new RuntimeException("Return type can't be ServerSide :" + (value != null ? value.getClass().getSimpleName() : "null"));
			}
		}
		return jsonWriterEngine.toJson(value, endPointDefinition.getIncludedFields(), endPointDefinition.getExcludedFields());
	}
}
