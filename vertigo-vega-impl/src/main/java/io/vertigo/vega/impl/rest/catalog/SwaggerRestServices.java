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
package io.vertigo.vega.impl.rest.catalog;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Option;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.vega.rest.EndPointTypeHelper;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.PathParam;
import io.vertigo.vega.rest.stereotype.SessionLess;
import io.vertigo.vega.rest.validation.UiMessageStack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Swagger RestService to list services published.
 * @see "https://github.com/wordnik/swagger-spec/blob/master/versions/2.0.md"
 * @author npiedeloup (22 juil. 2014 11:12:02)
 */
public final class SwaggerRestServices implements RestfulService {

	private final Map<String, Object> definitions = new LinkedHashMap<>();

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerApi")
	public Map<String, Object> getSwapperApi() {
		return createSwagger();
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi")
	public void getSwapperUi(final HttpServletResponse response) throws IOException {
		response.sendRedirect("./swaggerUi/index.html");
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi/")
	public void getSwapperUiEmpty(final HttpServletResponse response) throws IOException {
		response.sendRedirect("./index.html");
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi/{resourceUrl}")
	public void getSwapperUi(@PathParam("resourceUrl") final String resourceUrl, final HttpServletResponse response) throws IOException {
		if (resourceUrl.isEmpty()) {
			response.sendRedirect("./index.html");
		}
		final URL url = SwaggerRestServices.class.getResource("/swagger-site/" + resourceUrl);
		sendFile(url, resolveContentType(resourceUrl), response);
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/swaggerUi/{resourcePathUrl}/{resourceUrl}")
	public void getSwapperUi(@PathParam("resourcePathUrl") final String resourcePathUrl, @PathParam("resourceUrl") final String resourceUrl, final HttpServletResponse response) throws IOException {
		final URL url = SwaggerRestServices.class.getResource("/swagger-site/" + resourcePathUrl + "/" + resourceUrl);
		sendFile(url, resolveContentType(resourceUrl), response);
	}

	private void sendFile(final URL url, final String contentType, final HttpServletResponse response) throws IOException {
		if (url != null) {
			final URLConnection connection = url.openConnection();
			connection.connect();
			response.setContentLength(connection.getContentLength());
			response.setDateHeader("Last-Modified", connection.getLastModified());
			response.setContentType(contentType != null ? contentType : connection.getContentType());

			try (final InputStream input = connection.getInputStream()) {
				try (final BufferedInputStream bInput = new BufferedInputStream(input)) {
					try (final OutputStream output = response.getOutputStream()) {
						copy(bInput, output);
					}
				}
			}
		} else {
			response.setStatus(404);
		}
	}

	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			read = in.read(bytes);
		}
	}

	private String resolveContentType(final String resourceUrl) {
		if (resourceUrl.endsWith(".html")) {
			return "text/html";
		} else if (resourceUrl.endsWith(".css")) {
			return "text/css";
		} else if (resourceUrl.endsWith(".js")) {
			return "application/x-javascript";
		} else if (resourceUrl.endsWith(".png")) {
			return "image/png";
		} else if (resourceUrl.endsWith(".gif")) {
			return "image/gif";
		}
		return null;
	}

	private Map<String, Object> createSwagger() {
		final Map<String, Object> swagger = new LinkedHashMap<>();
		swagger.put("swagger", 2.0);
		swagger.put("info", createInfoObject());

		//host, basePath, schemes, consumes, produces
		swagger.put("paths", createPathsObject());
		//definitions, parameters, responses, security, tags, externalDocs
		putIfNotEmpty(swagger, "definitions", definitions);
		return swagger;
	}

	private Map<String, Object> createPathsObject() {
		final Map<String, Object> paths = new LinkedHashMap<>();
		final Collection<EndPointDefinition> endPointDefCollection = Home.getDefinitionSpace().getAll(EndPointDefinition.class);
		for (final EndPointDefinition endPointDefinition : endPointDefCollection) {
			final Map<String, Object> pathItem = (Map<String, Object>) paths.get(endPointDefinition.getPath());
			if (pathItem != null) {
				pathItem.putAll(createPathItemObject(endPointDefinition));
				paths.put(endPointDefinition.getPath(), pathItem);
			} else {
				paths.put(endPointDefinition.getPath(), createPathItemObject(endPointDefinition));
			}
		}
		return paths;
	}

	private Map<String, Object> createPathItemObject(final EndPointDefinition endPointDefinition) {
		final Map<String, Object> pathItem = new LinkedHashMap<>();
		pathItem.put(endPointDefinition.getVerb().name().toLowerCase(), createOperationObject(endPointDefinition));
		return pathItem;
	}

	private Map<String, Object> createOperationObject(final EndPointDefinition endPointDefinition) {
		final Map<String, Object> operation = new LinkedHashMap<>();
		operation.put("summary", endPointDefinition.getMethod().getName());
		final StringBuilder description = new StringBuilder();
		if (!endPointDefinition.getDoc().isEmpty()) {
			description.append(endPointDefinition.getDoc());
			description.append("<br/>");
		}
		if (endPointDefinition.isServerSideSave()) {
			description.append("This operation keep a full ServerSide state of returned object");
			description.append("<br/>");
		}
		putIfNotEmpty(operation, "description", description.toString());
		operation.put("operationId", endPointDefinition.getName());
		putIfNotEmpty(operation, "parameters", createParametersArray(endPointDefinition));
		putIfNotEmpty(operation, "responses", createResponsesObject(endPointDefinition));
		putIfNotEmpty(operation, "tags", createTagsArray(endPointDefinition));
		return operation;
	}

	private void putIfNotEmpty(final Map<String, Object> entity, final String key, final Object value) {
		if (value instanceof List && ((List) value).isEmpty()) {
			return;
		}
		if (value != null) {
			entity.put(key, value);
		}
	}

	private Map<String, Object> createResponsesObject(final EndPointDefinition endPointDefinition) {
		final Map<String, Object> responses = new LinkedHashMap<>();
		final Map<String, Object> headers = createResponsesHeaders(endPointDefinition);

		final Type returnType = endPointDefinition.getMethod().getGenericReturnType();
		if (void.class.isAssignableFrom(endPointDefinition.getMethod().getReturnType())) {
			responses.put("204", createResponseObject("No content", returnType, headers));
		} else if (endPointDefinition.getMethod().getName().startsWith("create")) {
			responses.put("201", createResponseObject("Created", returnType, headers));
		} else {
			responses.put("200", createResponseObject("Success", returnType, headers));
		}
		if (!endPointDefinition.getEndPointParams().isEmpty()) {
			responses.put("400", createResponseObject("Bad request : parsing error (json, number, date, ...)", ErrorMessage.class, headers));
		}
		if (endPointDefinition.isNeedAuthentification()) {
			//endPointDefinition.isNeedSession() don't mean that session is mandatory, it just say to create a session
			responses.put("401", createResponseObject("Unauthorized : no valid session", ErrorMessage.class, headers));
			responses.put("403", createResponseObject("Forbidden : not enought rights", ErrorMessage.class, headers));
		}
		if (!endPointDefinition.getEndPointParams().isEmpty()) {
			responses.put("422", createResponseObject("Unprocessable entity : validations or business error", UiMessageStack.class, headers));
		}
		responses.put("429", createResponseObject("Too many request : anti spam security (must wait for next time window)", ErrorMessage.class, headers));
		responses.put("500", createResponseObject("Internal server error", ErrorMessage.class, headers));
		return responses;
	}

	private Map<String, Object> createResponsesHeaders(final EndPointDefinition endPointDefinition) {
		final Map<String, Object> headers = new LinkedHashMap<>();
		if (endPointDefinition.isAccessTokenPublish()) {
			headers.put("x-access-token", createSchemaObject(String.class));
		}
		return headers;
	}

	final class ErrorMessage {
		private final List<String> globalErrorMessages = Collections.emptyList();

		public List<String> getGlobalErrorMessages() {
			return globalErrorMessages;
		}
	}

	private Map<String, Object> createResponseObject(final String description, final Type returnType, final Map<String, Object> headers) {
		final Map<String, Object> response = new LinkedHashMap<>();
		response.put("description", description);
		putIfNotEmpty(response, "schema", createSchemaObject(returnType));
		putIfNotEmpty(response, "headers", headers);
		return response;
	}

	private Map<String, Object> createSchemaObject(final Type type) {
		final Map<String, Object> schema = new LinkedHashMap<>();
		final Class<?> objectClass = EndPointTypeHelper.castAsClass(type);
		final String[] typeAndFormat = toSwaggerType(objectClass);
		schema.put("type", typeAndFormat[0]);
		if (typeAndFormat[1] != null) {
			schema.put("format", typeAndFormat[1]);
		}
		if (EndPointTypeHelper.isAssignableFrom(void.class, type)) {
			return null;
		} else if (EndPointTypeHelper.isAssignableFrom(List.class, type)) {
			final Type itemsType = ((ParameterizedType) type).getActualTypeArguments()[0]; //we known that List has one parameterized type
			schema.put("items", createSchemaObject(itemsType));
		} else if ("object".equals(typeAndFormat[0])) {
			final String objectName;
			final Class<?> parameterClass;
			if (type instanceof ParameterizedType //
					&& ((ParameterizedType) type).getActualTypeArguments().length == 1 //
					&& !(((ParameterizedType) type).getActualTypeArguments()[0] instanceof WildcardType)) {
				final Type itemsType = ((ParameterizedType) type).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
				parameterClass = EndPointTypeHelper.castAsClass(itemsType);
				objectName = objectClass.getSimpleName() + "&lt;" + parameterClass.getSimpleName() + "&gt;";
			} else {
				objectName = objectClass.getSimpleName();
				parameterClass = null;
			}
			schema.put("$ref", objectName);
			schema.remove("type");
			if (!definitions.containsKey(objectName)) {
				final Map<String, Object> definition = new LinkedHashMap<>();
				if (DtObject.class.isAssignableFrom(objectClass)) {
					final Class<? extends DtObject> dtClass = (Class<? extends DtObject>) objectClass;
					appendPropertiesDtObject(definition, dtClass);
				} else {
					appendPropertiesObject(definition, objectClass, parameterClass);
				}
				definitions.put(objectName, definition);
			}
		}
		return schema;
	}

	private void appendPropertiesDtObject(final Map<String, Object> entity, final Class<? extends DtObject> objectClass) {
		//can't be a primitive nor array nor DtListDelta
		final Map<String, Object> properties = new LinkedHashMap<>();
		final List<String> enums = new ArrayList<>(); //mandatory fields
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(objectClass);
		for (final DtField dtField : dtDefinition.getFields()) {
			final String fieldName = StringUtil.constToCamelCase(dtField.getName(), false);
			final Map<String, Object> fieldSchema = createSchemaObject(dtField.getDomain().getDataType().getJavaClass());
			fieldSchema.put("title", dtField.getLabel().getDisplay());
			fieldSchema.put("required", dtField.isNotNull());
			if (dtField.isNotNull()) {
				enums.add(fieldName);
			}
			properties.put(fieldName, fieldSchema);
		}
		putIfNotEmpty(entity, "enum", enums);
		putIfNotEmpty(entity, "properties", properties);
	}

	private void appendPropertiesObject(final Map<String, Object> entity, final Type type, final Class<? extends Object> parameterClass) {
		final Class<?> objectClass = EndPointTypeHelper.castAsClass(type);
		//can't be a primitive nor array nor DtListDelta
		final Map<String, Object> properties = new LinkedHashMap<>();
		final List<String> enums = new ArrayList<>(); //mandatory fields
		for (final Field field : objectClass.getDeclaredFields()) {
			if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | 0x1000)) == 0) { //0x1000 is for synthetic field (excludes)
				final Type fieldType = field.getGenericType();
				Type usedFieldType = fieldType;
				if (fieldType instanceof ParameterizedType) {
					final Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
					if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof TypeVariable) {
						final Type[] resolvedTypeArguments = new Type[] { parameterClass };
						usedFieldType = new ParameterizedType() {
							public Type[] getActualTypeArguments() {
								return resolvedTypeArguments;
							}

							public Type getRawType() {
								return ((ParameterizedType) fieldType).getRawType();
							}

							public Type getOwnerType() {
								return ((ParameterizedType) fieldType).getOwnerType();
							}

						};
					}
				} else if (fieldType instanceof TypeVariable) {
					usedFieldType = parameterClass;
				}
				final Map<String, Object> fieldSchema = createSchemaObject(usedFieldType);
				//fieldSchema.put("title", field.getName());
				if ((field.getModifiers() & (Modifier.FINAL)) != 0 //
						&& !Option.class.isAssignableFrom(field.getType())) {
					//fieldSchema.put("required", true);
					enums.add(field.getName());
				}
				properties.put(field.getName(), fieldSchema);
			}
		}
		putIfNotEmpty(entity, "enum", enums);
		putIfNotEmpty(entity, "properties", properties);
	}

	private List<String> createTagsArray(final EndPointDefinition endPointDefinition) {
		final List<String> tags = new ArrayList<>();
		tags.add(endPointDefinition.getMethod().getDeclaringClass().getSimpleName());
		return tags;
	}

	private List<Map<String, Object>> createParametersArray(final EndPointDefinition endPointDefinition) {
		final List<Map<String, Object>> parameters = new ArrayList<>();
		for (final EndPointParam endPointParams : endPointDefinition.getEndPointParams()) {
			final Map<String, Object> parameter = createParameterObject(endPointParams, endPointDefinition);
			if (parameter != null) {
				parameters.add(parameter);
			}
		}
		if (endPointDefinition.isAccessTokenMandatory()) {
			final Map<String, Object> parameter = new LinkedHashMap<>();
			parameter.put("name", "x-access-token");
			parameter.put("in", "header");
			parameter.put("description", "Security access token, must be sent to allow operation.");
			parameter.put("required", "true");
			parameter.put("type", "string");
			parameters.add(parameter);
		}
		return parameters;
	}

	private Map<String, Object> createParameterObject(final EndPointParam endPointParams, final EndPointDefinition endPointDefinition) {

		final String inValue;
		final String nameValue;
		String description = null;
		switch (endPointParams.getParamType()) {
			case Body:
				inValue = "body";
				nameValue = "body";
				break;
			case Implicit:
				return null; //no public parameter
			case InnerBody:
				inValue = "body";
				nameValue = "body";
				description = "InnerBody:" + endPointParams.getName();
				break;
			case Path:
				inValue = "path";
				nameValue = endPointParams.getName();
				break;
			case Query:
				inValue = endPointDefinition.getVerb() == Verb.GET ? "query" : "formData";
				nameValue = endPointParams.getName();
				break;
			case Header:
				inValue = "header";
				nameValue = endPointParams.getName();
				break;
			default:
				throw new RuntimeException("Unsupported type : " + endPointParams.getParamType());
		}

		final Map<String, Object> parameter = new LinkedHashMap<>();
		parameter.put("name", nameValue);
		parameter.put("in", inValue);
		putIfNotEmpty(parameter, "description", description);
		parameter.put("required", "true");
		if (endPointParams.getParamType() == RestParamType.Body || endPointParams.getParamType() == RestParamType.InnerBody) {
			parameter.put("schema", createSchemaObject(endPointParams.getGenericType()));
		} else {
			final String[] typeAndFormat = toSwaggerType(endPointParams.getType());
			parameter.put("type", typeAndFormat[0]);
			if (typeAndFormat[1] != null) {
				parameter.put("format", typeAndFormat[1]);
			}
			//items, collectionFormat, default, maximum, ...
		}
		return parameter;
	}

	private String[] toSwaggerType(final Class paramClass) {
		if (String.class.isAssignableFrom(paramClass)) {
			return new String[] { "string", null };
		} else if (boolean.class.isAssignableFrom(paramClass) || Boolean.class.isAssignableFrom(paramClass)) {
			return new String[] { "boolean", null };
		} else if (int.class.isAssignableFrom(paramClass) || Integer.class.isAssignableFrom(paramClass)) {
			return new String[] { "integer", "int32" };
		} else if (long.class.isAssignableFrom(paramClass) || Long.class.isAssignableFrom(paramClass)) {
			return new String[] { "integer", "int64" };
		} else if (float.class.isAssignableFrom(paramClass) || Float.class.isAssignableFrom(paramClass)) {
			return new String[] { "integer", "int32" };
		} else if (double.class.isAssignableFrom(paramClass) || Double.class.isAssignableFrom(paramClass)) {
			return new String[] { "integer", "int64" };
		} else if (Date.class.isAssignableFrom(paramClass)) {
			return new String[] { "string", "date-time" };
		} else if (KFile.class.isAssignableFrom(paramClass)) {
			return new String[] { "file", null };
		} else if (List.class.isAssignableFrom(paramClass) || Collection.class.isAssignableFrom(paramClass)) {
			return new String[] { "array", null };
		} else {
			return new String[] { "object", null };
		}
	}

	private Map<String, Object> createInfoObject() {
		final Map<String, Object> infoObject = new LinkedHashMap<>();
		infoObject.put("title", "MySwaggerAPI Tester");
		//description, termOfService, contact
		infoObject.put("license", createLicense());
		infoObject.put("version", "1.0");
		return infoObject;
	}

	private Map<String, Object> createLicense() {
		final Map<String, Object> licence = new LinkedHashMap<>();
		licence.put("name", "Apache 2.0");
		licence.put("url", "http://www.apache.org/licenses/LICENSE-2.0.html");
		return licence;
	}
}
