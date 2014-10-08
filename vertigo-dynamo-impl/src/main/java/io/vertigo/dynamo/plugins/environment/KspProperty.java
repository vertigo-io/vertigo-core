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
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityPropertyType;

/**
 * Métadonnée liée à la grammaire.
 *
 * @author  pchretien
 */
public class KspProperty {
	/**
	 * Nom de la classe java implémentant un concept tel que formatter, constraint...
	 */
	public static final EntityProperty CLASS_NAME = new EntityProperty("CLASS_NAME", EntityPropertyType.String);

	/**
	 * Arguments initialisant la classe précédante.
	 */
	public static final EntityProperty ARGS = new EntityProperty("ARGS", EntityPropertyType.String);

	//------------------------------
	//----Constraint
	//------------------------------
	/**
	 * Propriété standard : message d'erreur, valeur String.
	 */
	public static final EntityProperty MSG = new EntityProperty("MSG", EntityPropertyType.String);

	//------------------------------
	//----Domain
	//------------------------------
	/**
	 * Propriété standard : libellé du champ, valeur String.
	 */
	public static final EntityProperty LABEL = new EntityProperty("LABEL", EntityPropertyType.String);
	/**
	 * Propriété standard : champ obligatoire, valeur Boolean.
	 */
	public static final EntityProperty NOT_NULL = new EntityProperty("NOT_NULL", EntityPropertyType.Boolean);

	/**
	 * Propriété liée au broker : La donnée est-elle gérée en mode write par le Broker.
	 */
	public static final EntityProperty PERSISTENT = new EntityProperty("PERSISTENT", EntityPropertyType.Boolean);

	/**
	 * Champ qui porte le trie par défaut. (Un au plus par DT)
	 */
	public static final EntityProperty SORT_FIELD = new EntityProperty("SORT_FIELD", EntityPropertyType.String);

	/**
	 * Champ qui porte l'affichage par défaut. (Un au plus par DT)
	 */
	public static final EntityProperty DISPLAY_FIELD = new EntityProperty("DISPLAY_FIELD", EntityPropertyType.String);

	/**
	 * Le DT est-il représentée par un bean ou de maniére générique(dynamique)
	 */
	public static final EntityProperty DYNAMIC = new EntityProperty("DYNAMIC", EntityPropertyType.Boolean);

	/**
	 * Nom du champ représentant la clé étrangére dans une association simple (non NN).
	 */
	public static final EntityProperty FK_FIELD_NAME = new EntityProperty("FK_FIELD_NAME", EntityPropertyType.String);

	/**
	 * Cardinalité du noeud A dans une association : 1..* ou 0..* ou 1..1.
	 */
	public static final EntityProperty MULTIPLICITY_A = new EntityProperty("MULTIPLICITY_A", EntityPropertyType.String);

	/**
	 * Navigabilité du noeud A dans une association.
	 */
	public static final EntityProperty NAVIGABILITY_A = new EntityProperty("NAVIGABILITY_A", EntityPropertyType.Boolean);

	/**
	 * Nom du réle du noeud A dans une association.
	 */
	public static final EntityProperty ROLE_A = new EntityProperty("ROLE_A", EntityPropertyType.String);

	/**
	 * Label du noeud A dans une association.
	 */
	public static final EntityProperty LABEL_A = new EntityProperty("LABEL_A", EntityPropertyType.String);

	/**
	 * Cardinalité du noeud B dans une association : 1..* ou 0..* ou 1..1.
	 */
	public static final EntityProperty MULTIPLICITY_B = new EntityProperty("MULTIPLICITY_B", EntityPropertyType.String);

	/**
	 * Navigabilité du noeud B dans une association.
	 */
	public static final EntityProperty NAVIGABILITY_B = new EntityProperty("NAVIGABILITY_B", EntityPropertyType.Boolean);

	/**
	 * Label du noeud B dans une association.
	 */
	public static final EntityProperty LABEL_B = new EntityProperty("LABEL_B", EntityPropertyType.String);

	/**
	 * Nom du réle du noeud B dans une association.
	 */
	public static final EntityProperty ROLE_B = new EntityProperty("ROLE_B", EntityPropertyType.String);

	/**
	 * Nom de la table supportant l'association NN.
	 */
	public static final EntityProperty TABLE_NAME = new EntityProperty("TABLE_NAME", EntityPropertyType.String);

	//------------------------------
	//----Task
	//------------------------------
	/**
	 * Requête ou plus générallement paramètre d'une tache.
	 */
	public static final EntityProperty REQUEST = new EntityProperty("REQUEST", EntityPropertyType.String);

	/**
	 * Type in ou out d'un attribut de tache.
	 */
	public static final EntityProperty IN_OUT = new EntityProperty("IN_OUT", EntityPropertyType.String);

	//------------------------------
	//----FileInfo
	//------------------------------
	/**
	 * Racine des éléments de cette définition.
	 */
	public static final EntityProperty ROOT = new EntityProperty("ROOT", EntityPropertyType.String);

	/**
	 * Nom du Store utilisé pour cette définition.
	 */
	public static final EntityProperty STORE_NAME = new EntityProperty("STORE_NAME", EntityPropertyType.String);

	//------------------------------
	//----Mda
	//------------------------------
	/**
	 * Expression du champ computed.
	 */
	public static final EntityProperty EXPRESSION = new EntityProperty("EXPRESSION", EntityPropertyType.String);

	/**
	 * Type de données en base.
	 */
	public static final EntityProperty STORE_TYPE = new EntityProperty("STORE_TYPE", EntityPropertyType.String);

	//==========================================================================
	//==========================================================================
	//==========================================================================

	//------------------------------
	//----Propriétés hérités de DoProperty
	//------------------------------

	/**
	 * Propriété standard : longueur max du champ, valeur Integer.
	 */
	public static final EntityProperty MAX_LENGTH = new EntityProperty("MAX_LENGTH", EntityPropertyType.Integer);

	/**
	 * Propriété standard : Type des définitions.
	 */
	public static final EntityProperty TYPE = new EntityProperty("TYPE", EntityPropertyType.String);

	/**
	 * Proriété Regex de type String.
	 */
	public static final EntityProperty REGEX = new EntityProperty("REGEX", EntityPropertyType.String);

	/**
	 * Propriété de contrainte : valeur minimum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date min.
	 */
	public static final EntityProperty MIN_VALUE = new EntityProperty("MIN_VALUE", EntityPropertyType.Double);

	/**
	 * Propriété de contrainte : valeur maximum, Double.
	 * Dans le cas d'une date, cette propriété contient le timestamp de la date max.
	 */
	public static final EntityProperty MAX_VALUE = new EntityProperty("MAX_VALUE", EntityPropertyType.Double);

	//----------------Style----------------------------------------------------
	/**
	 * Propriété standard : Unité de la valeur, valeur String.
	 */
	public static final EntityProperty UNIT = new EntityProperty("UNIT", EntityPropertyType.String);

	/**
	 * Propriété standard : Type de l'index. (SOLR par exemple)
	 */
	public static final EntityProperty INDEX_TYPE = new EntityProperty("INDEX_TYPE", EntityPropertyType.String);
}
