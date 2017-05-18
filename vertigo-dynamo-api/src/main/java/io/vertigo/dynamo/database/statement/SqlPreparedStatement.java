/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.database.statement;

import java.sql.SQLException;
import java.util.List;

/**
 * PreparedStatement.
 * Il s'agit d'une encapsulation du preparedStatement Java
 * On peut ainsi tracer toutes les exécution de requêtes
 * On peut aussi débugger les requêtes en listant les paramètres en entrée : ce qui n'est pas possible sur preparedStatement de base.
 *
 * L'appel s'effectue selon les étapes suivantes :
 * - Création
 * - Définition des paramètres : addParameter
 * - Clôture de la définition des paramètres : init()
 * - Exécution de la requête
 * - Récupération des paramètres de sorties </li> (Pour KCallableStatement uniquement)
 *
 * @author pchretien
 */
public interface SqlPreparedStatement extends AutoCloseable {

	//=========================================================================
	//-----1ère Etape : Setters
	//=========================================================================
	/**
	 * Setter générique.
	 * @param index Indexe du paramètre
	 * @param o Valeur du paramètre
	 * @throws SQLException Exception sql
	 */
	<O> void setValue(final int index, final Class<O> dataType, final O value) throws SQLException;

	//=========================================================================
	//-----2ème Etape : Exécution
	//=========================================================================

	/**
	 * Exécute une requête et délègue l'interprêtation du résultat.
	 * Le Handler est initialisé par KprepareStatement via un appel sur la méthode init.
	 *
	 * @param domain Domain résultat.
	 * @return Résultat comprenant Objet créé (dto ou dtc)
	 * @throws SQLException Exception sql
	 */
	<O> List<O> executeQuery(final Class<O> dataType, final Integer limit) throws SQLException;

	/**
	 * Exécute la requête.
	 *
	 * @throws SQLException Si erreur
	 * @return either the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
	 */
	int executeUpdate() throws SQLException;

	/**
	 * Ajoute le traitement dans la liste des traitements batchs.
	 * @throws SQLException Si erreur
	 */
	void addBatch() throws SQLException;

	/**
	 * Execute le traitement batch.
	 * @throws SQLException Si erreur
	 * @return Nombre total de INSERT, UPDATE ou DELETE; 0 pour les traitements qui ne font rien.
	 */
	int executeBatch() throws SQLException;

	/**
	 * Donne les clefs générées lors d'un insert.
	 * @param columnName Nom de la colonne de la clé
	 * @param domain Domain de la clé
	 * @throws SQLException Exception SQL
	 */
	<O> O getGeneratedKey(final String columnName, final Class<O> dataType) throws SQLException;

	/**
	 * Ferme le PreparedStatement.
	 */
	@Override
	void close();
}
