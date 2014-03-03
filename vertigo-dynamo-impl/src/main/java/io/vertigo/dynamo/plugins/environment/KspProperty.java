package io.vertigo.dynamo.plugins.environment;

import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.kernel.lang.Assertion;

/**
 * Métadonnée liée à la grammaire.
 *
 * @author  pchretien
 * @version $Id: KspProperty.java,v 1.4 2014/01/20 17:46:11 pchretien Exp $
 *
 */
public enum KspProperty implements EntityProperty {
	/**
	 * Nom de la classe java implémentant un concept tel que formatter, constraint...
	 */
	CLASS_NAME(KDataType.String),

	/**
	 * Arguments initialisant la classe précédante.
	 */
	ARGS(KDataType.String),

	//------------------------------
	//----Constraint
	//------------------------------
	/**
	 * Propriété standard : message d'erreur, valeur String.
	 */
	MSG(KDataType.String),

	//------------------------------
	//----Domain
	//------------------------------
	/**
	 * Propriété standard : libellé du champ, valeur String.
	 */
	LABEL(KDataType.String),
	/**
	 * Propriété standard : champ obligatoire, valeur Boolean.
	 */
	NOT_NULL(KDataType.Boolean),

	/**
	 * Propriété liée au broker : La donnée est-elle gérée en mode write par le Broker.
	 */
	PERSISTENT(KDataType.Boolean),

	/**
	 * Champ qui porte le trie par défaut. (Un au plus par DT)
	 */
	SORT_FIELD(KDataType.String),

	/**
	 * Champ qui porte l'affichage par défaut. (Un au plus par DT)
	 */
	DISPLAY_FIELD(KDataType.String),

	/**
	 * Le DT est-il représentée par un bean ou de maniére générique(dynamique)
	 */
	DYNAMIC(KDataType.Boolean),

	/**
	 * Nom du champ représentant la clé étrangére dans une association simple (non NN).
	 */
	FK_FIELD_NAME(KDataType.String),

	/**
	 * Cardinalité du noeud A dans une association : 1..* ou 0..* ou 1..1.
	 */
	MULTIPLICITY_A(KDataType.String),

	/**
	 * Navigabilité du noeud A dans une association.
	 */
	NAVIGABILITY_A(KDataType.Boolean),

	/**
	 * Nom du réle du noeud A dans une association.
	 */
	ROLE_A(KDataType.String),

	/**
	 * Label du noeud A dans une association.
	 */
	LABEL_A(KDataType.String),

	/**
	 * Cardinalité du noeud B dans une association : 1..* ou 0..* ou 1..1.
	 */
	MULTIPLICITY_B(KDataType.String),

	/**
	 * Navigabilité du noeud B dans une association.
	 */
	NAVIGABILITY_B(KDataType.Boolean),

	/**
	 * Label du noeud B dans une association.
	 */
	LABEL_B(KDataType.String),

	/**
	 * Nom du réle du noeud B dans une association.
	 */
	ROLE_B(KDataType.String),

	/**
	 * Nom de la table supportant l'association NN.
	 */
	TABLE_NAME(KDataType.String),

	//------------------------------
	//----Task
	//------------------------------
	/**
	 * Requête ou plus générallement paramètre d'une tache.
	 */
	REQUEST(KDataType.String),

	/**
	 * Type in ou out d'un attribut de tache.
	 */
	IN_OUT(KDataType.String),

	//------------------------------
	//----FileInfo
	//------------------------------
	/**
	 * Racine des éléments de cette définition.
	 */
	ROOT(KDataType.String),

	/**
	 * Nom du Store utilisé pour cette définition.
	 */
	STORE_NAME(KDataType.String),

	//------------------------------
	//----Mda
	//------------------------------
	/**
	 * Expression du champ computed.
	 */
	EXPRESSION(KDataType.String),

	//==========================================================================
	//==========================================================================
	//==========================================================================

	//------------------------------
	//----Propriétés hérités de DoProperty
	//------------------------------

	/**
	 * Propriété standard : longueur max du champ, valeur Integer.
	 */
	MAX_LENGTH(KDataType.Integer),

	/**
	 * Propriété standard : Type des définitions.
	 */
	TYPE(KDataType.String),

	/**
	 * Proriété Regex de type String.
	 */
	REGEX(KDataType.String),

	/**
	 * Propriété de contrainte : valeur minimum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date min.
	 */
	MIN_VALUE(KDataType.Double),

	/**
	 * Propriété de contrainte : valeur maximum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date max.
	 */
	MAX_VALUE(KDataType.Double),

	//----------------Style----------------------------------------------------
	/**
	 * Propriété standard : Unité de la valeur, valeur String.
	 */
	UNIT(KDataType.String),

	/**
	 * Propriété standard : Type de l'index. (SOLR par exemple)
	 */
	INDEX_TYPE(KDataType.String);

	//==========================================================================
	//==========================================================================

	/**
	 * Classe java représentant la valeur de la propriété.
	 */
	private final KDataType dataType;

	/**
	 * Constructeur à partir du nom évocateur de la propriété.
	 * @param dataType Type Dynamo
	 */
	private KspProperty(final KDataType dataType) {
		Assertion.checkNotNull(dataType);
		//----------------------------------------------------------------------
		this.dataType = dataType;
	}

	//==========================================================================

	/** {@inheritDoc} */
	public final KDataType getDataType() {
		return dataType;
	}

	/** {@inheritDoc} */
	public final String getName() {
		return toString();
	}
}
