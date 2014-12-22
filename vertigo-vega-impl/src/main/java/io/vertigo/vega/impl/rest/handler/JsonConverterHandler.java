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
import io.vertigo.vega.rest.engine.UiList;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.model.DtListDelta;
import io.vertigo.vega.rest.model.DtObjectExtended;
import io.vertigo.vega.rest.model.UiListState;
import io.vertigo.vega.token.TokenManager;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

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

	private final TokenManager uiSecurityTokenManager;
	private final EndPointDefinition endPointDefinition;

	enum EncoderType {
		/** Type JSON simple */
		JSON(""),
		/** Type JSON UiContext */
		JSON_UI_CONTEXT("json+uicontext"),
		/** Type JSON list */
		JSON_LIST("json+list:%s"),
		/** Type JSON list with meta */
		JSON_LIST_META("json+list:%s+meta"),
		/** Type JSON entity */
		JSON_ENTITY("json+entity:%s"),
		/** Type JSON entity + meta */
		JSON_ENTITY_META("json+entity:%s+meta");

		private final Pattern contentTypePattern;
		private final String contentType;

		private EncoderType(final String contentType) {
			this.contentType = contentType;
			contentTypePattern = Pattern.compile(contentType.replaceAll("%s", ".+"));
		}

		public String createContentType(final String entityName) {
			return String.format(contentType, entityName);
		}

		public boolean isContentType(final String testedContentType) {
			return contentTypePattern.matcher(testedContentType).find();
		}
	}

	static class EncodedType {
		private final EncoderType encoderType;
		private final String contentType;

		EncodedType(final EncoderType encoderType, final String entityName) {
			this.encoderType = encoderType;
			contentType = encoderType.createContentType(entityName);
		}

		public EncoderType getEncoderType() {
			return encoderType;
		}

		public String getContentType() {
			return contentType;
		}
	}

	JsonConverterHandler(final TokenManager uiSecurityTokenManager, final EndPointDefinition endPointDefinition, final JsonEngine jsonWriterEngine, final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(uiSecurityTokenManager);
		Assertion.checkNotNull(endPointDefinition);
		Assertion.checkNotNull(jsonWriterEngine);
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.uiSecurityTokenManager = uiSecurityTokenManager;
		this.endPointDefinition = endPointDefinition;
		this.jsonWriterEngine = jsonWriterEngine;
		this.jsonReaderEngine = jsonReaderEngine;
	}

	/** {@inheritDoc}  */
	@Override
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
								//we read all InnerBody when we get the first one
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
		if (result == null) {
			response.status(HttpServletResponse.SC_NO_CONTENT);
		} else if (KFileUtil.isKFileResult(result)) {
			KFileUtil.sendKFile(result, request, response);
			return ""; // response already send but can't send null : javaspark understand it as : not consumed here
		} else if (result instanceof HttpServletResponse) {
			return ""; // response already send but can't send null : javaspark understand it as : not consumed here
		} else if (result instanceof String) {
			final String resultString = (String) result;
			final int length = resultString.length();
			Assertion.checkArgument(!(resultString.charAt(0) == '{' && resultString.charAt(length - 1) == '}') && !(resultString.charAt(0) == '[' && resultString.charAt(length - 1) == ']'), "Can't return pre-build json : {0}", resultString);
			response.type("text/plain;charset=UTF-8");
			return result;
		} else {
			final EncodedType encodedType = findEncodedType(result);
			final StringBuilder contentType = new StringBuilder("application/json;charset=UTF-8");
			if (encodedType.getEncoderType() != EncoderType.JSON) {
				contentType.append(";").append(encodedType.getContentType());
			}
			response.type(contentType.toString());
			return writeValue(result, response, encodedType);
		}
		return ""; //jetty understand null as 404 not found
	}

	private EncodedType findEncodedType(final Object result) {
		final EncodedType encodedType;
		if (result instanceof List) {
			if (result instanceof DtList) {
				final DtList<?> dtList = (DtList<?>) result;
				if (hasComplexTypeMeta(dtList)) {
					encodedType = new EncodedType(EncoderType.JSON_LIST_META, dtList.getDefinition().getClassSimpleName());
				} else {
					encodedType = new EncodedType(EncoderType.JSON_LIST, dtList.getDefinition().getClassSimpleName());
				}
			} else {
				encodedType = new EncodedType(EncoderType.JSON_LIST, Object.class.getSimpleName()); //TODO check entityName
			}
		} else if (result instanceof DtObject) {
			encodedType = new EncodedType(EncoderType.JSON_ENTITY, result.getClass().getSimpleName());
		} else if (result instanceof DtObjectExtended<?>) {
			encodedType = new EncodedType(EncoderType.JSON_ENTITY_META, ((DtObjectExtended<?>) result).getInnerObject().getClass().getSimpleName());
		} else if (result instanceof UiContext) {
			encodedType = new EncodedType(EncoderType.JSON_UI_CONTEXT, result.getClass().getSimpleName());
		} else {
			encodedType = new EncodedType(EncoderType.JSON, result.getClass().getSimpleName());
		}
		return encodedType;

	}

	private boolean hasComplexTypeMeta(final DtList<?> dtList) {
		for (final String entry : dtList.getMetaDataNames()) {
			final Option<Serializable> value = dtList.getMetaData(entry, Serializable.class);
			if (value.isDefined()) {
				final Class<?> metaClass = value.get().getClass();
				if (!(metaClass.isPrimitive()
						|| String.class.isAssignableFrom(metaClass)
						|| Integer.class.isAssignableFrom(metaClass)
						|| Long.class.isAssignableFrom(metaClass)
						|| Float.class.isAssignableFrom(metaClass)
						|| Double.class.isAssignableFrom(metaClass)
						|| Date.class.isAssignableFrom(metaClass))) {
					return true;
				}
			}
		}
		return false;
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
		} else if (Float.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(Float.valueOf(json));
		} else if (Double.class.isAssignableFrom(paramClass)) {
			return paramClass.cast(Double.valueOf(json));
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
		if (UiListState.class.isAssignableFrom(paramClass)
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
		} else if (DtList.class.isAssignableFrom(paramClass)) {
			final UiList<DtObject> uiList = jsonReaderEngine.<DtObject> uiListFromJson(json, paramGenericType);
			if (uiList != null) {
				postReadUiList(uiList, "", endPointParam, uiSecurityTokenManager);
			}
			return uiList;
		} else if (DtObjectExtended.class.isAssignableFrom(paramClass)) {
			throw new RuntimeException("Unsupported type DtObjectExtended (use multiple params instead, /*implicit body*/ myDto, @InnerBodyParams others...).");
		} else if (UiContext.class.isAssignableFrom(paramClass)) {
			throw new RuntimeException("Unsupported type UiContext (use @InnerBodyParams instead).");
		} else {
			return jsonReaderEngine.fromJson(json, paramClass);
		}
	}

	private static void postReadUiObject(final UiObject<DtObject> uiObject, final String inputKey, final EndPointParam endPointParam, final TokenManager uiSecurityTokenManager) throws VSecurityException {
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

	private static void postReadUiListDelta(final UiListDelta<DtObject> uiListDelta, final String inputKey, final EndPointParam endPointParam, final TokenManager uiSecurityTokenManager) throws VSecurityException {
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

	private static void postReadUiList(final UiList<DtObject> uiList, final String inputKey, final EndPointParam endPointParam, final TokenManager uiSecurityTokenManager) throws VSecurityException {
		final String prefix = inputKey.length() > 0 ? inputKey + "." : "";
		int index = 0;
		for (final UiObject<DtObject> entry : uiList) {
			final String uiObjectInputKey = prefix + "idx" + index;
			postReadUiObject(entry, uiObjectInputKey, endPointParam, uiSecurityTokenManager);
			index++;
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

	private String writeValue(final Object value, final Response response, final EncodedType encodedType) {
		Assertion.checkNotNull(value);
		//---------------------------------------------------------------------
		final String tokenId;
		if (endPointDefinition.isServerSideSave()) {
			Assertion.checkArgument(DtObject.class.isInstance(value)
					|| DtObjectExtended.class.isInstance(value)
					|| DtList.class.isInstance(value)
					|| UiContext.class.isInstance(value), "Return type can't be ServerSide : {0}", value.getClass().getSimpleName());
			tokenId = uiSecurityTokenManager.put((Serializable) value);
		} else {
			tokenId = null;
		}

		switch (encodedType.getEncoderType()) {
			case JSON:
				return jsonWriterEngine.toJson(value);
			case JSON_ENTITY:
				return toJson(value, Collections.<String, Serializable> emptyMap(), tokenId);
			case JSON_ENTITY_META:
				final DtObjectExtended<?> dtoExtended = (DtObjectExtended<?>) value;
				return toJson(dtoExtended.getInnerObject(), dtoExtended, tokenId);
			case JSON_LIST:
				writeListMetaToHeader((List) value, response);
				return toJson(value, Collections.<String, Serializable> emptyMap(), tokenId);
			case JSON_LIST_META:
				return toJson(value, getListMetas((DtList) value), tokenId);
			case JSON_UI_CONTEXT:
				//TODO build json in jsonWriterEngine
				final StringBuilder sb = new StringBuilder().append("{");
				String sep = "";
				for (final Map.Entry<String, Serializable> entry : ((UiContext) value).entrySet()) {
					sb.append(sep);
					final Serializable entryValue = entry.getValue();
					String encodedValue;
					if (entryValue instanceof DtList) {
						final DtList<?> dtList = (DtList<?>) entryValue;
						encodedValue = writeValue(entryValue, response, new EncodedType(EncoderType.JSON_LIST_META, dtList.getDefinition().getClassSimpleName()));
					} else if (entryValue instanceof DtObject || entryValue instanceof DtObjectExtended) {
						encodedValue = writeValue(entryValue, response, findEncodedType(entryValue));
					} else {
						encodedValue = jsonWriterEngine.toJson(entryValue);
					}
					sb.append("\"").append(entry.getKey()).append("\":").append(encodedValue).append("");
					sep = ", ";
				}
				sb.append("}");
				return sb.toString();
			default:
				throw new IllegalArgumentException("Return type :" + value.getClass().getSimpleName() + " is not supported");
		}
	}

	private void writeListMetaToHeader(final List<?> list, final Response response) {
		if (list instanceof DtList) {
			final DtList<?> dtList = (DtList<?>) list;
			for (final String entry : dtList.getMetaDataNames()) {
				final Option<Serializable> value = dtList.getMetaData(entry, Serializable.class);
				if (value.isDefined()) {
					if (value.get() instanceof String) {
						response.header(entry, (String) value.get()); //TODO escape somethings ?
					} else {
						response.header(entry, jsonWriterEngine.toJson(value.get()));
					}
				}
			}
		} //else nothing, there is no meta on standard list
	}

	private Map<String, Serializable> getListMetas(final DtList<?> dtList) {
		final Map<String, Serializable> metaDatas = new HashMap<>();
		for (final String entry : dtList.getMetaDataNames()) {
			final Option<Serializable> value = dtList.getMetaData(entry, Serializable.class);
			if (value.isDefined()) {
				metaDatas.put(entry, value.get());
			}
		}
		return metaDatas;
	}

	private String toJson(final Object value, final Map<String, Serializable> metaData, final String tokenId) {
		final Map<String, Serializable> metaDataToSend;
		if (tokenId != null) {
			metaDataToSend = new HashMap<>(metaData);
			metaDataToSend.put(JsonEngine.SERVER_SIDE_TOKEN_FIELDNAME, tokenId);
		} else {
			metaDataToSend = metaData;
		}
		return jsonWriterEngine.toJsonWithMeta(value, metaDataToSend, endPointDefinition.getIncludedFields(), endPointDefinition.getExcludedFields());
	}
}
