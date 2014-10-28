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

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.engine.UiContext;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.model.DtListDelta;
import io.vertigo.vega.rest.model.UiListState;
import io.vertigo.vega.security.UiSecurityTokenManager;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import com.google.gson.JsonSyntaxException;

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
		UiContext innerBodyParsed = null; //we can't read body at first : because if it's a multipart request call body() disabled getParts() access.
		for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
			try {
				final Object value;
				if (KFileUtil.isKFileParam(endPointParam)) {
					value = KFileUtil.readKFileParam(request, endPointParam);
				} else {
					switch (endPointParam.getParamType()) {
						case Body:
							value = readValue(request.body(), endPointParam);
							break;
						case InnerBody:
							if (innerBodyParsed == null) {
								innerBodyParsed = readInnerBodyValue(request.body(), endPointDefinition.getEndPointParams());
							}
							value = innerBodyParsed.get(endPointParam.getName());
							break;
						case Path:
							value = readPrimitiveValue(request.params(endPointParam.getName()), endPointParam.getType());
							break;
						case Query:
							value = readQueryValue(request.queryMap(), endPointParam);
							break;
						case Header:
							value = readPrimitiveValue(request.headers(endPointParam.getName()), endPointParam.getType());
							break;
						case Implicit:
							switch (ImplicitParam.valueOf(endPointParam.getName())) {
								case UiMessageStack:
									value = routeContext.getUiMessageStack();
									break;
								case Request:
									value = request.raw();
									break;
								case Response:
									value = response.raw();
									break;
								/*case UiListState:
									value = readQueryValue(request.queryMap(), endPointParam);
									break;*/
								//		case Request:
								//			value = request;
								//			break;
								//		case Response:
								//			value = response;
								//			break;
								default:
									throw new IllegalArgumentException("ImplicitParam : " + endPointParam.getName());
							}
							break;
						default:
							throw new IllegalArgumentException("RestParamType : " + endPointParam.getFullName());
					}
				}
				Assertion.checkNotNull(value, "RestParam not found : {0}", endPointParam);
				routeContext.setParamValue(endPointParam, value);
			} catch (final JsonSyntaxException e) {
				throw new JsonSyntaxException("Error parsing param " + endPointParam.getFullName() + " on service " + endPointDefinition.getVerb() + " " + endPointDefinition.getPath(), e);
			}
		}

		final Object result = chain.handle(request, response, routeContext);
		if (KFileUtil.isKFileResult(result)) {
			KFileUtil.sendKFile(result, request, response);
			return null; // response already send
		} else if (result instanceof HttpServletResponse) {
			return null; // response already send
		}
		if (result == null) {
			response.status(HttpServletResponse.SC_NO_CONTENT);
		} else {
			setHeadersFromResultType(result, response);
			return writeValue(result);
		}
		return ""; //jetty understand null as 404 not found
	}

	private static void setHeadersFromResultType(final Object result, final Response response) {
		if (result instanceof List) {
			if (result instanceof DtList && !((DtList) result).getMetaDataNames().isEmpty()) {
				response.type("application/json+list+meta;charset=UTF-8");
			} else {
				response.type("application/json+list;charset=UTF-8");
			}
		} else if (result instanceof DtObject) {
			response.type("application/json+entity:" + result.getClass().getSimpleName() + ";charset=UTF-8");
		} else {
			response.type("application/json;charset=UTF-8");
		}
	}

	private UiContext readInnerBodyValue(final String jsonBody, final List<EndPointParam> endPointParams) throws VSecurityException {
		final List<EndPointParam> innerBodyEndPointParams = new ArrayList<>();
		final Map<String, Type> innerBodyParams = new HashMap<>();
		for (final EndPointParam endPointParam : endPointParams) {
			if (endPointParam.getParamType() == RestParamType.InnerBody || endPointParam.getParamType() == RestParamType.Implicit) {
				innerBodyEndPointParams.add(endPointParam);
				innerBodyParams.put(endPointParam.getName(), endPointParam.getGenericType());
			}
		}
		if (!innerBodyParams.isEmpty()) {
			final UiContext uiContext = jsonReaderEngine.uiContextFromJson(jsonBody, innerBodyParams);
			for (final EndPointParam endPointParam : innerBodyEndPointParams) {
				final Serializable value = uiContext.get(endPointParam.getName());
				if (value instanceof UiObject) {
					postReadUiObject((UiObject<DtObject>) value, endPointParam.getName(), endPointParam, uiSecurityTokenManager);
				} else if (value instanceof UiListDelta) {
					postReadUiListDelta((UiListDelta<DtObject>) value, endPointParam.getName(), endPointParam, uiSecurityTokenManager);
				}
			}
			return uiContext;
		}
		return null;
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
		} else if (Date.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(jsonReaderEngine.fromJson(json, paramClass));
		} else {
			throw new RuntimeException("Unsupported type " + paramClass.getSimpleName());
		}
	}

	private <D> D readQueryValue(final QueryParamsMap queryMap, final EndPointParam endPointParam) throws VSecurityException {
		final Class<D> paramClass = (Class<D>) endPointParam.getType();
		final String paramName = endPointParam.getName();
		if (queryMap == null) {
			return null;
		}
		if (UiListState.class.isAssignableFrom(paramClass) //
				|| DtObject.class.isAssignableFrom(paramClass)) {
			return (D) readValue(convertToJson(queryMap, endPointParam.getName()), endPointParam);
		}
		return readPrimitiveValue(queryMap.get(paramName).value(), paramClass);
	}

	private String convertToJson(final QueryParamsMap queryMap, final String queryPrefix) {
		final String checkedQueryPrefix = queryPrefix.isEmpty() ? "" : queryPrefix + ".";
		final Map<String, Object> queryParams = new HashMap<>();
		for (final Entry<String, String[]> entry : queryMap.toMap().entrySet()) {
			if (entry.getKey().startsWith(checkedQueryPrefix)) {
				final String[] value = entry.getValue();
				final Object simplerValue = value.length == 0 ? null : value.length == 1 ? value[0] : value;
				queryParams.put(entry.getKey().substring(checkedQueryPrefix.length()), simplerValue);
			}
		}
		return jsonWriterEngine.toJson(queryParams);
	}

	private Object readValue(final String json, final EndPointParam endPointParam) throws VSecurityException {
		final Class<?> paramClass = endPointParam.getType();
		final Type paramGenericType = endPointParam.getGenericType();
		if (json == null) {
			return null;
		} else if (String.class.isAssignableFrom(paramClass)) {
			return json;
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return Integer.valueOf(json);
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return Long.valueOf(json);
		} else if (DtObject.class.isAssignableFrom(paramClass)) {
			final UiObject<DtObject> uiObject = jsonReaderEngine.<DtObject> uiObjectFromJson(json, paramGenericType);
			if (uiObject != null) {
				postReadUiObject(uiObject, "", endPointParam, uiSecurityTokenManager);
			}
			return uiObject;
		} else if (DtListDelta.class.isAssignableFrom(paramClass)) {
			final UiListDelta<DtObject> uiListDelta = jsonReaderEngine.<DtObject> uiListDeltaFromJson(json, paramGenericType);
			if (uiListDelta != null) {
				postReadUiListDelta(uiListDelta, "", endPointParam, uiSecurityTokenManager);
			}
			return uiListDelta;
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
			final Option<Serializable> serverSideObject;
			if (endPointParam.isConsumeServerSideToken()) {
				serverSideObject = uiSecurityTokenManager.getAndRemove(accessToken); //TODO if exception : token is consume ?
			} else {
				serverSideObject = uiSecurityTokenManager.get(accessToken);
			}
			if (serverSideObject.isEmpty()) {
				throw new VSecurityException(SERVER_SIDE_MANDATORY); //same message for no ServerSideToken or bad ServerSideToken
			}
			uiObject.setServerSideObject((DtObject) serverSideObject.get());
		}
	}

	private static void postReadUiListDelta(final UiListDelta<DtObject> uiListDelta, final String inputKey, final EndPointParam endPointParam, final UiSecurityTokenManager uiSecurityTokenManager) throws VSecurityException {
		final String prefix = inputKey.length() > 0 ? inputKey + "." : "";
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getCreatesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, endPointParam, uiSecurityTokenManager);
		}
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getUpdatesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, endPointParam, uiSecurityTokenManager);
		}
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getDeletesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, endPointParam, uiSecurityTokenManager);
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
