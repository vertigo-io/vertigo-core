package io.vertigo.vega.engines.webservice.json;

import io.vertigo.dynamo.domain.model.DtObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * ParameterizedType use for UiList.
 * @author npiedeloup
 */
final class UiListDeserializer<D extends DtObject> implements JsonDeserializer<UiList<D>> {

	/** {@inheritDoc} */
	@Override
	public UiList<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
		final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
		final Type uiObjectType = new KnownParameterizedType(UiObject.class, dtoClass);
		final JsonArray jsonArray = json.getAsJsonArray();

		final UiList<D> uiList = new UiList<>(dtoClass);
		for (final JsonElement element : jsonArray) {
			final UiObject<D> inputDto = context.deserialize(element, uiObjectType);
			uiList.add(inputDto);
		}
		return uiList;
	}
}
