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
package io.vertigo.vega.rest.engine;

import io.vertigo.core.component.ComponentInfo;
import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.lang.Option;
import io.vertigo.core.metamodel.DefinitionReference;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.vega.rest.EndPointTypeHelper;
import io.vertigo.vega.rest.model.DtListDelta;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

/**
 * @author pchretien, npiedeloup
 */
public final class GoogleJsonEngine implements JsonEngine {
	private final Gson gson = createGson();

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object data) {
		return toJson(data, Collections.<String> emptySet(), Collections.<String> emptySet());
	}

	/** {@inheritDoc} */
	@Override
	public String toJson(final Object data, final Set<String> includedFields, final Set<String> excludedFields) {
		final JsonElement jsonElement = gson.toJsonTree(data);
		filterFields(jsonElement, includedFields, excludedFields);
		return gson.toJson(jsonElement);
	}

	/** {@inheritDoc} */
	@Override
	public String toJsonWithTokenId(final Object data, final String tokenId, final Set<String> includedFields, final Set<String> excludedFields) {
		if (data instanceof List) {
			final JsonObject jsonObject = new JsonObject();
			final JsonElement jsonElement = gson.toJsonTree(data);
			filterFields(jsonElement, includedFields, excludedFields);
			jsonObject.add(LIST_VALUE_FIELDNAME, jsonElement);
			jsonObject.addProperty(SERVER_SIDE_TOKEN_FIELDNAME, tokenId);
			return gson.toJson(jsonObject);
		}
		final JsonElement jsonElement = gson.toJsonTree(data);
		filterFields(jsonElement, includedFields, excludedFields);
		jsonElement.getAsJsonObject().addProperty(SERVER_SIDE_TOKEN_FIELDNAME, tokenId);
		return gson.toJson(jsonElement);
	}

	private void filterFields(final JsonElement jsonElement, final Set<String> includedFields, final Set<String> excludedFields) {
		if (jsonElement.isJsonArray()) {
			final JsonArray jsonArray = jsonElement.getAsJsonArray();
			for (final JsonElement jsonSubElement : jsonArray) {
				filterFields(jsonSubElement, includedFields, excludedFields);
			}
		} else if (jsonElement.isJsonObject()) {
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			for (final String excludedField : excludedFields) {
				jsonObject.remove(excludedField);
			}
			if (!includedFields.isEmpty()) {
				final Set<String> notIncludedFields = new HashSet<>();
				for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					if (!includedFields.contains(entry.getKey())) {
						notIncludedFields.add(entry.getKey());
					}
				}
				for (final String notIncludedField : notIncludedFields) {
					jsonObject.remove(notIncludedField);
				}
			}

		}
		//else Primitive : no exclude
	}

	/** {@inheritDoc} */
	@Override
	public String toJsonError(final Throwable th) {
		final String exceptionMessage = th.getMessage() != null ? th.getMessage() : th.getClass().getSimpleName();
		return "{\"globalErrorMessages\":[\"" + exceptionMessage + "\"]}"; //TODO +stack;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends Object> D fromJson(final String json, final Type paramType) {
		return gson.fromJson(json, paramType);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiObject<D> uiObjectFromJson(final String json, final Type paramType) {
		final Type typeOfDest = createParameterizedType(UiObject.class, paramType);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> UiListDelta<D> uiListDeltaFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
		final Type typeOfDest = createParameterizedType(UiListDelta.class, dtoClass);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public UiContext uiContextFromJson(final String json, final Map<String, Type> paramTypes) {
		final UiContext result = new UiContext();
		try {
			final JsonElement jsonElement = new JsonParser().parse(json);
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			for (final Entry<String, Type> entry : paramTypes.entrySet()) {
				final String key = entry.getKey();
				final Type paramType = entry.getValue();
				final JsonElement jsonSubElement = jsonObject.get(key);

				final Serializable value;
				if (EndPointTypeHelper.isAssignableFrom(DtObject.class, paramType)) {
					final Type typeOfDest = createParameterizedType(UiObject.class, paramType);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (EndPointTypeHelper.isAssignableFrom(DtListDelta.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
					final Type typeOfDest = createParameterizedType(UiListDelta.class, dtoClass);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else {
					value = (Serializable) gson.fromJson(jsonSubElement, paramType);
				}
				result.put(key, value);
			}
			return result;
		} catch (final IllegalStateException e) {
			throw new JsonSyntaxException("JsonObject expected", e);
		}
	}

	private static Type createParameterizedType(final Class<?> rawClass, final Type paramType) {
		final Type[] typeArguments = { paramType };
		final Type typeOfDest = new ParameterizedType() {

			@Override
			public Type[] getActualTypeArguments() {
				return typeArguments;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}

			@Override
			public Type getRawType() {
				return rawClass;
			}
		};
		return typeOfDest;
	}

	static class UiObjectDeserializer<D extends DtObject> implements JsonDeserializer<UiObject<D>> {
		public UiObject<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
			final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
			final JsonObject jsonObject = json.getAsJsonObject();
			final D inputDto = context.deserialize(jsonObject, dtoClass);
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtoClass);
			final Set<String> dtFields = getFieldNames(dtDefinition);
			final Set<String> modifiedFields = new HashSet<>();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				final String fieldName = entry.getKey();
				if (dtFields.contains(fieldName)) { //we only keep fields of this dtObject
					modifiedFields.add(fieldName);
				}
			}
			final UiObject<D> uiObject = new UiObject<>(inputDto, modifiedFields);
			if (jsonObject.has(SERVER_SIDE_TOKEN_FIELDNAME)) {
				uiObject.setServerSideToken(jsonObject.get(SERVER_SIDE_TOKEN_FIELDNAME).getAsString());
			}
			return uiObject;
		}
	}

	static Set<String> getFieldNames(final DtDefinition dtDefinition) {
		final Set<String> dtFieldNames = new HashSet<>();
		for (final DtField dtField : dtDefinition.getFields()) {
			dtFieldNames.add(StringUtil.constToCamelCase(dtField.getName(), false));
		}
		return dtFieldNames;
	}

	/*  {@inheritDoc}
	 *  TODO
	 *  public <D extends DtObject> UiList<D> uiListFromJson(final String json, final Class<D> paramClass) {
		final Type[] typeArguments = { paramClass };
		final Type typeOfDest = new ParameterizedType() {

			@Override
			public Type[] getActualTypeArguments() {
				return typeArguments;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}

			@Override
			public Type getRawType() {
				return UiList.class;
			}
		};
		return gson.fromJson(json, typeOfDest);
	}*/

	static class UiListDeltaDeserializer<D extends DtObject> implements JsonDeserializer<UiListDelta<D>> {
		public UiListDelta<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
			final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
			final Type uiObjectType = createParameterizedType(UiObject.class, dtoClass);
			final JsonObject jsonObject = json.getAsJsonObject();

			final Map<String, UiObject<D>> collCreates = parseUiObjectMap(jsonObject, "collCreates", uiObjectType, context);
			final Map<String, UiObject<D>> collUpdates = parseUiObjectMap(jsonObject, "collUpdates", uiObjectType, context);
			final Map<String, UiObject<D>> collDeletes = parseUiObjectMap(jsonObject, "collDeletes", uiObjectType, context);

			final UiListDelta<D> uiListDelta = new UiListDelta<>(dtoClass, collCreates, collUpdates, collDeletes);
			return uiListDelta;
		}

		private Map<String, UiObject<D>> parseUiObjectMap(final JsonObject jsonObject, final String propertyName, final Type uiObjectType, final JsonDeserializationContext context) {
			final Map<String, UiObject<D>> uiObjectMap = new HashMap<>();
			final JsonObject jsonUiObjectMap = jsonObject.getAsJsonObject(propertyName);
			if (jsonUiObjectMap != null) {
				for (final Entry<String, JsonElement> entry : jsonUiObjectMap.entrySet()) {
					final String entryName = entry.getKey();
					final UiObject<D> inputDto = context.deserialize(entry.getValue(), uiObjectType);
					uiObjectMap.put(entryName, inputDto);
				}
			}
			return uiObjectMap;
		}
	}

	//	 TODO
	// static class UiListDeserializer implements JsonDeserializer<UiList<?>> {
	//
	//		public UiList<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
	//			final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
	//			final Class dtoClass = (Class) typeParameters[0]; // Id has only one parameterized type T
	//			final JsonObject jsonObject = json.getAsJsonObject();
	//			final DtObject inputDto = context.deserialize(jsonObject, dtoClass);
	//
	//			final Set<String> modifiedFields = new HashSet<>();
	//			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
	//				final String fieldName = entry.getKey();
	//				if (!SERVER_SIDE_TOKEN_FIELDNAME.equals(fieldName)) {
	//					modifiedFields.add(fieldName);
	//				}
	//			}
	//			final UiList<DtObject> uiList = new UiList(dtoClass);
	//			if (jsonObject.has(SERVER_SIDE_TOKEN_FIELDNAME)) {
	//				uiList.setServerSideToken(jsonObject.get(SERVER_SIDE_TOKEN_FIELDNAME).getAsString());
	//			}
	//			return uiList;
	//		}
	//	}

	private static Gson createGson() {
		return new GsonBuilder()//
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") //
				.setPrettyPrinting()//
				//TODO  registerTypeAdapter(String.class, new EmptyStringAsNull<>())// add "" <=> null 
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(UiObject.class, new UiObjectDeserializer<>())//
				.registerTypeAdapter(UiListDelta.class, new UiListDeltaDeserializer<>())//
				.registerTypeAdapter(ComponentInfo.class, new JsonSerializer<ComponentInfo>() {
					@Override
					public JsonElement serialize(final ComponentInfo componentInfo, final Type typeOfSrc, final JsonSerializationContext context) {
						final JsonObject jsonObject = new JsonObject();
						jsonObject.add(componentInfo.getTitle(), context.serialize(componentInfo.getValue()));
						return jsonObject;
					}
				})//
				.registerTypeAdapter(List.class, new JsonSerializer<List>() {

					@Override
					public JsonElement serialize(final List src, final Type typeOfSrc, final JsonSerializationContext context) {
						if (src.isEmpty()) {
							return null;
						}
						return context.serialize(src);
					}
				})//
				.registerTypeAdapter(Map.class, new JsonSerializer<Map>() {

					@Override
					public JsonElement serialize(final Map src, final Type typeOfSrc, final JsonSerializationContext context) {
						if (src.isEmpty()) {
							return null;
						}
						return context.serialize(src);
					}
				})//
				.registerTypeAdapter(DefinitionReference.class, new JsonSerializer<DefinitionReference>() {

					@Override
					public JsonElement serialize(final DefinitionReference src, final Type typeOfSrc, final JsonSerializationContext context) {
						return context.serialize(src.get().getName());
					}
				})//
				.registerTypeAdapter(Option.class, new JsonSerializer<Option>() {

					@Override
					public JsonElement serialize(final Option src, final Type typeOfSrc, final JsonSerializationContext context) {
						if (src.isDefined()) {
							return context.serialize(src.get());
						}
						return null; //rien
					}
				})//
				.registerTypeAdapter(Class.class, new JsonSerializer<Class>() {

					@Override
					public JsonElement serialize(final Class src, final Type typeOfSrc, final JsonSerializationContext context) {
						return new JsonPrimitive(src.getName());
					}
				})//
				.addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(final FieldAttributes arg0) {
						if (arg0.getAnnotation(JsonExclude.class) != null) {
							return true;
						}
						return false;
					}

					@Override
					public boolean shouldSkipClass(final Class<?> arg0) {
						return false;
					}
				}).create();
	}
}
