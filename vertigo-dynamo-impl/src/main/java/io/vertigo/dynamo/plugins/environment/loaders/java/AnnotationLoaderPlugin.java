package io.vertigo.dynamo.plugins.environment.loaders.java;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.kernel.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/**
 * Lecture des annotations présentes sur les objets métier.
 *
 * @author pchretien
 */
public final class AnnotationLoaderPlugin implements LoaderPlugin {
	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final Entity dtMetaDefinition;
	private final Entity dtFieldMetaDefinition;

	private final Entity associationNNMetaDefinition;
	private final Entity associationMetaDefinition;

	/**
	 * Constructeur.
	 * @param dtDefinitionsClassName Liste des classes java à parser.
	 */
	@Inject
	public AnnotationLoaderPlugin() {
		final DomainGrammar domainGrammar = DomainGrammar.INSTANCE;
		dtMetaDefinition = domainGrammar.getDtDefinitionEntity();
		dtFieldMetaDefinition = domainGrammar.getDtFieldEntity();
		associationNNMetaDefinition = domainGrammar.getAssociationNNEntity();
		associationMetaDefinition = domainGrammar.getAssociationEntity();
	}

	private static final class MethodComparator implements Comparator<Method> {
		/** {@inheritDoc} */
		public int compare(final Method m1, final Method m2) {
			return m1.getName().compareTo(m2.getName());
		}
	}

	private static final class FieldComparator implements Comparator<Field> {
		/** {@inheritDoc} */
		public int compare(final Field f1, final Field f2) {
			return f1.getName().compareTo(f2.getName());
		}
	}

	/**
	 * @return Liste des fichiers Java représentant des objets métiers.
	 */
	private static Iterable<Class<?>> getClasses(final String dtDefinitionsClassName) {
		return ClassUtil.newInstance(dtDefinitionsClassName, Iterable.class);
	}

	/** {@inheritDoc} */
	public void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final Iterable<Class<?>> classes = getClasses(resourcePath);

		//--Enregistrement des fichiers java annotés
		for (final Class<?> javaClass : classes) {
			//System.out.println(">>>>javaClass " + javaClass);
			load(javaClass, dynamicModelrepository);
		}
	}

	private void load(final Class<?> clazz, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		for (final Annotation annotation : clazz.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition) {
				parseDtDefinition((io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition) annotation, clazz, dynamicModelrepository);
				break;
			}
		}
	}

	private void parseDtDefinition(final io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition dtDefinitionAnnotation, final Class<?> clazz, final DynamicDefinitionRepository dynamicModelRepository) {
		final String simpleName = clazz.getSimpleName();
		final String packageName = clazz.getPackage().getName();

		final String urn = DT_DEFINITION_PREFIX + SEPARATOR + StringUtil.camelToConstCase(simpleName);

		final DynamicDefinitionBuilder dtDefinitionBuilder = dynamicModelRepository.createDynamicDefinitionBuilder(urn, dtMetaDefinition, packageName)//
				.withPropertyValue(KspProperty.PERSISTENT, dtDefinitionAnnotation.persistent());

		// Le tri des champs et des méthodes par ordre alphabétique est important car classe.getMethods() retourne
		// un ordre relativement aléatoire et la lecture des annotations peut donc changer l'ordre
		// des fields d'une lecture à l'autre (ou d'une compilation à l'autre).
		// Cela devient alors bloquant pour une communication par sérialisation entre 2 instances.

		final List<Field> fields = new ArrayList<>(ClassUtil.getAllFields(clazz));
		Collections.sort(fields, new FieldComparator());
		for (final Field field : fields) {
			//On regarde si il s'agit d'un champ
			parseFieldAnnotations(dynamicModelRepository, field, dtDefinitionBuilder);
		}

		final Method[] methods = clazz.getMethods();
		Arrays.sort(methods, new MethodComparator());
		for (final Method method : methods) {
			parseMethodAnnotations(dynamicModelRepository, method, dtDefinitionBuilder);
			//On regarde si il s'agit d'une associations
			parseAssociationDefinition(dynamicModelRepository, method, packageName);
		}
		final DynamicDefinition dtDefinition = dtDefinitionBuilder.build();
		dynamicModelRepository.addDefinition(dtDefinition);
	}

	private void parseAssociationDefinition(final DynamicDefinitionRepository dynamicModelRepository, final Method method, final String packageName) {
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.metamodel.annotation.Association) {
				final io.vertigo.dynamo.domain.metamodel.annotation.Association association = (io.vertigo.dynamo.domain.metamodel.annotation.Association) annotation;
				//============================================================
				//Attention pamc inverse dans oom les déclarations des objets !!
				final DynamicDefinitionKey primaryDtDefinitionKey = new DynamicDefinitionKey(association.primaryDtDefinitionName());
				final DynamicDefinitionKey foreignDtDefinitionKey = new DynamicDefinitionKey(association.foreignDtDefinitionName());

				final DynamicDefinition associationDefinition = dynamicModelRepository.createDynamicDefinitionBuilder(association.name(), associationMetaDefinition, packageName)//
						// associationDefinition.
						//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
						.withPropertyValue(KspProperty.MULTIPLICITY_A, association.primaryMultiplicity())//
						.withPropertyValue(KspProperty.MULTIPLICITY_B, association.foreignMultiplicity())//
						// navigabilités
						.withPropertyValue(KspProperty.NAVIGABILITY_A, association.primaryIsNavigable())//
						.withPropertyValue(KspProperty.NAVIGABILITY_B, association.foreignIsNavigable())//
						//Roles	
						.withPropertyValue(KspProperty.ROLE_A, association.primaryRole())//
						.withPropertyValue(KspProperty.LABEL_A, association.primaryLabel())//
						.withPropertyValue(KspProperty.ROLE_B, association.foreignRole())//
						.withPropertyValue(KspProperty.LABEL_B, association.foreignRole())//
						//---
						.withDefinition("dtDefinitionA", primaryDtDefinitionKey)//
						.withDefinition("dtDefinitionB", foreignDtDefinitionKey)//
						//---
						.withPropertyValue(KspProperty.FK_FIELD_NAME, association.fkFieldName())//
						.build();

				if (!dynamicModelRepository.containsDefinitionKey(associationDefinition.getDefinitionKey())) {
					//Les associations peuvent être déclarées sur les deux noeuds de l'association.
					dynamicModelRepository.addDefinition(associationDefinition);
				}
			} else if (annotation instanceof io.vertigo.dynamo.domain.metamodel.annotation.AssociationNN) {
				final io.vertigo.dynamo.domain.metamodel.annotation.AssociationNN association = (io.vertigo.dynamo.domain.metamodel.annotation.AssociationNN) annotation;
				//============================================================

				//Attention pamc inverse dans oom les déclarations des objets !!
				final DynamicDefinitionKey dtDefinitionAKey = new DynamicDefinitionKey(association.dtDefinitionA());
				final DynamicDefinitionKey dtDefinitionBKey = new DynamicDefinitionKey(association.dtDefinitionB());

				final DynamicDefinition associationDefinition = dynamicModelRepository.createDynamicDefinitionBuilder(association.name(), associationNNMetaDefinition, packageName)//
						.withPropertyValue(KspProperty.TABLE_NAME, association.tableName())//

						// associationDefinition.
						//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)

						// navigabilités
						.withPropertyValue(KspProperty.NAVIGABILITY_A, association.navigabilityA())//
						.withPropertyValue(KspProperty.NAVIGABILITY_B, association.navigabilityB())//

						.withPropertyValue(KspProperty.ROLE_A, association.roleA())//
						.withPropertyValue(KspProperty.LABEL_A, association.labelA())//
						.withPropertyValue(KspProperty.ROLE_B, association.roleB())//
						.withPropertyValue(KspProperty.LABEL_B, association.labelB())//

						.withDefinition("dtDefinitionA", dtDefinitionAKey)//
						.withDefinition("dtDefinitionB", dtDefinitionBKey)//
						.build();

				if (!dynamicModelRepository.containsDefinitionKey(associationDefinition.getDefinitionKey())) {
					//Les associations peuvent être déclarées sur les deux noeuds de l'association.
					dynamicModelRepository.addDefinition(associationDefinition);
				}
			}
		}
	}

	private void parseFieldAnnotations(final DynamicDefinitionRepository dynamicModelrepository, final Field field, final DynamicDefinitionBuilder dtDefinition) {
		for (final Annotation annotation : field.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.metamodel.annotation.Field) {
				//Le nom est automatiquement déduit du nom du champ
				final String fieldName = createFieldName(field);
				parseAnnotation(dynamicModelrepository, fieldName, dtDefinition, io.vertigo.dynamo.domain.metamodel.annotation.Field.class.cast(annotation));
			}
		}
	}

	private void parseMethodAnnotations(final DynamicDefinitionRepository dynamicModelrepository, final Method method, final DynamicDefinitionBuilder dtDefinition) {
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof io.vertigo.dynamo.domain.metamodel.annotation.Field) {
				//Le nom est automatiquement déduit du nom de la méthode
				final String fieldName = createFieldName(method);
				parseAnnotation(dynamicModelrepository, fieldName, dtDefinition, io.vertigo.dynamo.domain.metamodel.annotation.Field.class.cast(annotation));
			}
		}
	}

	/*
	 * Centralisation du parsing des annotations liées à un champ.
	 */
	private void parseAnnotation(final DynamicDefinitionRepository dynamicModelrepository, final String fieldName, final DynamicDefinitionBuilder dtDefinition, final io.vertigo.dynamo.domain.metamodel.annotation.Field field) {
		//Si on trouve un domaine on est dans un objet dynamo.
		final FieldType type = FieldType.valueOf(field.type());
		final DynamicDefinitionKey fieldDomainKey = new DynamicDefinitionKey(field.domain());
		final DynamicDefinition dtField = dynamicModelrepository.createDynamicDefinitionBuilder(fieldName, dtFieldMetaDefinition, null)//
				.withDefinition("domain", fieldDomainKey)//
				.withPropertyValue(KspProperty.LABEL, field.label())//
				.withPropertyValue(KspProperty.NOT_NULL, field.notNull())//
				.withPropertyValue(KspProperty.PERSISTENT, field.persistent())//
				.build();

		switch (type) {
			case PRIMARY_KEY:
				dtDefinition.withChildDefinition(DomainGrammar.PRIMARY_KEY, dtField);
				break;
			case DATA:
				dtDefinition.withChildDefinition("field", dtField);
				break;
			case COMPUTED:
				//Valeurs renseignées automatiquement parce que l'on est dans le cas d'un champ calculé
				dtDefinition.withChildDefinition("computed", dtField);
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
		// ----------------------------------------------------------------------
		final String fieldName = StringUtil.camelToConstCase(field.getName());
		if (StringUtil.constToCamelCase(fieldName, false).equals(field.getName())) {
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
		// ----------------------------------------------------------------------
		if (method.getName().startsWith("get")) {
			final String propertyName = method.getName().substring("get".length());
			final String fieldName = StringUtil.camelToConstCase(propertyName);
			if (StringUtil.constToCamelCase(fieldName, true).equals(propertyName)) {
				//Si on a une bijection alors OK
				return fieldName;
			}
		}
		throw new IllegalArgumentException(method.getName() + "ne permet pas de donner un nom unique de propriété ");
	}

	public String getType() {
		return "classes";
	}

}
