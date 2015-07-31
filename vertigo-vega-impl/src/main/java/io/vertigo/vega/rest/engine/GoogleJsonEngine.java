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

import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.Option;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.rest.EndPointTypeUtil;
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
		return gson.toJson(data);
	}

	/** {@inheritDoc} */
	@Override
	public String toJsonWithMeta(final Object data, final Map<String, Serializable> metaDatas, final Set<String> includedFields, final Set<String> excludedFields) {
		final JsonElement jsonValue = gson.toJsonTree(data);
		filterFields(jsonValue, includedFields, excludedFields);

		if (metaDatas.isEmpty() && data instanceof List) {
			return gson.toJson(jsonValue); //only case where result wasn't an object
		}

		final JsonObject jsonResult;
		if (data instanceof List) {
			jsonResult = new JsonObject();
			jsonResult.add(LIST_VALUE_FIELDNAME, jsonValue);
		} else {
			jsonResult = jsonValue.getAsJsonObject();
		}
		final JsonObject jsonMetaData = gson.toJsonTree(metaDatas).getAsJsonObject();
		for (final Entry<String, JsonElement> entry : jsonMetaData.entrySet()) {
			jsonResult.add(entry.getKey(), entry.getValue());
		}
		return gson.toJson(jsonResult);
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
		return gson.toJson(Collections.singletonMap("globalErrors", Collections.singletonList(exceptionMessage)));//TODO +stack;
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
	/*@Override
	public <D extends DtObject> UiObjectExtended<D> uiObjectExtendedFromJson(final String json, final Type paramType) {
		final Type typeOfDest = createParameterizedType(UiObjectExtended.class, paramType);
		return gson.fromJson(json, typeOfDest);
	}*/

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiListDelta<D> uiListDeltaFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
		final Type typeOfDest = createParameterizedType(UiListDelta.class, dtoClass);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiList<D> uiListFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtList has one parameterized type
		final Type typeOfDest = createParameterizedType(UiList.class, dtoClass);
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
				if (EndPointTypeUtil.isAssignableFrom(DtObject.class, paramType)) {
					final Type typeOfDest = createParameterizedType(UiObject.class, paramType);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (EndPointTypeUtil.isAssignableFrom(DtListDelta.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
					final Type typeOfDest = createParameterizedType(UiListDelta.class, dtoClass);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (EndPointTypeUtil.isAssignableFrom(DtList.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtList has one parameterized type
					final Type typeOfDest = createParameterizedType(UiList.class, dtoClass);
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
		return new KnowedParameterizedType(rawClass, typeArguments);
	}

	private static final class JsonExclusionStrategy implements ExclusionStrategy {
		/** {@inheritDoc} */
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
	}

	private static final class ClassJsonSerializer implements JsonSerializer<Class> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Class src, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(src.getName());
		}
	}

	private static final class OptionJsonSerializer implements JsonSerializer<Option> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Option src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isDefined()) {
				return context.serialize(src.get());
			}
			return null; //rien
		}
	}

	private static final class DefinitionReferenceJsonSerializer implements JsonSerializer<DefinitionReference> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final DefinitionReference src, final Type typeOfSrc, final JsonSerializationContext context) {
			return context.serialize(src.get().getName());
		}
	}

	private static final class MapJsonSerializer implements JsonSerializer<Map> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Map src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class ListJsonSerializer implements JsonSerializer<List> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final List src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private static final class ComponentInfoJsonSerializer implements JsonSerializer<ComponentInfo> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final ComponentInfo componentInfo, final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject jsonObject = new JsonObject();
			jsonObject.add(componentInfo.getTitle(), context.serialize(componentInfo.getValue()));
			return jsonObject;
		}
	}

	private static final class FacetedQueryResultJsonSerializer implements JsonSerializer<FacetedQueryResult<?, ?>> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final FacetedQueryResult<?, ?> facetedQueryResult, final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject jsonObject = new JsonObject();

			//1- add result list as data
			if (facetedQueryResult.getClusters().isEmpty()) {
				final JsonArray jsonList = (JsonArray) context.serialize(facetedQueryResult.getDtList());
				jsonObject.add("list", jsonList);
			} else {
				//if it's a cluster add data's cluster
				final JsonObject jsonCluster = new JsonObject();
				for (final Entry<FacetValue, ?> cluster : facetedQueryResult.getClusters().entrySet()) {
					final JsonArray jsonList = (JsonArray) context.serialize(cluster.getValue());
					jsonCluster.add(cluster.getKey().getLabel().getDisplay(), jsonList);
				}
				jsonObject.add("groups", jsonCluster);
			}

			//2- add facet list as facets
			final List<Facet> facets = facetedQueryResult.getFacets();
			final JsonObject jsonFacet = new JsonObject();
			for (final Facet facet : facets) {
				final Map<String, Long> maps = new HashMap<>();
				for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
					maps.put(entry.getKey().getLabel().getDisplay(), entry.getValue());
				}
				final JsonObject jsonFacetValues = (JsonObject) context.serialize(maps);
				final String facetName = facet.getDefinition().getName();
				jsonFacet.add(facetName, jsonFacetValues);
			}
			jsonObject.add("facets", jsonFacet);

			//3 -add totalCount
			jsonObject.addProperty(DtList.TOTAL_COUNT_META, facetedQueryResult.getCount());
			return jsonObject;
		}
	}

	private static final class KnowedParameterizedType implements ParameterizedType {
		private final Class<?> rawClass;
		private final Type[] typeArguments;

		KnowedParameterizedType(final Class<?> rawClass, final Type[] typeArguments) {
			this.rawClass = rawClass;
			this.typeArguments = typeArguments;
		}

		/** {@inheritDoc} */
		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments;
		}

		/** {@inheritDoc} */
		@Override
		public Type getOwnerType() {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public Type getRawType() {
			return rawClass;
		}
	}

	private static class UiObjectDeserializer<D extends DtObject> implements JsonDeserializer<UiObject<D>> {
		/** {@inheritDoc} */
		@Override
		public UiObject<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
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
			//Send a alert if no fields match the DtObject ones : details may be a security issue ?
			if (modifiedFields.isEmpty()) {
				final Set<String> jsonEntry = new HashSet<>();
				for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					jsonEntry.add(entry.getKey());
				}
				throw new JsonSyntaxException("Received Json's fields doesn't match " + dtoClass.getSimpleName() + " ones : " + jsonEntry);
			}
			final UiObject<D> uiObject = new UiObject<>(inputDto, modifiedFields);
			if (jsonObject.has(SERVER_SIDE_TOKEN_FIELDNAME)) {
				uiObject.setServerSideToken(jsonObject.get(SERVER_SIDE_TOKEN_FIELDNAME).getAsString());
			}
			return uiObject;
		}
	}

	/*private static class UiObjectExtendedDeserializer<D extends DtObject> implements JsonDeserializer<UiObjectExtended<D>> {
		@Override
		public UiObjectExtended<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
			final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
			final Type uiObjectType = createParameterizedType(UiObject.class, dtoClass);
			final UiObject<D> uiObject = context.deserialize(json, uiObjectType);
			final Set<String> uiObjectModifiedFields = uiObject.getModifiedFields();
			final UiObjectExtended<D> uiObjectExtended = new UiObjectExtended(uiObject);

			final JsonObject jsonObject = json.getAsJsonObject();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				final String key = entry.getKey();
				final JsonElement jsonSubElement = entry.getValue();
				if (!uiObjectModifiedFields.contains(key)) {
					//Can't type value : bad solution.
					uiObjectExtended.put(key, jsonSubElement.getAsString());
				}
			}
			return uiObjectExtended;
		}
	}*/

	private static Set<String> getFieldNames(final DtDefinition dtDefinition) {
		final Set<String> dtFieldNames = new HashSet<>();
		for (final DtField dtField : dtDefinition.getFields()) {
			dtFieldNames.add(StringUtil.constToLowerCamelCase(dtField.getName()));
		}
		return dtFieldNames;
	}

	private static class UiListDeltaDeserializer<D extends DtObject> implements JsonDeserializer<UiListDelta<D>> {
		/** {@inheritDoc} */
		@Override
		public UiListDelta<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
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

	private static class UiListDeserializer<D extends DtObject> implements JsonDeserializer<UiList<D>> {
		/** {@inheritDoc} */
		@Override
		public UiList<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
			final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
			final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
			final Type uiObjectType = createParameterizedType(UiObject.class, dtoClass);
			final JsonArray jsonArray = json.getAsJsonArray();

			final UiList<D> uiList = new UiList<>(dtoClass);
			for (final JsonElement element : jsonArray) {
				final UiObject<D> inputDto = context.deserialize(element, uiObjectType);
				uiList.add(inputDto);
			}
			return uiList;
		}
	}

	private Gson createGson() {
		return new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.setPrettyPrinting()
				//TODO  registerTypeAdapter(String.class, new EmptyStringAsNull<>())// add "" <=> null
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(UiObject.class, new UiObjectDeserializer<>())
				.registerTypeAdapter(UiListDelta.class, new UiListDeltaDeserializer<>())
				.registerTypeAdapter(UiList.class, new UiListDeserializer<>())
				//.registerTypeAdapter(UiObjectExtended.class, new UiObjectExtendedDeserializer<>())
				/*.registerTypeAdapter(DtObjectExtended.class, new JsonSerializer<DtObjectExtended<?>>() {
					@Override
					public JsonElement serialize(final DtObjectExtended<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
						final JsonObject jsonObject = new JsonObject();
						final JsonObject jsonInnerObject = (JsonObject) context.serialize(src.getInnerObject());
						for (final Entry<String, JsonElement> entry : jsonInnerObject.entrySet()) {
							jsonObject.add(entry.getKey(), entry.getValue());
						}
						for (final Entry<String, Serializable> entry : src.entrySet()) {
							jsonObject.add(entry.getKey(), context.serialize(entry.getValue()));
						}
						return jsonObject;
					}
				})*/
				.registerTypeAdapter(FacetedQueryResult.class, new FacetedQueryResultJsonSerializer())
				.registerTypeAdapter(ComponentInfo.class, new ComponentInfoJsonSerializer())
				.registerTypeAdapter(List.class, new ListJsonSerializer())
				.registerTypeAdapter(Map.class, new MapJsonSerializer())
				.registerTypeAdapter(DefinitionReference.class, new DefinitionReferenceJsonSerializer())
				.registerTypeAdapter(Option.class, new OptionJsonSerializer())
				.registerTypeAdapter(Class.class, new ClassJsonSerializer())
				.addSerializationExclusionStrategy(new JsonExclusionStrategy())
				.create();
	}
}
