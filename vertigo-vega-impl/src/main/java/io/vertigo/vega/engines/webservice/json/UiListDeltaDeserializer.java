package io.vertigo.vega.engines.webservice.json;

import io.vertigo.dynamo.domain.model.DtObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * ParameterizedType use for UiListDelta.
 * @author npiedeloup
 */
final class UiListDeltaDeserializer<D extends DtObject> implements JsonDeserializer<UiListDelta<D>> {

	/** {@inheritDoc} */
	@Override
	public UiListDelta<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
		final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
		final Type uiObjectType = new KnownParameterizedType(UiObject.class, dtoClass);
		final JsonObject jsonObject = json.getAsJsonObject();

		final Map<String, UiObject<D>> collCreates = parseUiObjectMap(jsonObject, "collCreates", uiObjectType, context);
		final Map<String, UiObject<D>> collUpdates = parseUiObjectMap(jsonObject, "collUpdates", uiObjectType, context);
		final Map<String, UiObject<D>> collDeletes = parseUiObjectMap(jsonObject, "collDeletes", uiObjectType, context);

		return new UiListDelta<>(dtoClass, collCreates, collUpdates, collDeletes);
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
