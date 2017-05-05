package io.vertigo.persona.plugins.security.loaders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import io.vertigo.persona.security.metamodel.SecurityAxe;
import io.vertigo.persona.security.metamodel.SecurityAxeType;
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

		final List<SecurityAxe> advancedAxes = new ArrayList<>();
		for (final JsonElement advancedAxe : jsonSecuredEntity.get("advancedAxes").getAsJsonArray()) {//TODO if null ?
			advancedAxes.add(deserializeSecurityAxes(entityDefinition, advancedAxe.getAsJsonObject(), context));
		}

		final Map<String, Permission2> permissionPerOperations = new HashMap<>();// on garde la map des operations pour resoudre les grants
		for (final JsonElement operation : jsonSecuredEntity.get("operations").getAsJsonArray()) { //TODO if null ?
			final Permission2 permission = deserializeOperations(entityDefinition, operation.getAsJsonObject(), context, permissionPerOperations);
			Assertion.checkArgument(!permissionPerOperations.containsKey(permission.getOperation().get()), "Operation {0} already declared on {1}", permission.getOperation().get(), entityDefinition.getName());
			permissionPerOperations.put(permission.getOperation().get(), permission);
		}

		return new SecuredEntity(entityDefinition, securityFields, advancedAxes, new ArrayList<>(permissionPerOperations.values()));
	}

	private static Permission2 deserializeOperations(final DtDefinition entityDefinition, final JsonObject operation, final JsonDeserializationContext context, final Map<String, Permission2> permissionPerOperations) {
		final String code = operation.get("name").getAsString();
		final String label = operation.get("label").getAsString();

		Set<String> overrides = context.deserialize(operation.get("overrides"), createParameterizedType(Set.class, String.class));
		if (overrides == null) {
			overrides = Collections.emptySet();
		}
		final Set<Permission2> grants;
		final Set<String> strGrants = context.deserialize(operation.get("grants"), createParameterizedType(Set.class, String.class));
		if (strGrants == null) {
			grants = Collections.emptySet();
		} else {
			grants = strGrants.stream()
					.map((strGrant) -> resolvePermission(strGrant, permissionPerOperations, entityDefinition))
					.collect(Collectors.toSet());
		}

		final List<DslMultiExpression> rules;
		final List<String> strRules = context.deserialize(operation.get("rules"), createParameterizedType(List.class, String.class));
		if (!strRules.isEmpty()) {
			rules = strRules.stream()
					.map(SecuredEntityDeserializer::parseRule)
					.collect(Collectors.toList());
		} else {
			rules = Collections.emptyList(); //if empty -> always true
		}
		return new Permission2(code, label, overrides, grants, entityDefinition, rules);
	}

	private static Permission2 resolvePermission(final String operationName, final Map<String, Permission2> permissionPerOperations, final DtDefinition entityDefinition) {
		Assertion.checkArgument(permissionPerOperations.containsKey(operationName), "Operation {0} not declared on {1} (may check declaration order)", operationName, entityDefinition.getName());
		//-----
		return permissionPerOperations.get(operationName);
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

	private static SecurityAxe deserializeSecurityAxes(final DtDefinition entityDefinition, final JsonObject advancedAxe, final JsonDeserializationContext context) {
		final String name = advancedAxe.get("name").getAsString();
		final SecurityAxeType type = SecurityAxeType.valueOf(advancedAxe.get("type").getAsString());
		final List<String> fieldNames = deserializeList(advancedAxe.get("fields"), String.class, context);
		final List<DtField> fields = fieldNames.stream()
				.map(fieldName -> deserializeDtField(entityDefinition, fieldName))
				.collect(Collectors.toList());
		final List<String> values = deserializeList(advancedAxe.get("values"), String.class, context);
		return new SecurityAxe(name, type, fields, values);
	}

	private static Type createParameterizedType(final Class<?> rawClass, final Type paramType) {
		final Type[] typeArguments = { paramType };
		return new KnownParameterizedType(rawClass, typeArguments);
	}

	private static DtField deserializeDtField(final DtDefinition entityDefinition, final String fieldName) {
		return entityDefinition.getField(fieldName);
	}

	private static <T> List<T> deserializeList(final JsonElement jsonElement, final Class<T> elementClass, final JsonDeserializationContext context) {
		if (jsonElement == null) {
			return Collections.emptyList();
		}
		return context.deserialize(jsonElement, createParameterizedType(List.class, elementClass));
	}

	private static DtDefinition findDtDefinition(final String entityName) {
		final String name = DefinitionUtil.getPrefix(DtDefinition.class) + Definition.SEPARATOR + entityName;
		return Home.getApp().getDefinitionSpace().resolve(name, DtDefinition.class);
	}
}
