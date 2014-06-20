package io.vertigo.dynamo.plugins.environment.loaders.poweramc;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMClass;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMLoader;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

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
	 * @param oomFileName Adresse du fichier powerAMC (OOM).
	 */
	@Inject
	public OOMLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		this.resourceManager = resourceManager;
		final DomainGrammar domainGrammar = DomainGrammar.INSTANCE;
		dtDefinitionEntity = domainGrammar.getDtDefinitionEntity();
		dtFieldEntity = domainGrammar.getDtFieldEntity();
		associationNNEntity = domainGrammar.getAssociationNNEntity();
		associationEntity = domainGrammar.getAssociationEntity();
	}

	/** {@inheritDoc} */
	public void load(final String resource, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resource);
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final URL powerAMCURL = resourceManager.resolve(resource);

		final OOMLoader loader = new OOMLoader(powerAMCURL);

		for (final OOMClass classOOM : loader.getClassOOMList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(classOOM, dynamicModelrepository));
		}

		for (final OOMAssociation associationOOM : loader.getAssociationOOMList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(associationOOM, dynamicModelrepository));
		}
	}

	private DynamicDefinition toDynamicDefinition(final OOMClass classOOM, final DynamicDefinitionRepository dynamicModelrepository) {

		final DynamicDefinitionBuilder dtDefinitionBuilder = dynamicModelrepository.createDynamicDefinitionBuilder(getDtDefinitionName(classOOM.getCode()), dtDefinitionEntity, classOOM.getPackageName());
		//Par défaut les DT lues depuis OOM sont persistantes.
		dtDefinitionBuilder.withPropertyValue(KspProperty.PERSISTENT, true);

		for (final OOMAttribute attributeOOM : classOOM.getKeyAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeOOM, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition(DomainGrammar.PRIMARY_KEY, dtField);
		}
		for (final OOMAttribute attributeOOM : classOOM.getFieldAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeOOM, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition("field", dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private DynamicDefinition toDynamicDefinition(final OOMAttribute attributeOOM, final DynamicDefinitionRepository dynamicModelrepository) {
		final DynamicDefinitionKey domainKey = new DynamicDefinitionKey(attributeOOM.getDomain());

		return dynamicModelrepository.createDynamicDefinitionBuilder(attributeOOM.getCode(), dtFieldEntity, null)//
				.withPropertyValue(KspProperty.LABEL, attributeOOM.getLabel())//
				.withPropertyValue(KspProperty.PERSISTENT, attributeOOM.isPersistent())//
				.withPropertyValue(KspProperty.NOT_NULL, attributeOOM.isNotNull())//
				.withDefinition("domain", domainKey)//
				.build();
	}

	private DynamicDefinition toDynamicDefinition(final OOMAssociation associationOOM, final DynamicDefinitionRepository dynamicModelrepository) {
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
		final DynamicDefinitionBuilder associationDefinitionBuilder = dynamicModelrepository.createDynamicDefinitionBuilder(name, dynamicMetaDefinition, associationOOM.getPackageName())//
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

	private String buildFkFieldName(final OOMAssociation associationOOM, final DynamicDefinitionRepository dynamicModelrepository) {
		// Dans le cas d'une association simple, on recherche le nom de la FK
		// recherche de code de contrainte destiné à renommer la fk selon convention du vbsript PowerAMC
		// Cas de la relation 1-n : où le nom de la FK est redéfini.
		// Exemple : DOS_UTI_LIQUIDATION (relation entre dossier et utilisateur : FK >> UTILISATEUR_ID_LIQUIDATION)
		final DynamicDefinition dtDefinitionA = dynamicModelrepository.getDefinition(getDtDefinitionKey(associationOOM.getCodeA()));
		final DynamicDefinition dtDefinitionB = dynamicModelrepository.getDefinition(getDtDefinitionKey(associationOOM.getCodeB()));

		final DynamicDefinition foreignDefinition = AssociationUtil.isAPrimaryNode(associationOOM.getMultiplicityA(), associationOOM.getMultiplicityB()) ? dtDefinitionA : dtDefinitionB;
		final List<DynamicDefinition> primaryKeyList = foreignDefinition.getChildDefinitions(DomainGrammar.PRIMARY_KEY);
		if (primaryKeyList.isEmpty()) {
			throw new IllegalArgumentException("Pour l'association '" + associationOOM.getCode() + "' aucune clé primaire sur la définition '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (primaryKeyList.size() > 1) {
			throw new IllegalArgumentException("Pour l'association '" + associationOOM.getCode() + "' clé multiple non géré sur '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (dtDefinitionA.getDefinitionKey().getName().equals(dtDefinitionB.getDefinitionKey().getName()) && associationOOM.getCodeName() == null) {
			throw new IllegalArgumentException("Pour l'association '" + associationOOM.getCode() + "' le nom de la clé est obligatoire (AutoJointure) '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}

		//On récupère le nom de LA clé primaire . 
		final String pkFieldName = primaryKeyList.get(0).getDefinitionKey().getName();

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

	private String getDtDefinitionName(final String code) {
		return DT_DEFINITION_PREFIX + SEPARATOR + code.toUpperCase(TO_UPPER_CASE_LOCALE);
	}

	private DynamicDefinitionKey getDtDefinitionKey(final String code) {
		return new DynamicDefinitionKey(getDtDefinitionName(code));
	}
	
	public String getType() {
		return "oom";
	}

}
