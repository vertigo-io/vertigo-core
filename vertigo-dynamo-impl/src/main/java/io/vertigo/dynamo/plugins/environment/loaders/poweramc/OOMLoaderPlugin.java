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
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.AssociationOOM;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.AttributeOOM;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.ClassOOM;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.LoaderOOM;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

/**
 * Parser d'un fichier powerAMC, OOM.
 *
 * @author pchretien
 * @version $Id: OOMLoaderPlugin.java,v 1.10 2014/01/20 17:46:41 pchretien Exp $
 */
public final class OOMLoaderPlugin implements LoaderPlugin {
	private static final Logger LOGGER = Logger.getLogger(OOMLoaderPlugin.class);

	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final URL powerAMCURL;
	private final Entity dtDefinitionEntity;
	private final Entity dtFieldEntity;
	private final Entity associationNNEntity;
	private final Entity associationEntity;

	/**
	 * Constructeur.
	 * @param oomFileName Adresse du fichier powerAMC (OOM).
	 */
	@Inject
	public OOMLoaderPlugin(@Named("oom") final String oomFileName, final ResourceManager resourceManager) {
		Assertion.checkArgNotEmpty(oomFileName);
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		final DomainGrammar domainGrammar = DomainGrammar.INSTANCE;
		powerAMCURL = resourceManager.resolve(oomFileName);
		dtDefinitionEntity = domainGrammar.getDtDefinitionEntity();
		dtFieldEntity = domainGrammar.getDtFieldEntity();
		associationNNEntity = domainGrammar.getAssociationNNEntity();
		associationEntity = domainGrammar.getAssociationEntity();
	}

	/** {@inheritDoc} */
	public void load(final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final LoaderOOM loader = new LoaderOOM(powerAMCURL);

		for (final ClassOOM classOOM : loader.getClassOOMList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(classOOM, dynamicModelrepository));
		}

		for (final AssociationOOM associationOOM : loader.getAssociationOOMList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(associationOOM, dynamicModelrepository));
		}
	}

	private DynamicDefinition toDynamicDefinition(final ClassOOM classOOM, final DynamicDefinitionRepository dynamicModelrepository) {

		final DynamicDefinitionBuilder dtDefinitionBuilder = dynamicModelrepository.createDynamicDefinition(getDtDefinitionName(classOOM.getCode()), dtDefinitionEntity, classOOM.getPackageName());
		//Par défaut les DT lues depuis OOM sont persistantes.
		dtDefinitionBuilder.putPropertyValue(KspProperty.PERSISTENT, true);

		for (final AttributeOOM attributeOOM : classOOM.getKeyAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeOOM, dynamicModelrepository);
			dtDefinitionBuilder.addChildDefinition(DomainGrammar.PRIMARY_KEY, dtField);
		}
		for (final AttributeOOM attributeOOM : classOOM.getFieldAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeOOM, dynamicModelrepository);
			dtDefinitionBuilder.addChildDefinition("field", dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private DynamicDefinition toDynamicDefinition(final AttributeOOM attributeOOM, final DynamicDefinitionRepository dynamicModelrepository) {
		final DynamicDefinitionBuilder dtFieldBuilder = dynamicModelrepository.createDynamicDefinition(attributeOOM.getCode(), dtFieldEntity, null);
		dtFieldBuilder.putPropertyValue(KspProperty.LABEL, attributeOOM.getLabel());
		dtFieldBuilder.putPropertyValue(KspProperty.PERSISTENT, attributeOOM.isPersistent());
		dtFieldBuilder.putPropertyValue(KspProperty.NOT_NULL, attributeOOM.isNotNull());
		final DynamicDefinitionKey domainKey = new DynamicDefinitionKey(attributeOOM.getDomain());
		dtFieldBuilder.addDefinition("domain", domainKey);
		return dtFieldBuilder.build();
	}

	private DynamicDefinition toDynamicDefinition(final AssociationOOM associationOOM, final DynamicDefinitionRepository dynamicModelrepository) {
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
		final DynamicDefinitionBuilder associationDefinitionBuilder = dynamicModelrepository.createDynamicDefinition(name, dynamicMetaDefinition, associationOOM.getPackageName());

		associationDefinitionBuilder.putPropertyValue(KspProperty.NAVIGABILITY_A, associationOOM.isNavigableA());
		associationDefinitionBuilder.putPropertyValue(KspProperty.NAVIGABILITY_B, associationOOM.isNavigableB());

		associationDefinitionBuilder.putPropertyValue(KspProperty.LABEL_A, associationOOM.getRoleLabelA());
		//On transforme en CODE ce qui est écrit en toutes lettres.
		associationDefinitionBuilder.putPropertyValue(KspProperty.ROLE_A, OOMUtil.french2Java(associationOOM.getRoleLabelA()));
		associationDefinitionBuilder.putPropertyValue(KspProperty.LABEL_B, associationOOM.getRoleLabelB());
		associationDefinitionBuilder.putPropertyValue(KspProperty.ROLE_B, OOMUtil.french2Java(associationOOM.getRoleLabelB()));

		associationDefinitionBuilder.addDefinition("dtDefinitionA", getDtDefinitionKey(associationOOM.getCodeA()));
		associationDefinitionBuilder.addDefinition("dtDefinitionB", getDtDefinitionKey(associationOOM.getCodeB()));

		if (isAssociationNN) {
			//Dans le cas d'une association NN il faut établir le nom de la table intermédiaire qui porte les relations
			final String tableName = associationOOM.getCode();
			associationDefinitionBuilder.putPropertyValue(KspProperty.TABLE_NAME, tableName);
			LOGGER.trace("isAssociationNN:Code=" + associationOOM.getCode());
		} else {
			LOGGER.trace("!isAssociationNN:Code=" + associationOOM.getCode());
			//Dans le cas d'une NN ses deux propriétés sont redondantes ; 
			//elles ne font donc pas partie de la définition d'une association de type NN
			associationDefinitionBuilder.putPropertyValue(KspProperty.MULTIPLICITY_A, associationOOM.getMultiplicityA());
			associationDefinitionBuilder.putPropertyValue(KspProperty.MULTIPLICITY_B, associationOOM.getMultiplicityB());
			associationDefinitionBuilder.putPropertyValue(KspProperty.FK_FIELD_NAME, buildFkFieldName(associationOOM, dynamicModelrepository));

		}
		return associationDefinitionBuilder.build();
	}

	private String buildFkFieldName(final AssociationOOM associationOOM, final DynamicDefinitionRepository dynamicModelrepository) {
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
		return DT_DEFINITION_PREFIX + SEPARATOR + code.toUpperCase();
	}

	private DynamicDefinitionKey getDtDefinitionKey(final String code) {
		return new DynamicDefinitionKey(getDtDefinitionName(code));
	}
}
