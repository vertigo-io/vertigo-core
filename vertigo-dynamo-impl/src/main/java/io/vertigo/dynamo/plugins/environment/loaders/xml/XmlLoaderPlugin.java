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
package io.vertigo.dynamo.plugins.environment.loaders.xml;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionBuilder;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.core.definition.loader.LoaderPlugin;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.OOMLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.lang.Assertion;

/**
 * Parser d'un fichier powerAMC/OOM ou EA/XMI.
 *
 * @author pchretien
 */
public abstract class XmlLoaderPlugin implements LoaderPlugin {
	private static final Locale TO_UPPER_CASE_LOCALE = Locale.FRANCE;
	private static final Logger LOGGER = Logger.getLogger(OOMLoaderPlugin.class);

	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;
	private final ResourceManager resourceManager;

	/**
	 * Constructeur.
	 */
	public XmlLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//-----
		this.resourceManager = resourceManager;
	}

	protected abstract XmlLoader createLoader(final URL url);

	/** {@inheritDoc} */
	@Override
	public final void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//-----
		final URL url = resourceManager.resolve(resourcePath);

		final XmlLoader loader = createLoader(url);

		for (final XmlClass clazz : loader.getClasses()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(clazz));
		}

		for (final XmlAssociation association : loader.getAssociations()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(association, dynamicModelrepository));
		}
	}

	private static DynamicDefinition toDynamicDefinition(final XmlClass clazz) {
		final DslEntity dtDefinitionEntity = DomainGrammar.DT_DEFINITION_ENTITY;
		final DynamicDefinitionBuilder dtDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(getDtDefinitionName(clazz.getCode()), dtDefinitionEntity, clazz.getPackageName())
				//Par défaut les DT lues depuis le OOM/XMI sont persistantes.
				.addPropertyValue(KspProperty.PERSISTENT, true)
				.addPropertyValue(KspProperty.STEREOTYPE, clazz.getStereotype());

		for (final XmlAttribute attribute : clazz.getKeyAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attribute);
			dtDefinitionBuilder.addChildDefinition(DomainGrammar.ID, dtField);
		}
		for (final XmlAttribute tagAttribute : clazz.getFieldAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(tagAttribute);
			dtDefinitionBuilder.addChildDefinition(DomainGrammar.FIELD, dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private static DynamicDefinition toDynamicDefinition(final XmlAttribute attribute) {
		final DslEntity dtFieldEntity = DomainGrammar.DT_FIELD_ENTITY;

		return DynamicDefinitionRepository.createDynamicDefinitionBuilder(attribute.getCode(), dtFieldEntity, null)
				.addPropertyValue(KspProperty.LABEL, attribute.getLabel())
				.addPropertyValue(KspProperty.PERSISTENT, attribute.isPersistent())
				.addPropertyValue(KspProperty.NOT_NULL, attribute.isNotNull())
				.addDefinitionLink("domain", attribute.getDomain())
				.build();
	}

	private static DynamicDefinition toDynamicDefinition(final XmlAssociation association, final DynamicDefinitionRepository dynamicModelrepository) {
		final DslEntity associationEntity = DomainGrammar.ASSOCIATION_ENTITY;
		final DslEntity associationNNEntity = DomainGrammar.ASSOCIATION_NN_ENTITY;

		final String name = association.getCode().toUpperCase();

		//On regarde si on est dans le cas d'une association simple ou multiple
		final boolean isAssociationNN = AssociationUtil.isMultiple(association.getMultiplicityA()) && AssociationUtil.isMultiple(association.getMultiplicityB());
		final DslEntity dynamicMetaDefinition;
		if (isAssociationNN) {
			dynamicMetaDefinition = associationNNEntity;
		} else {
			dynamicMetaDefinition = associationEntity;
		}

		//On crée l'association
		final DynamicDefinitionBuilder associationDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(name, dynamicMetaDefinition, association.getPackageName())
				.addPropertyValue(KspProperty.NAVIGABILITY_A, association.isNavigableA())
				.addPropertyValue(KspProperty.NAVIGABILITY_B, association.isNavigableB())
				//---
				.addPropertyValue(KspProperty.LABEL_A, association.getRoleLabelA())
				//On transforme en CODE ce qui est écrit en toutes lettres.
				.addPropertyValue(KspProperty.ROLE_A, XmlUtil.french2Java(association.getRoleLabelA()))
				.addPropertyValue(KspProperty.LABEL_B, association.getRoleLabelB())
				.addPropertyValue(KspProperty.ROLE_B, XmlUtil.french2Java(association.getRoleLabelB()))
				//---
				.addDefinitionLink("dtDefinitionA", getDtDefinitionName(association.getCodeA()))
				.addDefinitionLink("dtDefinitionB", getDtDefinitionName(association.getCodeB()));

		if (isAssociationNN) {
			//Dans le cas d'une association NN il faut établir le nom de la table intermédiaire qui porte les relations
			final String tableName = association.getCode();
			associationDefinitionBuilder.addPropertyValue(KspProperty.TABLE_NAME, tableName);
			LOGGER.trace("isAssociationNN:Code=" + association.getCode());
		} else {
			LOGGER.trace("!isAssociationNN:Code=" + association.getCode());
			//Dans le cas d'une NN ses deux propriétés sont redondantes ;
			//elles ne font donc pas partie de la définition d'une association de type NN
			associationDefinitionBuilder
					.addPropertyValue(KspProperty.MULTIPLICITY_A, association.getMultiplicityA())
					.addPropertyValue(KspProperty.MULTIPLICITY_B, association.getMultiplicityB())
					.addPropertyValue(KspProperty.FK_FIELD_NAME, buildFkFieldName(association, dynamicModelrepository));

		}
		return associationDefinitionBuilder.build();
	}

	private static String buildFkFieldName(final XmlAssociation association, final DynamicDefinitionRepository dynamicModelrepository) {
		// Dans le cas d'une association simple, on recherche le nom de la FK
		// recherche de code de contrainte destiné à renommer la fk selon convention du vbsript PowerAMC
		// Cas de la relation 1-n : où le nom de la FK est redéfini.
		// Exemple : DOS_UTI_LIQUIDATION (relation entre dossier et utilisateur : FK >> UTILISATEUR_ID_LIQUIDATION)
		final DynamicDefinition dtDefinitionA = dynamicModelrepository.getDefinition(getDtDefinitionName(association.getCodeA()));
		final DynamicDefinition dtDefinitionB = dynamicModelrepository.getDefinition(getDtDefinitionName(association.getCodeB()));

		final DynamicDefinition foreignDefinition = AssociationUtil.isAPrimaryNode(association.getMultiplicityA(), association.getMultiplicityB()) ? dtDefinitionA : dtDefinitionB;
		final List<DynamicDefinition> primaryKeys = foreignDefinition.getChildDefinitions(DomainGrammar.ID);
		if (primaryKeys.isEmpty()) {
			throw new IllegalArgumentException("Pour l'association '" + association.getCode() + "' aucune clé primaire sur la définition '" + foreignDefinition.getName() + "'");
		}
		if (primaryKeys.size() > 1) {
			throw new IllegalArgumentException("Pour l'association '" + association.getCode() + "' clé multiple non géré sur '" + foreignDefinition.getName() + "'");
		}
		if (dtDefinitionA.getName().equals(dtDefinitionB.getName()) && association.getCodeName() == null) {
			throw new IllegalArgumentException("Pour l'association '" + association.getCode() + "' le nom de la clé est obligatoire (AutoJointure) '" + foreignDefinition.getName() + "'. Ce nom est déduit du code l'association, le code doit être composé ainsi : {Trigramme Table1}_{Trigramme Table2}_{Code association}. Par exemple : DOS_UTI_EMMETEUR, DOS_UTI_DESTINATAIRE, DOS_DOS_PARENT, ...");
		}

		//On récupère le nom de LA clé primaire .
		final String pkFieldName = primaryKeys.get(0).getName();

		//Par défaut le nom de la clé étrangère est constituée de la clé primaire référencée.
		String fkFieldName = pkFieldName;

		//Si l'association possède une nom défini par l'utilisateur, alors on l'ajoute à la FK avec un séparateur.
		if (association.getCodeName() != null) {
			//On construit le nom de la clé étrangère.
			fkFieldName = fkFieldName + '_' + association.getCodeName();
		}

		//On raccourci le nom de la clé étrangère.
		if (fkFieldName.length() > 30) { // 30 est le max de dynamo (et de Oracle)
			fkFieldName = fkFieldName.substring(0, 30);
			while (fkFieldName.endsWith("_")) {
				fkFieldName = fkFieldName.substring(0, fkFieldName.length() - 1);
			}

		}
		LOGGER.trace(KspProperty.FK_FIELD_NAME + "=" + fkFieldName);
		//-----
		Assertion.checkNotNull(fkFieldName, "La clé primaire n''a pas pu être définie pour l'association '{0}'", association.getCode());
		return fkFieldName;
	}

	private static String getDtDefinitionName(final String code) {
		return DT_DEFINITION_PREFIX + SEPARATOR + code.toUpperCase(TO_UPPER_CASE_LOCALE);
	}

	//	public String getType() {
	//		return "oom";
	//	}
}
