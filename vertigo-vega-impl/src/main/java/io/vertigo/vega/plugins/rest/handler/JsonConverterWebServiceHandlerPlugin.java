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

import io.vertigo.core.Home;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.vega.impl.rest.WebServiceHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.converter.DefaultJsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.DtListDeltaJsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.DtListJsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.DtObjectJsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.ImplicitJsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.JsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.PrimitiveJsonConverter;
import io.vertigo.vega.plugins.rest.handler.converter.VFileJsonConverter;
import io.vertigo.vega.plugins.rest.handler.reader.BodyJsonReader;
import io.vertigo.vega.plugins.rest.handler.reader.HeaderJsonReader;
import io.vertigo.vega.plugins.rest.handler.reader.InnerBodyJsonReader;
import io.vertigo.vega.plugins.rest.handler.reader.JsonReader;
import io.vertigo.vega.plugins.rest.handler.reader.PathJsonReader;
import io.vertigo.vega.plugins.rest.handler.reader.QueryJsonReader;
import io.vertigo.vega.plugins.rest.handler.reader.RequestJsonReader;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.engine.UiContext;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.WebServiceDefinition;
import io.vertigo.vega.rest.metamodel.WebServiceParam;
import io.vertigo.vega.rest.metamodel.WebServiceParam.WebServiceParamType;
import io.vertigo.vega.rest.model.ExtendedObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;

import com.google.gson.JsonSyntaxException;

/**
 * Params handler.
 * It's an handler barrier : bellow this handler anything is object, over this handler it's json.
 * Extract and Json convert.
 * @author npiedeloup
 */
public final class JsonConverterWebServiceHandlerPlugin implements WebServiceHandlerPlugin {
	private static final Class<? extends JsonConverter>[] JSON_CONVERTER_CLASSES = new Class[] {
			ImplicitJsonConverter.class, PrimitiveJsonConverter.class,
			DtListJsonConverter.class, DtObjectJsonConverter.class, DtListDeltaJsonConverter.class,
			VFileJsonConverter.class, DefaultJsonConverter.class };
	private static final Class<? extends JsonReader<?>>[] JSON_READER_CLASSES = new Class[] {
			BodyJsonReader.class, InnerBodyJsonReader.class, HeaderJsonReader.class,
			PathJsonReader.class, QueryJsonReader.class, RequestJsonReader.class };

	private final JsonEngine jsonWriterEngine;

	private final Map<Class, List<JsonConverter>> jsonConverters = new HashMap<>();
	private final EnumMap<WebServiceParamType, List<JsonReader<?>>> jsonReaders = new EnumMap<>(WebServiceParamType.class);

	/**
	 * encodeType.
	 * jgarnier le 20/05/2015 : les content-type ne doivent pas contenir de ":" mais des "=" pour les paramètres.
	 * http://www.w3.org/Protocols/rfc1341/4_Content-Type.html : Content-Type := type "/" subtype *[";" parameter]
	 * avec : parameter := attribute "=" value
	 */
	enum EncoderType {

		/** Type JSON simple */
		JSON(""),
		/** Type JSON UiContext */
		JSON_UI_CONTEXT("json+uicontext"),
		/** Type JSON list */
		JSON_LIST("json+list=%s"),
		/** Type JSON entity */
		JSON_ENTITY("json+entity=%s");

		private final String HAS_META_MARKER = "+meta";
		private final Pattern contentTypePattern;
		private final String contentType;

		private EncoderType(final String contentType) {
			this.contentType = contentType;
			contentTypePattern = Pattern.compile(contentType.replaceAll("%s", ".+"));
		}

		/**
		 * @param entityName Entity name
		 * @param meta has meta
		 * @return contentType
		 */
		public String createContentType(final String entityName, final boolean meta) {
			return String.format(contentType, entityName) + (meta ? HAS_META_MARKER : "");
		}

		/**
		 * @param testedContentType contentType to test
		 * @return If testedContentType is 'this' EncoderType
		 */
		public boolean isContentType(final String testedContentType) {
			return contentTypePattern.matcher(testedContentType).find();
		}
	}

	/**
	 * EncodedType : encoderType + hasMeta + entityName.
	 */
	static class EncodedType {
		private final EncoderType encoderType;
		private final boolean meta;
		private final String entityName;

		/**
		 * constructor.
		 * @param encoderType encoderType
		 * @param meta has meta
		 * @param entityName entityName
		 */
		EncodedType(final EncoderType encoderType, final boolean meta, final String entityName) {
			this.encoderType = encoderType;
			this.meta = meta;
			this.entityName = entityName;
		}

		/**
		 * @return encoderType
		 */
		public EncoderType getEncoderType() {
			return encoderType;
		}

		/**
		 * @return contentType
		 */
		public boolean hasMeta() {
			return meta;
		}

		/**
		 * @return entityName
		 */
		public String getEntityName() {
			return entityName;
		}

		/**
		 * @return contentType
		 */
		public String obtainContentType() {
			return encoderType.createContentType(entityName, meta);
		}
	}

	/**
	 * @param tokenManager tokenManager (optional)
	 * @param jsonWriterEngine jsonWriterEngine
	 * @param jsonReaderEngine jsonReaderEngine
	 */
	@Inject
	public JsonConverterWebServiceHandlerPlugin(final JsonEngine jsonWriterEngine, final JsonEngine jsonReaderEngine) {
		Assertion.checkNotNull(jsonWriterEngine);
		Assertion.checkNotNull(jsonReaderEngine);
		//-----
		this.jsonWriterEngine = jsonWriterEngine;

		for (final Class<? extends JsonConverter> jsonConverterClass : JSON_CONVERTER_CLASSES) {
			final JsonConverter jsonConverter = Injector.newInstance(jsonConverterClass, Home.getComponentSpace());
			for (final Class inputType : jsonConverter.getSupportedInputs()) {
				List<JsonConverter> jsonConverterBySourceType = jsonConverters.get(inputType);
				if (jsonConverterBySourceType == null) {
					jsonConverterBySourceType = new ArrayList<>();
					jsonConverters.put(inputType, jsonConverterBySourceType);
				}
				jsonConverterBySourceType.add(jsonConverter);
			}
		}

		for (final Class<? extends JsonReader<?>> jsonReaderClass : JSON_READER_CLASSES) {
			final JsonReader<?> jsonReader = Injector.newInstance(jsonReaderClass, Home.getComponentSpace());
			for (final WebServiceParamType restParamType : jsonReader.getSupportedInput()) {
				List<JsonReader<?>> jsonReaderByRestParamType = jsonReaders.get(restParamType);
				if (jsonReaderByRestParamType == null) {
					jsonReaderByRestParamType = new ArrayList<>();
					jsonReaders.put(restParamType, jsonReaderByRestParamType);
				}
				jsonReaderByRestParamType.add(jsonReader);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final WebServiceDefinition webServiceDefinition) {
		return true;
	}

	/** {@inheritDoc}  */
	@Override
	public Object handle(final Request request, final Response response, final WebServiceCallContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		//we can't read body at first : because if it's a multipart request call body() disabled getParts() access.
		for (final WebServiceParam webServiceParam : routeContext.getWebServiceDefinition().getWebServiceParams()) {
			readParameterValue(request, routeContext, webServiceParam);
		}
		final Object result = chain.handle(request, response, routeContext);
		return convertResultToJson(result, request, response, routeContext);
	}

	private void readParameterValue(final Request request, final WebServiceCallContext routeContext, final WebServiceParam webServiceParam) throws VSecurityException {
		try {
			boolean found = false;
			JsonReader jsonReaderToApply = null;
			JsonConverter jsonConverterToApply = null;
			for (final JsonReader jsonReader : jsonReaders.get(webServiceParam.getParamType())) {
				jsonReaderToApply = jsonReader;

				for (final JsonConverter jsonConverter : jsonConverters.get(jsonReader.getSupportedOutput())) {

					if (jsonConverter.canHandle(webServiceParam.getType())) {
						jsonConverterToApply = jsonConverter;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			//-----
			Assertion.checkNotNull(jsonReaderToApply, "Can't parse param {0} of service {1} {2} no compatible JsonReader found for {3}", webServiceParam.getFullName(), routeContext.getWebServiceDefinition().getVerb(), routeContext.getWebServiceDefinition().getPath(), webServiceParam.getParamType());
			Assertion.checkNotNull(jsonConverterToApply, "Can't parse param {0} of service {1} {2} no compatible JsonConverter found for {3} {4}", webServiceParam.getFullName(), routeContext.getWebServiceDefinition().getVerb(), routeContext.getWebServiceDefinition().getPath(), webServiceParam.getParamType(), webServiceParam.getType());
			//-----
			final Object converterSource = jsonReaderToApply.extractData(request, webServiceParam, routeContext);
			if (converterSource != null) { //On ne convertit pas les null
				jsonConverterToApply.populateWebServiceCallContext(converterSource, webServiceParam, routeContext);
			}
			if (webServiceParam.isOptional()) {
				final Object paramValue = routeContext.getParamValue(webServiceParam);
				routeContext.setParamValue(webServiceParam, Option.option(paramValue));
			}
			Assertion.checkNotNull(routeContext.getParamValue(webServiceParam), "RestParam not found : {0}", webServiceParam);
		} catch (final JsonSyntaxException e) {
			throw new JsonSyntaxException("Error parsing param " + webServiceParam.getFullName() + " on service " + routeContext.getWebServiceDefinition().getVerb() + " " + routeContext.getWebServiceDefinition().getPath(), e);
		}
	}

	private String convertResultToJson(final Object result, final Request request, final Response response, final WebServiceCallContext routeContext) {
		if (result == null) {
			//if status was not set, or set to OK we set it to NO_CONTENT
			if (response.raw().getStatus() == HttpServletResponse.SC_OK || response.raw().getStatus() == 0) {
				response.status(HttpServletResponse.SC_NO_CONTENT);
			}
			return ""; //jetty understand null as 404 not found
		} else if (VFileUtil.isVFileResult(result)) {
			VFileUtil.sendVFile(result, request, response);
			return ""; // response already send but can't send null : javaspark understand it as : not consumed here
		} else if (result instanceof HttpServletResponse) {
			Assertion.checkState(((HttpServletResponse) result).isCommitted(), "The httpResponse returned wasn't close. Ensure you have close your streams.");
			//-----
			return ""; // response already send but can't send null : javaspark understand it as : not consumed here
		} else if (result instanceof String) {
			final String resultString = (String) result;
			final int length = resultString.length();
			Assertion.checkArgument(!(resultString.charAt(0) == '{' && resultString.charAt(length - 1) == '}') && !(resultString.charAt(0) == '[' && resultString.charAt(length - 1) == ']'), "Can't return pre-build json : {0}", resultString);
			response.type("text/plain;charset=UTF-8");
			return (String) result;
		} else {
			final EncodedType encodedType = findEncodedType(result);
			final StringBuilder contentType = new StringBuilder("application/json;charset=UTF-8");
			if (encodedType.getEncoderType() != EncoderType.JSON) {
				contentType.append(";").append(encodedType.obtainContentType());
			}
			response.type(contentType.toString());
			return writeValue(result, response, routeContext.getWebServiceDefinition());
		}
	}

	private static EncodedType findEncodedType(final Object value) {
		final EncodedType encodedType;
		if (value instanceof DtList) {
			final DtList<?> dtList = (DtList<?>) value;
			encodedType = new EncodedType(EncoderType.JSON_LIST, hasComplexTypeMeta(dtList), dtList.getDefinition().getClassSimpleName());
		} else if (value instanceof List) {
			final String entityName = ((List) value).isEmpty() ? Object.class.getSimpleName() : ((List) value).get(0).getClass().getSimpleName();
			encodedType = new EncodedType(EncoderType.JSON_LIST, false, entityName);
		} else if (value instanceof DtObject) {
			encodedType = new EncodedType(EncoderType.JSON_ENTITY, false, value.getClass().getSimpleName());
		} else if (value instanceof UiContext) {
			encodedType = new EncodedType(EncoderType.JSON_UI_CONTEXT, false, value.getClass().getSimpleName());
		} else if (value instanceof ExtendedObject<?>) {
			//ce type n'est qu'un conteneur de l'objet sous jacent, lorsqu'il ne contient que le tokenId il ne modifie par le type mime
			final EncodedType innerEncodedType = findEncodedType(((ExtendedObject) value).getInnerObject());
			//si le type interne n'as pas de meta, et que le ExtendedObject contient d'autres metas que le seul serverSideToken, on change le type mime
			if (!innerEncodedType.hasMeta()
					&& !(((ExtendedObject) value).isEmpty()
					|| (((ExtendedObject) value).size() == 1 && ((ExtendedObject) value).containsKey(JsonEngine.SERVER_SIDE_TOKEN_FIELDNAME)))) {
				encodedType = new EncodedType(innerEncodedType.getEncoderType(), true, innerEncodedType.getEntityName());
			} else {
				encodedType = innerEncodedType;
			}
		} else {
			encodedType = new EncodedType(EncoderType.JSON, false, value.getClass().getSimpleName());
		}
		return encodedType;

	}

	private static boolean hasComplexTypeMeta(final DtList<?> dtList) {
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

	private String writeValue(final Object value, final Response response, final WebServiceDefinition webServiceDefinition) {
		Assertion.checkNotNull(value);
		//-----
		if (value instanceof DtList && hasComplexTypeMeta((DtList) value)) {
			return toJson(value, getListMetas((DtList) value), webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else if (value instanceof List) {
			writeListMetaToHeader((List) value, response);
			return toJson(value, Collections.<String, Serializable> emptyMap(), webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else if (value instanceof DtObject) {
			return toJson(value, Collections.<String, Serializable> emptyMap(), webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else if (value instanceof UiContext) {
			//TODO build json in jsonWriterEngine
			final StringBuilder sb = new StringBuilder().append("{");
			String sep = "";
			for (final Map.Entry<String, Serializable> entry : ((UiContext) value).entrySet()) {
				sb.append(sep);
				final String encodedValue = writeValue(entry.getValue(), response, webServiceDefinition);
				sb.append("\"").append(entry.getKey()).append("\":").append(encodedValue).append("");
				sep = ", ";
			}
			sb.append("}");
			return sb.toString();
		} else if (value instanceof ExtendedObject<?>) {
			final ExtendedObject<?> extendedObject = (ExtendedObject<?>) value;
			return toJson(extendedObject.getInnerObject(), extendedObject, webServiceDefinition.getIncludedFields(), webServiceDefinition.getExcludedFields());
		} else {
			return jsonWriterEngine.toJson(value);
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

	private static Map<String, Serializable> getListMetas(final DtList<?> dtList) {
		final Map<String, Serializable> metaDatas = new HashMap<>();
		for (final String entry : dtList.getMetaDataNames()) {
			final Option<Serializable> value = dtList.getMetaData(entry, Serializable.class);
			if (value.isDefined()) {
				metaDatas.put(entry, value.get());
			}
		}
		return metaDatas;
	}

	private String toJson(final Object value, final Map<String, Serializable> metaData, final Set<String> includedFields, final Set<String> excludedFields) {
		return jsonWriterEngine.toJsonWithMeta(value, metaData, includedFields, excludedFields);
	}
}
