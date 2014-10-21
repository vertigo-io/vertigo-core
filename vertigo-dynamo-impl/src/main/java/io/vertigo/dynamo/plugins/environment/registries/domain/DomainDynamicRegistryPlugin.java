/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.environment.registries.domain;

import io.vertigo.core.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.ComputedExpression;
import io.vertigo.dynamo.domain.metamodel.Constraint;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.Properties;
import io.vertigo.dynamo.domain.metamodel.PropertiesBuilder;
import io.vertigo.dynamo.domain.metamodel.Property;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractConstraintImpl;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author pchretien
 */
public final class DomainDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {
	private static final Logger LOGGER = Logger.getLogger(DomainDynamicRegistryPlugin.class);
	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final String ASSOCIATION_DEFINITION_PREFIX = DefinitionUtil.getPrefix(AssociationDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;
	private final DefinitionSpace definitionSpace;

	/**
	 * Constructeur.
	 */
	public DomainDynamicRegistryPlugin() {
		super(DomainGrammar.GRAMMAR);
		definitionSpace = Home.getDefinitionSpace();
		definitionSpace.register(DtDefinition.class);
		definitionSpace.register(Domain.class);
		definitionSpace.register(Formatter.class);
		definitionSpace.register(Constraint.class);
		definitionSpace.register(AssociationDefinition.class);
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		final Entity metaDefinition = xdefinition.getEntity();
		if (metaDefinition.equals(DomainGrammar.DOMAIN_ENTITY)) {
			final Domain definition = createDomain(xdefinition);
			definitionSpace.put(definition, Domain.class);
		} else if (metaDefinition.equals(DomainGrammar.DT_DEFINITION_ENTITY)) {
			final DtDefinition dtDefinition = createDtDefinition(xdefinition);
			definitionSpace.put(dtDefinition, DtDefinition.class);
		} else if (metaDefinition.equals(DomainGrammar.ASSOCIATION_ENTITY)) {
			final AssociationDefinition definition = createAssociationSimpleDefinition(xdefinition);
			definitionSpace.put(definition, AssociationDefinition.class);
		} else if (metaDefinition.equals(DomainGrammar.ASSOCIATION_NN_ENTITY)) {
			final AssociationDefinition definition = createAssociationNNDefinition(xdefinition);
			definitionSpace.put(definition, AssociationDefinition.class);
		} else if (metaDefinition.equals(DomainGrammar.CONSTAINT_ENTITY)) {
			final Constraint definition = createConstraint(xdefinition);
			definitionSpace.put(definition, Constraint.class);
		} else if (metaDefinition.equals(DomainGrammar.FORMATTER_ENTITY)) {
			final Formatter definition = createFormatter(xdefinition);
			definitionSpace.put(definition, Formatter.class);
		} else {
			throw new IllegalArgumentException("Type de définition non gérée: " + xdefinition.getDefinitionKey().getName());
		}
	}

	/**
	 * Enregistrement de contrainte
	 *
	 * @param xconstraint Définition de contrainte
	 * @return DefinitionStandard Définition typée créée.
	 */
	private static Constraint createConstraint(final DynamicDefinition xconstraint) {
		//On transforme la liste des paramètres (Liste de String) sous forme de tableau de String pour éviter
		//le sous typage de List et pour se rapprocher de la syntaxe connue de Main.
		final String args = getPropertyValueAsString(xconstraint, KspProperty.ARGS);
		final String msg = getPropertyValueAsString(xconstraint, KspProperty.MSG);
		final Class<? extends AbstractConstraintImpl> constraintClass = getConstraintClass(xconstraint);
		//----------------------------------------------------------------------
		//On instancie un objet contrainte.
		return createConstraint(constraintClass, xconstraint.getDefinitionKey().getName(), args, msg);
	}

	private static Class<? extends AbstractConstraintImpl> getConstraintClass(final DynamicDefinition xconstraint) {
		final String className = getPropertyValueAsString(xconstraint, KspProperty.CLASS_NAME);
		return ClassUtil.classForName(className, AbstractConstraintImpl.class);
	}

	private static Class<? extends AbstractFormatterImpl> getFormatterClass(final DynamicDefinition xformatter) {
		final String className = getPropertyValueAsString(xformatter, KspProperty.CLASS_NAME);
		return ClassUtil.classForName(className, AbstractFormatterImpl.class);
	}

	private static Constraint createConstraint(final Class<? extends AbstractConstraintImpl> constraintClass, final String constraintName, final String args, final String msg) {
		final Constructor<? extends AbstractConstraintImpl> constructor = ClassUtil.findConstructor(constraintClass, new Class[] { String.class });
		final AbstractConstraintImpl constraint = ClassUtil.newInstance(constructor, new Object[] { constraintName });
		constraint.initParameters(args);
		constraint.initMsg(msg == null ? null : new MessageText(msg, null));
		return constraint;
	}

	private static Formatter createFormatter(final Class<? extends AbstractFormatterImpl> formatterClass, final String formatterName, final String args) {
		final Constructor<? extends AbstractFormatterImpl> constructor = ClassUtil.findConstructor(formatterClass, new Class[] { String.class });
		final AbstractFormatterImpl formatter = ClassUtil.newInstance(constructor, new Object[] { formatterName });
		formatter.initParameters(args);
		return formatter;
	}

	/**
	 * Enregistrement de formatter
	 *
	 * @param xformatter Définition du formatter
	 * @return DefinitionStandard Définition typée créée.
	 */
	private static Formatter createFormatter(final DynamicDefinition xformatter) {
		//On transforme la liste des paramètres (Liste de String) sous forme de tableau de String pour éviter
		//le sous typage de List et pour se rapprocher de la syntaxe connue de Main.

		final String args = getPropertyValueAsString(xformatter, KspProperty.ARGS);
		final Class<? extends AbstractFormatterImpl> formatterClass = getFormatterClass(xformatter);
		//---------------------------------------------------------------------
		//On instancie un objet formatter.
		return createFormatter(formatterClass, xformatter.getDefinitionKey().getName(), args);
	}

	private Domain createDomain(final DynamicDefinition xdomain) {
		final String formatterUrn = xdomain.getDefinitionKey("formatter").getName();
		final Formatter formatter = definitionSpace.resolve(formatterUrn, Formatter.class);

		final DataType dataType = DataType.valueOf(xdomain.getDefinitionKey("dataType").getName());
		final List<DynamicDefinitionKey> constraintNames = xdomain.getDefinitionKeys("constraint");

		final String urn = xdomain.getDefinitionKey().getName();
		//		final String packageName = xdomain.getPackageName();
		return new Domain(urn, dataType, formatter, createConstraints(constraintNames), extractProperties(xdomain));
	}

	private final Map<String, DtDefinitionBuilder> dtDefinitionBuilders = new HashMap<>();

	/**
	 * @param xdtDefinition Définition de DT
	 */
	private DtDefinition createDtDefinition(final DynamicDefinition xdtDefinition) {
		//Déclaration de la définition
		final String sortFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.SORT_FIELD);
		final String displayFieldName = (String) xdtDefinition.getPropertyValue(KspProperty.DISPLAY_FIELD);
		//----------------------------------------------------------------------
		final Boolean persistent = (Boolean) xdtDefinition.getPropertyValue(KspProperty.PERSISTENT);
		Assertion.checkNotNull(persistent, "Le mot-clé ''persistent'' est obligatoire sur une DtDefinition ({0}).", xdtDefinition.getDefinitionKey().getName());
		//----------------------------------------------------------------------
		final Boolean tmpDynamic = (Boolean) xdtDefinition.getPropertyValue(KspProperty.DYNAMIC);
		//Si DYNAMIC est non renseigné on suppose que le champ est non dynamic.
		final boolean dynamic = tmpDynamic != null && tmpDynamic.booleanValue();
		//----------------------------------------------------------------------
		final String dtDefinitionName = xdtDefinition.getDefinitionKey().getName();
		final DtDefinitionBuilder dtDefinitionBuilder = new DtDefinitionBuilder(dtDefinitionName)//
				.withPackageName(xdtDefinition.getPackageName())//
				.withPersistent(persistent)//
				.withDynamic(dynamic);
		//On enregistre les Builder pour pouvoir les mettre à jour sur les associations.
		Assertion.checkArgument(!dtDefinitionBuilders.containsKey(dtDefinitionName), "Definition '{0}' déjà enregistrée", dtDefinitionName);
		dtDefinitionBuilders.put(dtDefinitionName, dtDefinitionBuilder);

		//Déclaration de la clé primaire
		final List<DynamicDefinition> keys = xdtDefinition.getChildDefinitions(DomainGrammar.PRIMARY_KEY);
		populateIdDtField(dtDefinitionBuilder, keys, sortFieldName, displayFieldName);

		//Déclaration des champs du DT
		final List<DynamicDefinition> fields = xdtDefinition.getChildDefinitions(DomainGrammar.FIELD);
		populateDataDtField(dtDefinitionBuilder, fields, sortFieldName, displayFieldName);

		//Déclaration des champs calculés
		final List<DynamicDefinition> computedFields = xdtDefinition.getChildDefinitions(DomainGrammar.COMPUTED);
		populateComputedDtField(dtDefinitionBuilder, computedFields, sortFieldName, displayFieldName);

		final DtDefinition dtDefinition = dtDefinitionBuilder.build();

		//--Vérification du champ sort et display--
		final boolean sortEmpty = sortFieldName == null && dtDefinition.getSortField().isEmpty();
		final boolean sortNotEmpty = sortFieldName != null && dtDefinition.getSortField().isDefined();

		Assertion.checkState(sortEmpty || sortNotEmpty, "Champ de tri {0} inconnu", sortFieldName);

		final boolean displayEmpty = displayFieldName == null && dtDefinition.getDisplayField().isEmpty();
		final boolean displayNotEmpty = displayFieldName != null && dtDefinition.getDisplayField().isDefined();

		Assertion.checkState(displayEmpty || displayNotEmpty, "Champ d'affichage {0} inconnu", displayFieldName);
		//--Vérification OK
		return dtDefinition;
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private void populateIdDtField(final DtDefinitionBuilder dtDefinitionBuilder, final List<DynamicDefinition> fields, final String sortFieldName, final String displayFieldName) {
		for (final DynamicDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionKey("domain").getName(), Domain.class);
			//--
			Assertion.checkArgument(field.getProperties().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//--
			final String fieldName = field.getDefinitionKey().getName();
			final boolean sort = fieldName.equals(sortFieldName);
			final boolean display = fieldName.equals(displayFieldName);
			//----------------------------------------------------------------------
			dtDefinitionBuilder.withIdField(fieldName, label, domain, sort, display);
		}
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private void populateDataDtField(final DtDefinitionBuilder dtDefinitionBuilder, final List<DynamicDefinition> fields, final String sortFieldName, final String displayFieldName) {
		for (final DynamicDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionKey("domain").getName(), Domain.class);
			//--
			Assertion.checkArgument(field.getProperties().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//--
			final boolean notNull = ((Boolean) field.getPropertyValue(KspProperty.NOT_NULL)).booleanValue();
			Assertion.checkArgument(field.getProperties().contains(KspProperty.NOT_NULL), "Not null est une propriété obligatoire.");
			//--
			final Boolean tmpPersistent = (Boolean) field.getPropertyValue(KspProperty.PERSISTENT);
			//Si PERSISTENT est non renseigné on suppose que le champ est à priori persistant .
			final boolean persistent = tmpPersistent == null || tmpPersistent.booleanValue();
			//--
			final String fieldName = field.getDefinitionKey().getName();
			final boolean sort = fieldName.equals(sortFieldName);
			final boolean display = fieldName.equals(displayFieldName);
			//----------------------------------------------------------------------
			dtDefinitionBuilder.withDataField(fieldName, label, domain, notNull, persistent, sort, display);
		}
	}

	/**
	 * Ajoute une liste de champs d'un certain type à la dtDefinition
	 *
	 * @param fields List
	 */
	private void populateComputedDtField(final DtDefinitionBuilder dtDefinitionBuilder, final List<DynamicDefinition> fields, final String sortFieldName, final String displayFieldName) {
		for (final DynamicDefinition field : fields) {
			final Domain domain = definitionSpace.resolve(field.getDefinitionKey("domain").getName(), Domain.class);
			//--
			Assertion.checkArgument(field.getProperties().contains(KspProperty.LABEL), "Label est une propriété obligatoire");
			final String label = (String) field.getPropertyValue(KspProperty.LABEL);
			//---
			final String expression = (String) field.getPropertyValue(KspProperty.EXPRESSION);
			final ComputedExpression computedExpression = new ComputedExpression(expression);
			//--
			final String fieldName = field.getDefinitionKey().getName();
			final boolean sort = fieldName.equals(sortFieldName);
			final boolean display = fieldName.equals(displayFieldName);

			dtDefinitionBuilder.withComputedField(fieldName, label, domain, computedExpression, sort, display);
		}
	}

	private AssociationDefinition createAssociationNNDefinition(final DynamicDefinition xassociation) {
		final String tableName = getPropertyValueAsString(xassociation, KspProperty.TABLE_NAME);

		final DtDefinition dtDefinitionA = definitionSpace.resolve(xassociation.getDefinitionKey("dtDefinitionA").getName(), DtDefinition.class);
		final boolean navigabilityA = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_A);
		final String roleA = getPropertyValueAsString(xassociation, KspProperty.ROLE_A);
		final String labelA = getPropertyValueAsString(xassociation, KspProperty.LABEL_A);

		final DtDefinition dtDefinitionB = definitionSpace.resolve(xassociation.getDefinitionKey("dtDefinitionB").getName(), DtDefinition.class);
		final boolean navigabilityB = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_B);
		final String roleB = getPropertyValueAsString(xassociation, KspProperty.ROLE_B);
		final String labelB = getPropertyValueAsString(xassociation, KspProperty.LABEL_B);

		final AssociationNode associationNodeA = new AssociationNode(dtDefinitionA, navigabilityA, roleA, labelA, true, false);
		final AssociationNode associationNodeB = new AssociationNode(dtDefinitionB, navigabilityB, roleB, labelB, true, false);
		final String urn = fixAssociationName(xassociation.getDefinitionKey().getName());
		return new AssociationNNDefinition(urn, tableName, associationNodeA, associationNodeB);
	}

	// méthode permettant de créer une liste de contraintes à partir d'une liste de noms de contrainte
	private List<Constraint<?, Object>> createConstraints(final List<DynamicDefinitionKey> constraintKeys) {
		final List<Constraint<?, Object>> constraints = new ArrayList<>(constraintKeys.size());
		for (final DynamicDefinitionKey constraintKey : constraintKeys) {
			constraints.add(definitionSpace.resolve(constraintKey.getName(), Constraint.class));
		}
		return constraints;
	}

	/**
	 * Corrige le nom des associations qui ne respectent pas la règle de nommage.
	 * @param name Nom de l'association
	 * @return Nom corrigé de l'association comprenant le préfix obligatoire.
	 */
	private static String fixAssociationName(final String name) {
		if (!name.startsWith(ASSOCIATION_DEFINITION_PREFIX + SEPARATOR)) {
			return ASSOCIATION_DEFINITION_PREFIX + SEPARATOR + name;
		}
		return name;
	}

	private AssociationDefinition createAssociationSimpleDefinition(final DynamicDefinition xassociation) {
		final String fkFieldName = getPropertyValueAsString(xassociation, KspProperty.FK_FIELD_NAME);

		final DtDefinition dtDefinitionA = definitionSpace.resolve(xassociation.getDefinitionKey("dtDefinitionA").getName(), DtDefinition.class);
		final String multiplicityA = getPropertyValueAsString(xassociation, KspProperty.MULTIPLICITY_A);
		final boolean navigabilityA = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_A).booleanValue();
		final String roleA = getPropertyValueAsString(xassociation, KspProperty.ROLE_A);
		final String labelA = getPropertyValueAsString(xassociation, KspProperty.LABEL_A);

		final DtDefinition dtDefinitionB = definitionSpace.resolve(xassociation.getDefinitionKey("dtDefinitionB").getName(), DtDefinition.class);
		final String multiplicityB = getPropertyValueAsString(xassociation, KspProperty.MULTIPLICITY_B);
		final boolean navigabilityB = getPropertyValueAsBoolean(xassociation, KspProperty.NAVIGABILITY_B).booleanValue();
		final String roleB = getPropertyValueAsString(xassociation, KspProperty.ROLE_B);
		final String labelB = getPropertyValueAsString(xassociation, KspProperty.LABEL_B);

		// Vérification que l'on est bien dans le cas d'une association simple de type 1-n
		if (AssociationUtil.isMultiple(multiplicityB) && AssociationUtil.isMultiple(multiplicityA)) {
			//Relation n-n
			throw new IllegalArgumentException("Utiliser la déclaration AssociationNN");
		}
		if (!AssociationUtil.isMultiple(multiplicityB) && !AssociationUtil.isMultiple(multiplicityA)) {
			//Relation 1-1
			throw new IllegalArgumentException("Les associations 1-1 sont interdites");
		}

		//Relation 1-n ou 1-1
		final String urn = fixAssociationName(xassociation.getDefinitionKey().getName());
		final AssociationSimpleDefinition associationSimpleDefinition = AssociationSimpleDefinition.createAssociationSimpleDefinition(urn, fkFieldName, //
				dtDefinitionA, navigabilityA, roleA, labelA, AssociationUtil.isMultiple(multiplicityA), AssociationUtil.isNotNull(multiplicityA), //
				dtDefinitionB, navigabilityB, roleB, labelB, AssociationUtil.isMultiple(multiplicityB), AssociationUtil.isNotNull(multiplicityB));

		final AssociationNode primaryAssociationNode = associationSimpleDefinition.getPrimaryAssociationNode();
		final AssociationNode foreignAssociationNode = associationSimpleDefinition.getForeignAssociationNode();

		final DtDefinition fkDefinition = primaryAssociationNode.getDtDefinition();

		LOGGER.trace("" + xassociation.getDefinitionKey().getName() + " : ajout d'une FK [" + fkFieldName + "] sur la table '" + foreignAssociationNode.getDtDefinition().getName() + "'");

		final String label = primaryAssociationNode.getLabel();
		dtDefinitionBuilders.get(foreignAssociationNode.getDtDefinition().getName()).withForeignKey(fkFieldName, label, fkDefinition.getIdField().get().getDomain(), primaryAssociationNode.isNotNull(), fkDefinition.getName(), false, false); //On estime qu'une FK n'est ni une colonne de tri ni un champ d'affichage

		return associationSimpleDefinition;
	}

	/**
	 * Extrait le PropertyContainer<DtProperty> d'une DynamicDefinition.
	 * Associe les DtProperty et les KspProperty par leur nom.
	 * @param dynamicDefinition Definition
	 * @return Container des propriétés
	 */
	private static Properties extractProperties(final DynamicDefinition dynamicDefinition) {
		final PropertiesBuilder propertiesBuilder = new PropertiesBuilder();

		//On associe les propriétés Dt et Ksp par leur nom.
		for (final EntityProperty entityProperty : dynamicDefinition.getProperties()) {
			final Property property = DtProperty.valueOf(entityProperty.getName());
			propertiesBuilder.withValue(property, dynamicDefinition.getPropertyValue(entityProperty));
		}
		return propertiesBuilder.build();
	}

	/** {@inheritDoc} */
	@Override
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		if (xdefinition.getEntity().equals(DomainGrammar.DT_DEFINITION_ENTITY)) {
			//Dans le cas des DT on ajoute les domaines
			registerxxxxDomain(xdefinition.getDefinitionKey().getName(), xdefinition.getPackageName(), dynamicModelrepository);
		}
	}

	/*
	 * Construction des deux domaines relatif à un DT : DO_DT_XXX_DTO et DO_DT_XXX_DTC
	 */
	private static void registerxxxxDomain(final String definitionName, final String packageName, final DynamicDefinitionRepository dynamicModelRepository) {
		//C'est le constructeur de DtDomainStandard qui vérifie la cohérence des données passées.
		//Notamment la validité de la liste des contraintes et la nullité du formatter

		final Entity metaDefinitionDomain = DomainGrammar.DOMAIN_ENTITY;
		final DynamicDefinitionKey fmtDefaultKey = new DynamicDefinitionKey(Formatter.FMT_DEFAULT);
		final DynamicDefinitionKey dtObjectKey = new DynamicDefinitionKey("DtObject");

		final DynamicDefinition domain = DynamicDefinitionRepository.createDynamicDefinitionBuilder(DOMAIN_PREFIX + SEPARATOR + definitionName + "_DTO", metaDefinitionDomain, packageName)//
				.withDefinition("formatter", fmtDefaultKey)//
				.withDefinition("dataType", dtObjectKey)//
				//On dit que le domaine possède une prop définissant le type comme étant le nom du DT
				.withPropertyValue(KspProperty.TYPE, definitionName)//
				.build();

		//On ajoute le domain crée au repository
		dynamicModelRepository.addDefinition(domain);

		//On fait la même chose avec DTC

		final DynamicDefinitionKey dtListKey = new DynamicDefinitionKey("DtList");
		final DynamicDefinition domain2 = DynamicDefinitionRepository.createDynamicDefinitionBuilder(DOMAIN_PREFIX + SEPARATOR + definitionName + "_DTC", metaDefinitionDomain, packageName)//
				.withDefinition("formatter", fmtDefaultKey)//
				.withDefinition("dataType", dtListKey)//
				//On dit que le domaine possède une prop définissant le type comme étant le nom du DT
				.withPropertyValue(KspProperty.TYPE, definitionName)//
				.build();

		//On ajoute le domain crée au repository
		dynamicModelRepository.addDefinition(domain2);
	}
}
