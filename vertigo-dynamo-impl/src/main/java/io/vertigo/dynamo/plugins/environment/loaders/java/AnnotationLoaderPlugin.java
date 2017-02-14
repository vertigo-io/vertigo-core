/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.environment.loaders.java;

import static io.vertigo.dynamo.plugins.environment.KspProperty.DATA_SPACE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.FK_FIELD_NAME;
import static io.vertigo.dynamo.plugins.environment.KspProperty.LABEL;
import static io.vertigo.dynamo.plugins.environment.KspProperty.LABEL_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.LABEL_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.MULTIPLICITY_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.MULTIPLICITY_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NAVIGABILITY_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NAVIGABILITY_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NOT_NULL;
import static io.vertigo.dynamo.plugins.environment.KspProperty.PERSISTENT;
import static io.vertigo.dynamo.plugins.environment.KspProperty.ROLE_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.ROLE_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.STEREOTYPE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.TABLE_NAME;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionBuilder;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.core.definition.loader.LoaderPlugin;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.model.DtMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.Fragment;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.stereotype.DataSpace;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.Selector;
import io.vertigo.util.Selector.ClassConditions;
import io.vertigo.util.StringUtil;

/**
 * Lecture des annotations présentes sur les objets métier.
 *
 * @author pchretien
 */
public final class AnnotationLoaderPlugin implements LoaderPlugin {
	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private static final class MethodComparator implements Comparator<Method>, Serializable {
		private static final long serialVersionUID = -3272894481096942477L;

		/** {@inheritDoc} */
		@Override
		public int compare(final Method m1, final Method m2) {
			return m1.getName().compareTo(m2.getName());
		}
	}

	private static final class FieldComparator implements Comparator<Field>, Serializable {
		private static final long serialVersionUID = -3272894481096942477L;

		/** {@inheritDoc} */
		@Override
		public int compare(final Field f1, final Field f2) {
			return f1.getName().compareTo(f2.getName());
		}
	}

	/**
	 * @return Liste des fichiers Java représentant des objets métiers.
	 */
	private static <F> Set<Class<F>> selectClasses(final String resourcePath, final Class<F> filterClass) {
		final Selector selector = new Selector();
		if (resourcePath.endsWith("*")) {
			//by package
			final String packageName = resourcePath.substring(0, resourcePath.length() - 1);
			selector.from(packageName);
		} else {
			//by Iterable of classes
			final Iterable dtDefinitionsClass = ClassUtil.newInstance(resourcePath, Iterable.class);
			selector.from(dtDefinitionsClass);
		}
		return selector
				.filterClasses(ClassConditions.subTypeOf(filterClass))
				.findClasses()
				.stream()
				.map(clazz -> (Class<F>) clazz)
				.collect(Collectors.toSet());
	}

	/** {@inheritDoc} */
	@Override
	public void load(final String resourcePath, final DslDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//-----
		//--Enregistrement des fichiers java annotés
		for (final Class<DtObject> javaClass : selectClasses(resourcePath, DtObject.class)) {
			load(javaClass, dynamicModelrepository);
		}
	}

	private static void load(final Class<DtObject> clazz, final DslDefinitionRepository dynamicModelrepository) {
		Assertion.checkNotNull(dynamicModelrepository);
		//-----
		final String simpleName = clazz.getSimpleName();
		final String packageName = clazz.getPackage().getName();
		final String dtDefinitionName = DT_DEFINITION_PREFIX + SEPARATOR + StringUtil.camelToConstCase(simpleName);

		String fragmentOf = null;
		if (Fragment.class.isAssignableFrom(clazz)) {
			//Fragments
			for (final Annotation annotation : clazz.getAnnotations()) {
				if (annotation instanceof io.vertigo.dynamo.domain.stereotype.Fragment) {
					fragmentOf = ((io.vertigo.dynamo.domain.stereotype.Fragment) annotation).fragmentOf();
					break;
				}
			}
			parseFragment(clazz, fragmentOf, dtDefinitionName, packageName, dynamicModelrepository);
		} else {
			final DtStereotype stereotype = parseStereotype(clazz);
			parseDtDefinition(clazz, stereotype, dtDefinitionName, packageName, dynamicModelrepository);
		}

	}

	private static void parseFragment(
			final Class<DtObject> clazz,
			final String fragmentOf,
			final String dtDefinitionName,
			final String packageName,
			final DslDefinitionRepository dynamicModelRepository) {
		final DslDefinitionBuilder dtDefinitionBuilder = DslDefinitionRepository.createDynamicDefinitionBuilder(dtDefinitionName, DomainGrammar.FRAGMENT_ENTITY, packageName)
				.addDefinitionLink("from", fragmentOf);

		parseDynamicDefinitionBuilder(clazz, dtDefinitionBuilder, dynamicModelRepository);
	}

	private static void parseDtDefinition(
			final Class<DtObject> clazz,
			final DtStereotype stereotype,
			final String dtDefinitionName,
			final String packageName,
			final DslDefinitionRepository dynamicModelRepository) {
		final DslDefinitionBuilder dtDefinitionBuilder = DslDefinitionRepository.createDynamicDefinitionBuilder(dtDefinitionName, DomainGrammar.DT_DEFINITION_ENTITY, packageName)
				.addPropertyValue(STEREOTYPE, stereotype.name());

		// Only Persistent stereotypes have a dataspace => Fragment got it from parent
		if (stereotype.isPersistent()) {
			dtDefinitionBuilder.addPropertyValue(DATA_SPACE, parseDataSpaceAnnotation(clazz));
		}
		parseDynamicDefinitionBuilder(clazz, dtDefinitionBuilder, dynamicModelRepository);
	}

	private static void parseDynamicDefinitionBuilder(final Class<DtObject> clazz, final DslDefinitionBuilder dtDefinitionBuilder, final DslDefinitionRepository dynamicModelRepository) {
		final String packageName = clazz.getPackage().getName();

		// Le tri des champs et des méthodes par ordre alphabétique est important car classe.getMethods() retourne
		// un ordre relativement aléatoire et la lecture des annotations peut donc changer l'ordre
		// des fields d'une lecture à l'autre (ou d'une compilation à l'autre).
		// Cela devient alors bloquant pour une communication par sérialisation entre 2 instances.

		final List<Field> fields = new ArrayList<>(ClassUtil.getAllFields(clazz));
		Collections.sort(fields, new FieldComparator());
		for (final Field field : fields) {
			//On regarde si il s'agit d'un champ
			parseFieldAnnotations(field, dtDefinitionBuilder);
		}

		final Method[] methods = clazz.getMethods();
		Arrays.sort(methods, new MethodComparator());
		for (final Method method : methods) {
			parseMethodAnnotations(method, dtDefinitionBuilder);
			//On regarde si il s'agit d'une associations
			parseAssociationDefinition(dynamicModelRepository, method, packageName);
		}

		final DslDefinition dtDefinition = dtDefinitionBuilder.build();
		dynamicModelRepository.addDefinition(dtDefinition);
	}

	private static String parseDataSpaceAnnotation(final Class<?> clazz) {
		final DataSpace[] dataSpaceAnnotations = clazz.getAnnotationsByType(DataSpace.class);
		Assertion.checkState(dataSpaceAnnotations.length <= 1, "Entity {0} can have at max one DataSpace", clazz.getSimpleName());
		// ---
		if (dataSpaceAnnotations.length == 1) {
			return dataSpaceAnnotations[0].value();
		}
		return StoreManager.MAIN_DATA_SPACE_NAME;
	}

	private static DtStereotype parseStereotype(final Class<DtObject> clazz) {
		if (DtMasterData.class.isAssignableFrom(clazz)) {
			return DtStereotype.MasterData;
		} else if (KeyConcept.class.isAssignableFrom(clazz)) {
			return DtStereotype.KeyConcept;
		} else if (Entity.class.isAssignableFrom(clazz)) {
			return DtStereotype.Entity;
		}
		return DtStereotype.ValueObject;
	}

	private static void parseAssociationDefinition(final DslDefinitionRepository dynamicModelRepository, final Method method, final String packageName) {
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.stereotype.Association) {
				final io.vertigo.dynamo.domain.stereotype.Association association = (io.vertigo.dynamo.domain.stereotype.Association) annotation;
				//============================================================
				//Attention pamc inverse dans oom les déclarations des objets !!

				final DslDefinition associationDefinition = DslDefinitionRepository.createDynamicDefinitionBuilder(association.name(), DomainGrammar.ASSOCIATION_ENTITY, packageName)
						// associationDefinition.
						//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
						.addPropertyValue(MULTIPLICITY_A, association.primaryMultiplicity())
						.addPropertyValue(MULTIPLICITY_B, association.foreignMultiplicity())
						// navigabilités
						.addPropertyValue(NAVIGABILITY_A, association.primaryIsNavigable())
						.addPropertyValue(NAVIGABILITY_B, association.foreignIsNavigable())
						//Roles
						.addPropertyValue(ROLE_A, association.primaryRole())
						.addPropertyValue(LABEL_A, association.primaryLabel())
						.addPropertyValue(ROLE_B, association.foreignRole())
						.addPropertyValue(LABEL_B, association.foreignRole())
						//---
						.addDefinitionLink("dtDefinitionA", association.primaryDtDefinitionName())
						.addDefinitionLink("dtDefinitionB", association.foreignDtDefinitionName())
						//---
						.addPropertyValue(FK_FIELD_NAME, association.fkFieldName())
						.build();

				if (!dynamicModelRepository.containsDefinitionName(associationDefinition.getName())) {
					//Les associations peuvent être déclarées sur les deux noeuds de l'association.
					dynamicModelRepository.addDefinition(associationDefinition);
				}
			} else if (annotation instanceof io.vertigo.dynamo.domain.stereotype.AssociationNN) {
				final io.vertigo.dynamo.domain.stereotype.AssociationNN association = (io.vertigo.dynamo.domain.stereotype.AssociationNN) annotation;
				//============================================================

				//Attention pamc inverse dans oom les déclarations des objets !!
				final DslDefinition associationDefinition = DslDefinitionRepository.createDynamicDefinitionBuilder(association.name(), DomainGrammar.ASSOCIATION_NN_ENTITY, packageName)
						.addPropertyValue(TABLE_NAME, association.tableName())

						// associationDefinition.
						//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)

						// navigabilités
						.addPropertyValue(NAVIGABILITY_A, association.navigabilityA())
						.addPropertyValue(NAVIGABILITY_B, association.navigabilityB())

						.addPropertyValue(ROLE_A, association.roleA())
						.addPropertyValue(LABEL_A, association.labelA())
						.addPropertyValue(ROLE_B, association.roleB())
						.addPropertyValue(LABEL_B, association.labelB())

						.addDefinitionLink("dtDefinitionA", association.dtDefinitionA())
						.addDefinitionLink("dtDefinitionB", association.dtDefinitionB())
						.build();

				if (!dynamicModelRepository.containsDefinitionName(associationDefinition.getName())) {
					//Les associations peuvent être déclarées sur les deux noeuds de l'association.
					dynamicModelRepository.addDefinition(associationDefinition);
				}
			}
		}
	}

	private static void parseFieldAnnotations(final Field field, final DslDefinitionBuilder dtDefinition) {
		for (final Annotation annotation : field.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.stereotype.Field) {
				//Le nom est automatiquement déduit du nom du champ
				final String fieldName = createFieldName(field);
				parseAnnotation(fieldName, dtDefinition, io.vertigo.dynamo.domain.stereotype.Field.class.cast(annotation));
			}
		}
	}

	private static void parseMethodAnnotations(final Method method, final DslDefinitionBuilder dtDefinition) {
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.stereotype.Field) {
				//Le nom est automatiquement déduit du nom de la méthode
				final String fieldName = createFieldName(method);
				parseAnnotation(fieldName, dtDefinition, io.vertigo.dynamo.domain.stereotype.Field.class.cast(annotation));
			}
		}
	}

	/*
	 * Centralisation du parsing des annotations liées à un champ.
	 */
	private static void parseAnnotation(final String fieldName, final DslDefinitionBuilder dtDefinition, final io.vertigo.dynamo.domain.stereotype.Field field) {
		//Si on trouve un domaine on est dans un objet dynamo.
		final FieldType type = FieldType.valueOf(field.type());
		final DslDefinition dtField = DslDefinitionRepository.createDynamicDefinitionBuilder(fieldName, DomainGrammar.DT_FIELD_ENTITY, null)
				.addDefinitionLink("domain", field.domain())
				.addPropertyValue(LABEL, field.label())
				.addPropertyValue(NOT_NULL, field.required())
				.addPropertyValue(PERSISTENT, field.persistent())
				.build();

		switch (type) {
			case ID:
				dtDefinition.addChildDefinition(DomainGrammar.ID, dtField);
				break;
			case DATA:
				dtDefinition.addChildDefinition("field", dtField);
				break;
			case COMPUTED:
				//Valeurs renseignées automatiquement parce que l'on est dans le cas d'un champ calculé
				dtDefinition.addChildDefinition("computed", dtField);
				break;
			case FOREIGN_KEY:
				//on ne fait rien puisque le champ est défini par une association.
				break;
			default:
				throw new IllegalArgumentException("case " + type + " not implemented");
		}
	}

	/**
	 * Génération du nom du champ (Sous forme de constante) à partir du nom du champ.
	 * @param field champ
	 * @return Constante représentant le nom du champ
	 */
	private static String createFieldName(final Field field) {
		Assertion.checkNotNull(field);
		//-----
		final String fieldName = StringUtil.camelToConstCase(field.getName());
		if (StringUtil.constToLowerCamelCase(fieldName).equals(field.getName())) {
			return fieldName;
		}
		throw new IllegalArgumentException(field.getName() + " ne permet pas de donner un nom unique de propriété ");
	}

	/**
	 * Génération du nom du champ (Sous forme de constante) à partir du nom de la méthode.
	 * @param method Method
	 * @return Constante représentant le nom du champ
	 */
	private static String createFieldName(final Method method) {
		Assertion.checkNotNull(method);
		//-----
		if (method.getName().startsWith("get")) {
			final String propertyName = method.getName().substring("get".length());
			final String fieldName = StringUtil.camelToConstCase(propertyName);
			if (StringUtil.constToUpperCamelCase(fieldName).equals(propertyName)) {
				//Si on a une bijection alors OK
				return fieldName;
			}
		}
		throw new IllegalArgumentException(method.getName() + "ne permet pas de donner un nom unique de propriété ");
	}

	@Override
	public String getType() {
		return "classes";
	}
}
