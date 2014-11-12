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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.loaders.TagAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.TagAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.TagClass;
import io.vertigo.dynamo.plugins.environment.loaders.TagLoader;
import io.vertigo.dynamo.plugins.environment.loaders.TagUtil;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiLoader;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.lang.Assertion;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

public final class EAXmiLoaderPlugin implements LoaderPlugin {
	private static final Logger LOGGER = Logger.getLogger(EAXmiLoaderPlugin.class);

	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final ResourceManager resourceManager;

	/**
	 * Constructeur.
	 */
	@Inject
	public EAXmiLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		this.resourceManager = resourceManager;
	}

	private EAXmiLoader createTagLoader(final URL url) {
		return new EAXmiLoader(url);
	}

	/** {@inheritDoc} */
	public void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final URL url = resourceManager.resolve(resourcePath);

		final TagLoader loader = createTagLoader(url);

		for (final TagClass tagClass : loader.getTagClasses()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(tagClass, dynamicModelrepository));
		}

		for (final TagAssociation tagAssociation : loader.getTagAssociations()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(tagAssociation, dynamicModelrepository));
		}
	}

	private static DynamicDefinition toDynamicDefinition(final TagClass tagClass, final DynamicDefinitionRepository dynamicModelrepository) {
		final Entity dtDefinitionEntity = DomainGrammar.DT_DEFINITION_ENTITY;
		final DynamicDefinitionBuilder dtDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(getDtDefinitionName(tagClass.getCode()), dtDefinitionEntity, tagClass.getPackageName())
				//Par défaut les DT lues depuis le OOM/XMI sont persistantes.
				.withPropertyValue(KspProperty.PERSISTENT, true);

		for (final TagAttribute tagAttribute : tagClass.getKeyAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(tagAttribute, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition(DomainGrammar.PRIMARY_KEY, dtField);
		}
		for (final TagAttribute tagAttribute : tagClass.getFieldAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(tagAttribute, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition(DomainGrammar.FIELD, dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private static DynamicDefinition toDynamicDefinition(final TagAttribute tagAttribute, final DynamicDefinitionRepository dynamicModelrepository) {
		final Entity dtFieldEntity = DomainGrammar.DT_FIELD_ENTITY;
		final DynamicDefinitionKey domainKey = new DynamicDefinitionKey(tagAttribute.getDomain());

		return DynamicDefinitionRepository.createDynamicDefinitionBuilder(tagAttribute.getCode(), dtFieldEntity, null)
				.withPropertyValue(KspProperty.LABEL, tagAttribute.getLabel())
				.withPropertyValue(KspProperty.PERSISTENT, tagAttribute.isPersistent())
				.withPropertyValue(KspProperty.NOT_NULL, tagAttribute.isNotNull())
				.withDefinition("domain", domainKey)
				.build();
	}

	private static DynamicDefinition toDynamicDefinition(final TagAssociation tagAssociation, final DynamicDefinitionRepository dynamicModelrepository) {
		final Entity associationEntity = DomainGrammar.ASSOCIATION_ENTITY;
		final Entity associationNNEntity = DomainGrammar.ASSOCIATION_NN_ENTITY;

		final String name = tagAssociation.getCode().toUpperCase();

		//On regarde si on est dans le cas d'une association simple ou multiple
		final boolean isAssociationNN = AssociationUtil.isMultiple(tagAssociation.getMultiplicityA()) && AssociationUtil.isMultiple(tagAssociation.getMultiplicityB());
		final Entity dynamicMetaDefinition;
		if (isAssociationNN) {
			dynamicMetaDefinition = associationNNEntity;
		} else {
			dynamicMetaDefinition = associationEntity;
		}

		//On crée l'association
		final DynamicDefinitionBuilder associationDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(name, dynamicMetaDefinition, tagAssociation.getPackageName())
				.withPropertyValue(KspProperty.NAVIGABILITY_A, tagAssociation.isNavigableA())
				.withPropertyValue(KspProperty.NAVIGABILITY_B, tagAssociation.isNavigableB())
				//---
				.withPropertyValue(KspProperty.LABEL_A, tagAssociation.getRoleLabelA())
				//On transforme en CODE ce qui est écrit en toutes lettres.
				.withPropertyValue(KspProperty.ROLE_A, TagUtil.french2Java(tagAssociation.getRoleLabelA()))
				.withPropertyValue(KspProperty.LABEL_B, tagAssociation.getRoleLabelB())
				.withPropertyValue(KspProperty.ROLE_B, TagUtil.french2Java(tagAssociation.getRoleLabelB()))
				//---
				.withDefinition("dtDefinitionA", getDtDefinitionKey(tagAssociation.getCodeA()))
				.withDefinition("dtDefinitionB", getDtDefinitionKey(tagAssociation.getCodeB()));

		if (isAssociationNN) {
			//Dans le cas d'une association NN il faut établir le nom de la table intermédiaire qui porte les relations
			final String tableName = tagAssociation.getCode();
			associationDefinitionBuilder.withPropertyValue(KspProperty.TABLE_NAME, tableName);
			LOGGER.trace("isAssociationNN:Code=" + tagAssociation.getCode());
		} else {
			LOGGER.trace("!isAssociationNN:Code=" + tagAssociation.getCode());
			//Dans le cas d'une NN ses deux propriétés sont redondantes ;
			//elles ne font donc pas partie de la définition d'une association de type NN
			associationDefinitionBuilder
					.withPropertyValue(KspProperty.MULTIPLICITY_A, tagAssociation.getMultiplicityA())
					.withPropertyValue(KspProperty.MULTIPLICITY_B, tagAssociation.getMultiplicityB())
					.withPropertyValue(KspProperty.FK_FIELD_NAME, buildFkFieldName(tagAssociation, dynamicModelrepository));

		}
		return associationDefinitionBuilder.build();
	}

	private static String buildFkFieldName(final TagAssociation tagAssociation, final DynamicDefinitionRepository dynamicModelrepository) {
		// Dans le cas d'une association simple, on recherche le nom de la FK
		// recherche de code de contrainte destiné à renommer la fk selon convention du vbsript PowerAMC
		// Cas de la relation 1-n : où le nom de la FK est redéfini.
		// Exemple : DOS_UTI_LIQUIDATION (relation entre dossier et utilisateur : FK >> UTILISATEUR_ID_LIQUIDATION)
		final DynamicDefinition dtDefinitionA = dynamicModelrepository.getDefinition(getDtDefinitionKey(tagAssociation.getCodeA()));
		final DynamicDefinition dtDefinitionB = dynamicModelrepository.getDefinition(getDtDefinitionKey(tagAssociation.getCodeB()));

		final DynamicDefinition foreignDefinition = AssociationUtil.isAPrimaryNode(tagAssociation.getMultiplicityA(), tagAssociation.getMultiplicityB()) ? dtDefinitionA : dtDefinitionB;
		final List<DynamicDefinition> primaryKeys = foreignDefinition.getChildDefinitions(DomainGrammar.PRIMARY_KEY);
		if (primaryKeys.isEmpty()) {
			throw new IllegalArgumentException("Pour l'association '" + tagAssociation.getCode() + "' aucune clé primaire sur la définition '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (primaryKeys.size() > 1) {
			throw new IllegalArgumentException("Pour l'association '" + tagAssociation.getCode() + "' clé multiple non géré sur '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (dtDefinitionA.getDefinitionKey().getName().equals(dtDefinitionB.getDefinitionKey().getName()) && tagAssociation.getCodeName() == null) {
			throw new IllegalArgumentException("Pour l'association '" + tagAssociation.getCode() + "' le nom de la clé est obligatoire (AutoJointure) '" + foreignDefinition.getDefinitionKey().getName() + "'. Ce nom est déduit du code l'association, le code doit être composé ainsi : {Trigramme Table1}_{Trigramme Table2}_{Code association}. Par exemple : DOS_UTI_EMMETEUR, DOS_UTI_DESTINATAIRE, DOS_DOS_PARENT, ...");
		}

		//On récupère le nom de LA clé primaire .
		final String pkFieldName = primaryKeys.get(0).getDefinitionKey().getName();

		//Par défaut le nom de la clé étrangère est constituée de la clé primaire référencée.
		String fkFieldName = pkFieldName;

		//Si l'association possède une nom défini par l'utilisateur, alors on l'ajoute à la FK avec un séparateur.
		if (tagAssociation.getCodeName() != null) {
			//On construit le nom de la clé étrangère.
			fkFieldName = fkFieldName + '_' + tagAssociation.getCodeName();
		}

		//On raccourci le nom de la clé étrangère.
		if (fkFieldName.length() > 30) { // 30 est le max de dynamo (et de Oracle)
			fkFieldName = fkFieldName.substring(0, 30);
			while (fkFieldName.endsWith("_")) {
				fkFieldName = fkFieldName.substring(0, fkFieldName.length() - 1);
			}

		}
		LOGGER.trace(KspProperty.FK_FIELD_NAME + "=" + fkFieldName);
		//-----------------------------------------------------------------
		Assertion.checkNotNull(fkFieldName, "La clé primaire n''a pas pu être définie pour l'association '{0}'", tagAssociation.getCode());
		return fkFieldName;
	}

	private static DynamicDefinitionKey getDtDefinitionKey(final String code) {
		return new DynamicDefinitionKey(getDtDefinitionName(code));
	}

	//-----
	private static String getDtDefinitionName(final String code) {
		return DT_DEFINITION_PREFIX + SEPARATOR + code.toUpperCase();
	}

	public String getType() {
		return "xmi";
	}
}
