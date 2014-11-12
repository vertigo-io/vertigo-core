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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc;

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
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMLoader;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.lang.Assertion;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * Parser d'un fichier powerAMC, OOM.
 *
 * @author pchretien
 */
public final class OOMLoaderPlugin implements LoaderPlugin {
	private static final Locale TO_UPPER_CASE_LOCALE = Locale.FRANCE;
	private static final Logger LOGGER = Logger.getLogger(OOMLoaderPlugin.class);
	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final Entity dtDefinitionEntity;
	private final Entity dtFieldEntity;
	private final Entity associationNNEntity;
	private final Entity associationEntity;
	private final ResourceManager resourceManager;

	/**
	 * Constructeur.
	 */
	@Inject
	public OOMLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		this.resourceManager = resourceManager;
		dtDefinitionEntity = DomainGrammar.DT_DEFINITION_ENTITY;
		dtFieldEntity = DomainGrammar.DT_FIELD_ENTITY;
		associationNNEntity = DomainGrammar.ASSOCIATION_NN_ENTITY;
		associationEntity = DomainGrammar.ASSOCIATION_ENTITY;
	}

	/** {@inheritDoc} */
	public void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final URL powerAMCURL = resourceManager.resolve(resourcePath);

		final OOMLoader loader = new OOMLoader(powerAMCURL);

		for (final TagClass classOOM : loader.getClassOOMList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(classOOM, dynamicModelrepository));
		}

		for (final TagAssociation associationOOM : loader.getAssociationOOMList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(associationOOM, dynamicModelrepository));
		}
	}

	private DynamicDefinition toDynamicDefinition(final TagClass classOOM, final DynamicDefinitionRepository dynamicModelrepository) {

		final DynamicDefinitionBuilder dtDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(getDtDefinitionName(classOOM.getCode()), dtDefinitionEntity, classOOM.getPackageName());
		//Par défaut les DT lues depuis OOM sont persistantes.
		dtDefinitionBuilder.withPropertyValue(KspProperty.PERSISTENT, true);

		for (final TagAttribute attributeOOM : classOOM.getKeyAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeOOM, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition(DomainGrammar.PRIMARY_KEY, dtField);
		}
		for (final TagAttribute attributeOOM : classOOM.getFieldAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeOOM, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition("field", dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private DynamicDefinition toDynamicDefinition(final TagAttribute attributeOOM, final DynamicDefinitionRepository dynamicModelrepository) {
		final DynamicDefinitionKey domainKey = new DynamicDefinitionKey(attributeOOM.getDomain());

		return DynamicDefinitionRepository.createDynamicDefinitionBuilder(attributeOOM.getCode(), dtFieldEntity, null)//
				.withPropertyValue(KspProperty.LABEL, attributeOOM.getLabel())//
				.withPropertyValue(KspProperty.PERSISTENT, attributeOOM.isPersistent())//
				.withPropertyValue(KspProperty.NOT_NULL, attributeOOM.isNotNull())//
				.withDefinition("domain", domainKey)//
				.build();
	}

	private DynamicDefinition toDynamicDefinition(final TagAssociation associationOOM, final DynamicDefinitionRepository dynamicModelrepository) {
		//			final DynamicDefinition associationDefinition = dynamicModelrepository.createDynamicDefinition(name,
		//					return associationDefinition;
		final String name = associationOOM.getCode().toUpperCase();

		//On regarde si on est dans le cas d'une association simple ou multiple
		final boolean isAssociationNN = AssociationUtil.isMultiple(associationOOM.getMultiplicityA()) && AssociationUtil.isMultiple(associationOOM.getMultiplicityB());
		final Entity dynamicMetaDefinition;
		if (isAssociationNN) {
			dynamicMetaDefinition = associationNNEntity;
		} else {
			dynamicMetaDefinition = associationEntity;
		}

		//On crée l'association
		final DynamicDefinitionBuilder associationDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(name, dynamicMetaDefinition, associationOOM.getPackageName())//
				.withPropertyValue(KspProperty.NAVIGABILITY_A, associationOOM.isNavigableA())//
				.withPropertyValue(KspProperty.NAVIGABILITY_B, associationOOM.isNavigableB())//
				//---
				.withPropertyValue(KspProperty.LABEL_A, associationOOM.getRoleLabelA())//
				//On transforme en CODE ce qui est écrit en toutes lettres.
				.withPropertyValue(KspProperty.ROLE_A, OOMUtil.french2Java(associationOOM.getRoleLabelA()))//
				.withPropertyValue(KspProperty.LABEL_B, associationOOM.getRoleLabelB())//
				.withPropertyValue(KspProperty.ROLE_B, OOMUtil.french2Java(associationOOM.getRoleLabelB()))//
				//---
				.withDefinition("dtDefinitionA", getDtDefinitionKey(associationOOM.getCodeA()))//
				.withDefinition("dtDefinitionB", getDtDefinitionKey(associationOOM.getCodeB()));

		if (isAssociationNN) {
			//Dans le cas d'une association NN il faut établir le nom de la table intermédiaire qui porte les relations
			final String tableName = associationOOM.getCode();
			associationDefinitionBuilder.withPropertyValue(KspProperty.TABLE_NAME, tableName);
			LOGGER.trace("isAssociationNN:Code=" + associationOOM.getCode());
		} else {
			LOGGER.trace("!isAssociationNN:Code=" + associationOOM.getCode());
			//Dans le cas d'une NN ses deux propriétés sont redondantes ;
			//elles ne font donc pas partie de la définition d'une association de type NN
			associationDefinitionBuilder.withPropertyValue(KspProperty.MULTIPLICITY_A, associationOOM.getMultiplicityA())//
					.withPropertyValue(KspProperty.MULTIPLICITY_B, associationOOM.getMultiplicityB())//
					.withPropertyValue(KspProperty.FK_FIELD_NAME, buildFkFieldName(associationOOM, dynamicModelrepository));

		}
		return associationDefinitionBuilder.build();
	}

	private static String buildFkFieldName(final TagAssociation associationOOM, final DynamicDefinitionRepository dynamicModelrepository) {
		// Dans le cas d'une association simple, on recherche le nom de la FK
		// recherche de code de contrainte destiné à renommer la fk selon convention du vbsript PowerAMC
		// Cas de la relation 1-n : où le nom de la FK est redéfini.
		// Exemple : DOS_UTI_LIQUIDATION (relation entre dossier et utilisateur : FK >> UTILISATEUR_ID_LIQUIDATION)
		final DynamicDefinition dtDefinitionA = dynamicModelrepository.getDefinition(getDtDefinitionKey(associationOOM.getCodeA()));
		final DynamicDefinition dtDefinitionB = dynamicModelrepository.getDefinition(getDtDefinitionKey(associationOOM.getCodeB()));

		final DynamicDefinition foreignDefinition = AssociationUtil.isAPrimaryNode(associationOOM.getMultiplicityA(), associationOOM.getMultiplicityB()) ? dtDefinitionA : dtDefinitionB;
		final List<DynamicDefinition> primaryKeys = foreignDefinition.getChildDefinitions(DomainGrammar.PRIMARY_KEY);
		if (primaryKeys.isEmpty()) {
			throw new IllegalArgumentException("Pour l'association '" + associationOOM.getCode() + "' aucune clé primaire sur la définition '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (primaryKeys.size() > 1) {
			throw new IllegalArgumentException("Pour l'association '" + associationOOM.getCode() + "' clé multiple non géré sur '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (dtDefinitionA.getDefinitionKey().getName().equals(dtDefinitionB.getDefinitionKey().getName()) && associationOOM.getCodeName() == null) {
			throw new IllegalArgumentException("Pour l'association '" + associationOOM.getCode() + "' le nom de la clé est obligatoire (AutoJointure) '" + foreignDefinition.getDefinitionKey().getName() + "'. Ce nom est déduis du code l'association, le code doit être composé ainsi : {Trigramme Table1}_{Trigramme Table2}_{Code association}. Par exemple : DOS_UTI_EMMETEUR, DOS_UTI_DESTINATAIRE, DOS_DOS_PARENT, ...");
		}

		//On récupère le nom de LA clé primaire .
		final String pkFieldName = primaryKeys.get(0).getDefinitionKey().getName();

		//Par défaut le nom de la clé étrangére est constituée de la clé primaire référencée.
		String fkFieldName = pkFieldName;

		//Si l'association possède une nom défini par l'utilisateur, alors on l'ajoute à la FK avec un séparateur.
		if (associationOOM.getCodeName() != null) {
			//On construit le nom de la clé étrangére.
			fkFieldName = fkFieldName + '_' + associationOOM.getCodeName();
		}

		//On raccourci le nom de la clé étrangére.
		if (fkFieldName.length() > 30) { // 30 est le max de dynamo (et de Oracle)
			fkFieldName = fkFieldName.substring(0, 30);
			while (fkFieldName.endsWith("_")) {
				fkFieldName = fkFieldName.substring(0, fkFieldName.length() - 1);
			}

		}
		LOGGER.trace(KspProperty.FK_FIELD_NAME + "=" + fkFieldName);
		//-----------------------------------------------------------------
		Assertion.checkNotNull(fkFieldName, "La clé primaire n''a pas pu être définie pour l'association '{0}'", associationOOM.getCode());
		return fkFieldName;
	}

	private static String getDtDefinitionName(final String code) {
		return DT_DEFINITION_PREFIX + SEPARATOR + code.toUpperCase(TO_UPPER_CASE_LOCALE);
	}

	private static DynamicDefinitionKey getDtDefinitionKey(final String code) {
		return new DynamicDefinitionKey(getDtDefinitionName(code));
	}

	public String getType() {
		return "oom";
	}

}
