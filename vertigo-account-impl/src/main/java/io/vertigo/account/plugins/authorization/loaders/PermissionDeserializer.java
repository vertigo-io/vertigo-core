package io.vertigo.account.plugins.authorization.loaders;

import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertigo.account.authorization.metamodel.Permission;

/**
 * Deserializer json
 *
 * @author npiedeloup
 */
final class PermissionDeserializer implements JsonDeserializer<Permission> {

	/** {@inheritDoc} */
	@Override
	public Permission deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final JsonObject jsonPermission = json.getAsJsonObject();
		final String code = jsonPermission.get("name").getAsString();
		final String label = jsonPermission.get("label").getAsString();
		final Optional<String> comment = Optional.ofNullable(jsonPermission.get("__comment"))
				.map(JsonElement::getAsString);
		return new Permission(code, label, comment);
	}
}
