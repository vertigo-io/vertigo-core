/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.vega.engines.webservice.json;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import io.vertigo.core.component.ComponentInfo;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.WrappedException;
import io.vertigo.vega.webservice.WebServiceTypeUtil;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.UiObject;

/**
 * @author pchretien, npiedeloup
 */
public final class GoogleJsonEngine implements JsonEngine {
	private final Gson gson;

	private static enum SearchApiVersion {
		V1(FacetedQueryResultJsonSerializerV1.class), //first api
		V2(FacetedQueryResultJsonSerializerV2.class), //with array instead of object
		V3(FacetedQueryResultJsonSerializerV3.class), //with code label, count on facets
		V4(FacetedQueryResultJsonSerializerV4.class); //with highlights and code, label for facet

		private Class<? extends JsonSerializer<FacetedQueryResult<?, ?>>> jsonSerializerClass;

		<C extends JsonSerializer<FacetedQueryResult<?, ?>>> SearchApiVersion(final Class<C> jsonSerializerClass) {
			this.jsonSerializerClass = jsonSerializerClass;
		}

		Class<? extends JsonSerializer<FacetedQueryResult<?, ?>>> getJsonSerializerClass() {
			return jsonSerializerClass;
		}
	}

	@Inject
	public GoogleJsonEngine(@Named("searchApiVersion") final Optional<String> searchApiVersionStr) {
		final SearchApiVersion searchApiVersion = SearchApiVersion.valueOf(searchApiVersionStr.orElse(SearchApiVersion.V4.name()));
		gson = createGson(searchApiVersion);
	}

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
		return gson.toJson(Collections.singletonMap("globalErrors", Collections.singletonList(exceptionMessage)));
	}

	/** {@inheritDoc} */
	@Override
	public <D> D fromJson(final String json, final Type paramType) {
		return gson.fromJson(json, paramType);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiObject<D> uiObjectFromJson(final String json, final Type paramType) {
		final Type typeOfDest = createParameterizedType(UiObject.class, paramType);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiListDelta<D> uiListDeltaFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
		final Type typeOfDest = createParameterizedType(UiListDelta.class, dtoClass);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiListModifiable<D> uiListFromJson(final String json, final Type paramType) {
		final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtList has one parameterized type
		final Type typeOfDest = createParameterizedType(UiListModifiable.class, dtoClass);
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
				if (WebServiceTypeUtil.isAssignableFrom(DtObject.class, paramType)) {
					final Type typeOfDest = new KnownParameterizedType(UiObject.class, paramType);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (WebServiceTypeUtil.isAssignableFrom(DtListDelta.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtListDelta has one parameterized type
					final Type typeOfDest = new KnownParameterizedType(UiListDelta.class, dtoClass);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else if (WebServiceTypeUtil.isAssignableFrom(DtList.class, paramType)) {
					final Class<DtObject> dtoClass = (Class<DtObject>) ((ParameterizedType) paramType).getActualTypeArguments()[0]; //we known that DtList has one parameterized type
					final Type typeOfDest = new KnownParameterizedType(UiListModifiable.class, dtoClass);
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
		return new KnownParameterizedType(rawClass, typeArguments);
	}

	private static final class JsonExclusionStrategy implements ExclusionStrategy {
		/** {@inheritDoc} */
		@Override
		public boolean shouldSkipField(final FieldAttributes arg0) {
			return (arg0.getAnnotation(JsonExclude.class) != null);
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

	private static final class OptionJsonSerializer implements JsonSerializer<Optional> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Optional src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isPresent()) {
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

	private static final class URIJsonAdapter implements JsonSerializer<URI>, JsonDeserializer<URI> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final URI uri, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(uri.urn());
		}

		/** {@inheritDoc} */
		@Override
		public URI deserialize(final JsonElement json, final Type paramType, final JsonDeserializationContext paramJsonDeserializationContext) {
			return URI.fromURN(json.getAsString());
		}
	}

	private static class UTCDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Date date, final Type type, final JsonSerializationContext jsonSerializationContext) {
			//Use INPUT_DATE_FORMATS[0] => ISO8601 format
			return new JsonPrimitive(UTCDateUtil.format(date));
		}

		/** {@inheritDoc} */
		@Override
		public Date deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
			return UTCDateUtil.parse(jsonElement.getAsString());
		}
	}

	private static Gson createGson(final SearchApiVersion searchApiVersion) {
		try {
			return new GsonBuilder()
					.setPrettyPrinting()
					//.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
					.registerTypeAdapter(Date.class, new UTCDateAdapter())
					//TODO  registerTypeAdapter(String.class, new EmptyStringAsNull<>())// add "" <=> null
					.registerTypeAdapter(UiObject.class, new UiObjectDeserializer<>())
					.registerTypeAdapter(UiListDelta.class, new UiListDeltaDeserializer<>())
					.registerTypeAdapter(UiListModifiable.class, new UiListDeserializer<>())
					.registerTypeAdapter(DtList.class, new DtListDeserializer<>())
					.registerTypeAdapter(ComponentInfo.class, new ComponentInfoJsonSerializer())
					.registerTypeAdapter(FacetedQueryResult.class, searchApiVersion.getJsonSerializerClass().newInstance())
					.registerTypeAdapter(List.class, new ListJsonSerializer())
					.registerTypeAdapter(Map.class, new MapJsonSerializer())
					.registerTypeAdapter(DefinitionReference.class, new DefinitionReferenceJsonSerializer())
					.registerTypeAdapter(Optional.class, new OptionJsonSerializer())
					.registerTypeAdapter(Class.class, new ClassJsonSerializer())
					.registerTypeAdapter(URI.class, new URIJsonAdapter())
					.addSerializationExclusionStrategy(new JsonExclusionStrategy())
					.create();
		} catch (InstantiationException | IllegalAccessException e) {
			throw WrappedException.wrapIfNeeded(e, "Can't create Gson");
		}
	}
}
