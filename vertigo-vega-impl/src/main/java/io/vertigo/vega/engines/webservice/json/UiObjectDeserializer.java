package io.vertigo.vega.engines.webservice.json;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.util.StringUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * ParameterizedType use for UiObject.
 * @author npiedeloup
 */
final class UiObjectDeserializer<D extends DtObject> implements JsonDeserializer<UiObject<D>> {

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
		if (jsonObject.has(JsonEngine.SERVER_SIDE_TOKEN_FIELDNAME)) {
			uiObject.setServerSideToken(jsonObject.get(JsonEngine.SERVER_SIDE_TOKEN_FIELDNAME).getAsString());
		}
		return uiObject;
	}

	private static Set<String> getFieldNames(final DtDefinition dtDefinition) {
		final Set<String> dtFieldNames = new HashSet<>();
		for (final DtField dtField : dtDefinition.getFields()) {
			dtFieldNames.add(StringUtil.constToLowerCamelCase(dtField.getName()));
		}
		return dtFieldNames;
	}
}
