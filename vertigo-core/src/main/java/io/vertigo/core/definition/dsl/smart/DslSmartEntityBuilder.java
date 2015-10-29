package io.vertigo.core.definition.dsl.smart;

import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityBuilder;
import io.vertigo.core.definition.dsl.entity.EntityField;
import io.vertigo.core.definition.dsl.entity.EntityPropertyType;
import io.vertigo.core.definition.dsl.entity.EntityType;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DslSmartEntityBuilder {

	public static Object build(final DynamicDefinition dynamicDefinition) {
		final Object instance = ClassUtil.newInstance(dynamicDefinition.getEntity().getName());

		for (final String propertyName : dynamicDefinition.getEntity().getPropertyNames()) {
			final Field field = getField(instance, propertyName);
			ClassUtil.set(instance, field, dynamicDefinition.getPropertyValue(propertyName));
		}
		for (final EntityField entityField : dynamicDefinition.getEntity().getAttributes()) {
			if (entityField.getType() instanceof List) {
				throw new UnsupportedOperationException();
			}
			//System.out.println(">>child[" + entityField.getName() + "]::" + dynamicDefinition.getChildDefinitions(entityField.getName()));
			final DynamicDefinition childDynamicDefinition = dynamicDefinition.getChildDefinitions(entityField.getName()).get(0);
			final Object childInstance = build(childDynamicDefinition);
			final Field field = getField(instance, entityField.getName());
			ClassUtil.set(instance, field, childInstance);
		}
		return instance;
	}

	private static Field getField(final Object instance, final String fieldName) {
		Field field;
		try {
			field = instance.getClass().getField(fieldName);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return field;
	}

	public static List<Entity> build(final Class<?> entityClass) {
		final Map<String, Entity> map = new LinkedHashMap<>();
		doBuild(map, entityClass);
		return new ArrayList<>(map.values());
	}

	private static Entity doBuild(final Map<String, Entity> map, final Class<?> entityClass) {
		Assertion.checkNotNull(entityClass);
		Assertion.checkState(entityClass.isAnnotationPresent(DslEntity.class), "annotation {0} required on {1}", DslEntity.class, entityClass);
		//-----
		final EntityBuilder entityBuilder = new EntityBuilder(entityClass.getName());
		for (final Field field : entityClass.getFields()) {
			Assertion.checkState(field.isAnnotationPresent(DslField.class), "annotation {0} required on {1} for field {2}", DslField.class, entityClass, field.getName());
			final String fieldName = field.getName();
			final EntityType entityType = find(map, field.getType());
			final boolean notNull = field.getAnnotation(DslField.class).value();
			entityBuilder.addField(fieldName, entityType, notNull);
		}
		final Entity entity = entityBuilder.build();
		map.put(entity.getName(), entity);
		return entity;
	}

	private static EntityType find(final Map<String, Entity> map, final Class type) {
		try {
			return EntityPropertyType.valueOf(type.getSimpleName());
		} catch (final Exception e) {
			if (map.containsKey(type.getName())) {
				return map.get(type.getName());
			}
			return doBuild(map, type);
		}
	}
}
