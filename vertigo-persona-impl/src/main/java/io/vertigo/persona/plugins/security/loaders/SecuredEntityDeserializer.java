package io.vertigo.persona.plugins.security.loaders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertigo.app.Home;
import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.persona.impl.security.dsl.rules.DslParserUtil;
import io.vertigo.persona.security.dsl.model.DslMultiExpression;
import io.vertigo.persona.security.metamodel.Permission2;
import io.vertigo.persona.security.metamodel.SecuredEntity;
import io.vertigo.persona.security.metamodel.SecurityAxeType;
import io.vertigo.persona.security.metamodel.SecurityAxes;
import io.vertigo.util.StringUtil;

/**
 * Deserializer json
 *
 * @author npiedeloup
 */
public class SecuredEntityDeserializer implements JsonDeserializer<SecuredEntity> {

	/** {@inheritDoc} */
	@Override
	public SecuredEntity deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final JsonObject jsonSecuredEntity = json.getAsJsonObject();
		final DtDefinition entityDefinition = findDtDefinition(jsonSecuredEntity.get("entity").getAsString());

		final List<DtField> securityFields = new ArrayList<>();
		for (final JsonElement securityField : jsonSecuredEntity.get("securityFields").getAsJsonArray()) {
			securityFields.add(deserializeDtField(entityDefinition, securityField.getAsString()));
		}

		final List<SecurityAxes> advancedAxes = new ArrayList<>();
		for (final JsonElement advancedAxe : jsonSecuredEntity.get("advancedAxes").getAsJsonArray()) {//TODO if null ?
			advancedAxes.add(deserializeSecurityAxes(entityDefinition, advancedAxe.getAsJsonObject(), context));
		}

		final List<Permission2> operations = new ArrayList<>();
		for (final JsonElement operation : jsonSecuredEntity.get("operations").getAsJsonArray()) { //TODO if null ?
			operations.add(deserializeOperations(entityDefinition, operation.getAsJsonObject(), context));
		}

		return new SecuredEntity(entityDefinition, securityFields, advancedAxes, operations);
	}

	private static Permission2 deserializeOperations(final DtDefinition entityDefinition, final JsonObject operation, final JsonDeserializationContext context) {
		final String code = operation.get("name").getAsString();
		final String label = operation.get("label").getAsString();

		List<String> overrides = context.deserialize(operation.get("overrides"), createParameterizedType(List.class, String.class));
		if (overrides == null) {
			overrides = Collections.emptyList();
		}
		List<String> grants = context.deserialize(operation.get("grants"), createParameterizedType(List.class, String.class));
		if (grants == null) {
			grants = Collections.emptyList();
		}
		final List<String> strRules = context.deserialize(operation.get("rules"), createParameterizedType(List.class, String.class));
		final List<DslMultiExpression> rules = strRules.stream()
				.map(SecuredEntityDeserializer::parseRule)
				.collect(Collectors.toList());

		return new Permission2(code, label, overrides, grants, entityDefinition, rules);
	}

	private final static DslMultiExpression parseRule(final String securityRule) {
		Assertion.checkNotNull(securityRule);
		//-----
		try {
			return DslParserUtil.parseMultiExpression(securityRule);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getFullMessage());
			throw WrappedException.wrap(e, message);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getMessage());
			throw WrappedException.wrap(e, message);
		}
	}

	private static SecurityAxes deserializeSecurityAxes(final DtDefinition entityDefinition, final JsonObject advancedAxe, final JsonDeserializationContext context) {
		final SecurityAxeType type = SecurityAxeType.valueOf(advancedAxe.get("type").getAsString());
		final DtField field = deserializeDtField(entityDefinition, advancedAxe.get("field").getAsString());
		final List<String> values = context.deserialize(advancedAxe.get("values"), createParameterizedType(List.class, String.class));

		return new SecurityAxes(type, field, values);
	}

	private static Type createParameterizedType(final Class<?> rawClass, final Type paramType) {
		final Type[] typeArguments = { paramType };
		return new KnownParameterizedType(rawClass, typeArguments);
	}

	private static DtField deserializeDtField(final DtDefinition entityDefinition, final String fieldName) {
		return entityDefinition.getField(fieldName);
	}

	private static DtDefinition findDtDefinition(final String entityName) {
		final String name = DefinitionUtil.getPrefix(DtDefinition.class) + Definition.SEPARATOR + entityName;
		return Home.getApp().getDefinitionSpace().resolve(name, DtDefinition.class);
	}
}
