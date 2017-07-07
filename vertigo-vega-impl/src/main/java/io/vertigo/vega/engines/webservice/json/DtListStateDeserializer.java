package io.vertigo.vega.engines.webservice.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.vertigo.dynamo.domain.model.DtListState;

final class DtListStateDeserializer implements JsonDeserializer<DtListState> {

	@Override
	public DtListState deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();

		final Integer top = jsonObject.has("top") ? jsonObject.get("top").getAsInt() : null;
		final int skip = jsonObject.has("skip") ? jsonObject.get("skip").getAsInt() : 0;
		final String sortFieldName = jsonObject.has("sortFieldName") ? jsonObject.get("sortFieldName").getAsString() : null;
		final Boolean sortDesc = jsonObject.has("sortDesc") ? jsonObject.get("sortDesc").getAsBoolean() : null;

		return new DtListState(top, skip, sortFieldName, sortDesc);
	}
}
