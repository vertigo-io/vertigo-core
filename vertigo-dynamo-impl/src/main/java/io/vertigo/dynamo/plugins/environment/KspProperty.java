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
package io.vertigo.dynamo.plugins.environment;

import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.meta.PrimitiveType;
import io.vertigo.kernel.lang.Assertion;

/**
 * Métadonnée liée à la grammaire.
 *
 * @author  pchretien
 */
public enum KspProperty implements EntityProperty {
	/**
	 * Nom de la classe java implémentant un concept tel que formatter, constraint...
	 */
	CLASS_NAME(PrimitiveType.String),

	/**
	 * Arguments initialisant la classe précédante.
	 */
	ARGS(PrimitiveType.String),

	//------------------------------
	//----Constraint
	//------------------------------
	/**
	 * Propriété standard : message d'erreur, valeur String.
	 */
	MSG(PrimitiveType.String),

	//------------------------------
	//----Domain
	//------------------------------
	/**
	 * Propriété standard : libellé du champ, valeur String.
	 */
	LABEL(PrimitiveType.String),
	/**
	 * Propriété standard : champ obligatoire, valeur Boolean.
	 */
	NOT_NULL(PrimitiveType.Boolean),

	/**
	 * Propriété liée au broker : La donnée est-elle gérée en mode write par le Broker.
	 */
	PERSISTENT(PrimitiveType.Boolean),

	/**
	 * Champ qui porte le trie par défaut. (Un au plus par DT)
	 */
	SORT_FIELD(PrimitiveType.String),

	/**
	 * Champ qui porte l'affichage par défaut. (Un au plus par DT)
	 */
	DISPLAY_FIELD(PrimitiveType.String),

	/**
	 * Le DT est-il représentée par un bean ou de maniére générique(dynamique)
	 */
	DYNAMIC(PrimitiveType.Boolean),

	/**
	 * Nom du champ représentant la clé étrangére dans une association simple (non NN).
	 */
	FK_FIELD_NAME(PrimitiveType.String),

	/**
	 * Cardinalité du noeud A dans une association : 1..* ou 0..* ou 1..1.
	 */
	MULTIPLICITY_A(PrimitiveType.String),

	/**
	 * Navigabilité du noeud A dans une association.
	 */
	NAVIGABILITY_A(PrimitiveType.Boolean),

	/**
	 * Nom du réle du noeud A dans une association.
	 */
	ROLE_A(PrimitiveType.String),

	/**
	 * Label du noeud A dans une association.
	 */
	LABEL_A(PrimitiveType.String),

	/**
	 * Cardinalité du noeud B dans une association : 1..* ou 0..* ou 1..1.
	 */
	MULTIPLICITY_B(PrimitiveType.String),

	/**
	 * Navigabilité du noeud B dans une association.
	 */
	NAVIGABILITY_B(PrimitiveType.Boolean),

	/**
	 * Label du noeud B dans une association.
	 */
	LABEL_B(PrimitiveType.String),

	/**
	 * Nom du réle du noeud B dans une association.
	 */
	ROLE_B(PrimitiveType.String),

	/**
	 * Nom de la table supportant l'association NN.
	 */
	TABLE_NAME(PrimitiveType.String),

	//------------------------------
	//----Task
	//------------------------------
	/**
	 * Requête ou plus générallement paramètre d'une tache.
	 */
	REQUEST(PrimitiveType.String),

	/**
	 * Type in ou out d'un attribut de tache.
	 */
	IN_OUT(PrimitiveType.String),

	//------------------------------
	//----FileInfo
	//------------------------------
	/**
	 * Racine des éléments de cette définition.
	 */
	ROOT(PrimitiveType.String),

	/**
	 * Nom du Store utilisé pour cette définition.
	 */
	STORE_NAME(PrimitiveType.String),

	//------------------------------
	//----Mda
	//------------------------------
	/**
	 * Expression du champ computed.
	 */
	EXPRESSION(PrimitiveType.String),

	/**
	 * Type de données en base.
	 */
	STORE_TYPE(PrimitiveType.String),

	//==========================================================================
	//==========================================================================
	//==========================================================================

	//------------------------------
	//----Propriétés hérités de DoProperty
	//------------------------------

	/**
	 * Propriété standard : longueur max du champ, valeur Integer.
	 */
	MAX_LENGTH(PrimitiveType.Integer),

	/**
	 * Propriété standard : Type des définitions.
	 */
	TYPE(PrimitiveType.String),

	/**
	 * Proriété Regex de type String.
	 */
	REGEX(PrimitiveType.String),

	/**
	 * Propriété de contrainte : valeur minimum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date min.
	 */
	MIN_VALUE(PrimitiveType.Double),

	/**
	 * Propriété de contrainte : valeur maximum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date max.
	 */
	MAX_VALUE(PrimitiveType.Double),

	//----------------Style----------------------------------------------------
	/**
	 * Propriété standard : Unité de la valeur, valeur String.
	 */
	UNIT(PrimitiveType.String),

	/**
	 * Propriété standard : Type de l'index. (SOLR par exemple)
	 */
	INDEX_TYPE(PrimitiveType.String);

	//==========================================================================
	//==========================================================================

	/**
	 * Classe java représentant la valeur de la propriété.
	 */
	private final PrimitiveType primitiveType;

	/**
	 * Constructeur à partir du nom évocateur de la propriété.
	 * @param dataType Type Dynamo
	 */
	private KspProperty(final PrimitiveType primitiveType) {
		Assertion.checkNotNull(primitiveType);
		//----------------------------------------------------------------------
		this.primitiveType = primitiveType;
	}

	//==========================================================================

	/** {@inheritDoc} */
	public final PrimitiveType getPrimitiveType() {
		return primitiveType;
	}

	/** {@inheritDoc} */
	public final String getName() {
		return toString();
	}
}
