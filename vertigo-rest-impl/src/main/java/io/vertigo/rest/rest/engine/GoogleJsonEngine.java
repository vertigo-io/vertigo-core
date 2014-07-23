package io.vertigo.rest.rest.engine;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.lang.JsonExclude;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
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
	private static final String LIST_VALUE_FIELDNAME = "value";
	private static final String SERVER_SIDE_TOKEN_FIELDNAME = "serverId";
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
		return "{globalErrorMessages:[\"" + exceptionMessage + "\"]}"; //TODO +stack;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends Object> D fromJson(final String json, final Class<D> paramClass) {
		return gson.fromJson(json, paramClass);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> UiObject<D> uiObjectFromJson(final String json, final Class<D> paramClass) {
		final Type typeOfDest = createUiObjectType(paramClass);
		return gson.fromJson(json, typeOfDest);
	}

	/** {@inheritDoc} */
	@Override
	public UiContext uiContextFromJson(final String json, final Map<String, Class<?>> paramClasses) {
		final UiContext result = new UiContext();
		try {
			final JsonElement jsonElement = new JsonParser().parse(json);
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			for (final Entry<String, Class<?>> entry : paramClasses.entrySet()) {
				final String key = entry.getKey();
				final Class<?> paramClass = entry.getValue();
				final JsonElement jsonSubElement = jsonObject.get(key);

				final Serializable value;
				if (DtObject.class.isAssignableFrom(paramClass)) {
					final Type typeOfDest = createUiObjectType(paramClass);
					value = gson.fromJson(jsonSubElement, typeOfDest);
				} else {
					value = (Serializable) gson.fromJson(jsonSubElement, paramClass);
				}
				result.put(key, value);
			}
			return result;
		} catch (final IllegalStateException e) {
			throw new JsonSyntaxException("JsonObject expected", e);
		}
	}

	private Type createUiObjectType(final Class<?> paramClass) {
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
				return UiObject.class;
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

			final Set<String> modifiedFields = new HashSet<>();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				final String fieldName = entry.getKey();
				if (!SERVER_SIDE_TOKEN_FIELDNAME.equals(fieldName)) {
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
				//.serializeNulls()//On veut voir les null
				.registerTypeAdapter(UiObject.class, new UiObjectDeserializer<>())//
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
