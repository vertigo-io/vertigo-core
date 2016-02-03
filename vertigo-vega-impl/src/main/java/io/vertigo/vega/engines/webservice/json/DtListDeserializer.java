package io.vertigo.vega.engines.webservice.json;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * JsonDeserializer of DtList.
 * Warn : no validators, should use UiList instead.
 * @author npiedeloup
 */
final class DtListDeserializer<D extends DtObject> implements JsonDeserializer<DtList<D>> {
	/** {@inheritDoc} */
	@Override
	public DtList<D> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final Type[] typeParameters = ((ParameterizedType) typeOfT).getActualTypeArguments();
		final Class<D> dtoClass = (Class<D>) typeParameters[0]; // Id has only one parameterized type T
		final JsonArray jsonArray = json.getAsJsonArray();

		final DtList<D> dtList = new DtList<>(dtoClass);
		for (final JsonElement element : jsonArray) {
			final D inputDto = context.deserialize(element, dtoClass);
			dtList.add(inputDto);
		}
		return dtList;
	}
}
