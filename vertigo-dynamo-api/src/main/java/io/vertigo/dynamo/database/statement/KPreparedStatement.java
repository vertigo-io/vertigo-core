package io.vertigo.dynamo.database.statement;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;

import java.sql.SQLException;

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
public interface KPreparedStatement {
	/**
	 * Type des paramètres.
	 */
	enum ParameterType {
		/** in. */
		IN,
		/** out. */
		OUT,
		/** inout. */
		INOUT
	}

	//--------------------------------------------------------------------
	//------------------1ere Etape : Enregistrement-----------------------
	//--------------------------------------------------------------------

	/**
	 * Ajoute un paramètre en précisant son type.
	 * @param index Indexe du paramètre
	 * @param dataType Type 
	 * @param inOut Type du paramètre
	 */
	void registerParameter(final int index, final DataType dataType, final ParameterType inOut);

	//--------------------------------------------------------------------
	//------------------Clôture des affectations et 1ere Etape -------------------------------
	//--------------------------------------------------------------------
	/**
	 * Construit le PreparedStatement JDBC.
	 * @throws SQLException Si erreur lors de la construction
	 */
	void init() throws SQLException;

	//--------------------------------------------------------------------
	//------------------2ème Etape : Setters------------------------------
	//--------------------------------------------------------------------
	/**
	 * Setter générique.
	 * @param index Indexe du paramètre
	 * @param o Valeur du paramètre
	 * @throws SQLException Exception sql
	 */
	void setValue(final int index, final Object o) throws SQLException;

	//--------------------------------------------------------------------
	//------------------3ème Etape : Exécution------------------------------
	//--------------------------------------------------------------------

	/**
	 * Exécute une requête et délègue l'interprêtation du résultat.
	 * Le Handler est initialisé par KprepareStatement via un appel sur la méthode init.
	 *
	 * @param domain Domain résultat.
	 * @return Résultat comprenant Objet créé (dto ou dtc)
	 * @throws SQLException Exception sql
	 */
	QueryResult executeQuery(final Domain domain) throws SQLException;

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
	Object getGeneratedKey(final String columnName, final Domain domain) throws SQLException;

	/**
	 * Ferme le PreparedStatement.
	 */
	void close();
}
