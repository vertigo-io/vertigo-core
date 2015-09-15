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
package io.vertigo.dynamox.task;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.connection.SqlConnectionProvider;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.transaction.VTransaction;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.dynamox.task.TaskEngineSQLParam.InOutType;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fournit des méthodes de haut niveau pour les services de type SQL.<br>
 * Un Service SQL est composé de paramètre de type primitif, DTO ou DTC, en IN, OUT ou INOUT et d'une requête SQL
 * sous forme de texte.
 * La requête est parsée puis préparée pour replacer les paramètres dynamo  par des variables bindées.
 * Grammaire des requêtes :<br>
 * <code>#<parametre>#</code> : paramètre IN <br>
 * <code>%<parametre>%</code> : paramètre OUT <br>
 * <code>@<parametre>@</code> : paramètre INOUT <br>
 * où <parametre> est : <br>
 * "primitif"     : LON_IDENTIFIANT_ID ou DAT_DATE_SAISIE <br>
 * "champ de dto" : <nom_du_DTO>.<nom_du_champ> : DTO_PERSONNE.NOM ou encore DTO_PERSONNE.PER_ID <br>
 * "champ de dtc" : <nom_du_DTC>.<n°_de_ligne>.<nom_du_champ> : DTC_PERSONNE.2.NOM ou encore DTC_PERSONNE.0.PER_ID <br>
 *  <br>
 * Les DTO et DTC n'ont pas besoin d'être en OUT ou en INOUT pour être mutées. <br>
 *  <br>
 * Intérêt de gérer des paramètres DTC : il existe maintenant un moyen d'accéder aux champs
 * d'une DTC, qui peut être utilisé pour créer des ServiceProviderSQL pseudo-dynamiques (ajout de mots clefs
 * dans la requête SQL du KSP pour gérer des itérations sur DTC par ex). <br>
 *
 * Exemple de requête : <br>
 *
 * SELECT TOTO_ID, NOM  <br>
 * FROM TOTO  <br>
 * WHERE TOTO_ID = #LON_TOTO_ID# <br>
 * AND   NOM like #DTO_FILTRE.NOM#||'%' <br>
 * AND   TYPE_ID IN (#DTC_TYPE.0.TYPE_ID#,#DTC_TYPE.1.TYPE_ID#,#DTC_TYPE.2.TYPE_ID#) <br>
 *
 * De plus permet de créer du SQL dynamiquement interprété.
 * Les paramètres IN de la tache peuvent être invoqués pour construire
 * la requête SQL.
 * Exemple :
 * request = " Select *
 *       From PRODUIT
 *       <%if (dtoProduitCritere.getPrdLibelle()!=null) {%>
 *               Where PRD_LIBELLE like #DTO_PRODUIT_CRITERE.PRD_LIBELLE#||'%%'
 *       <%}%> order by <%=1%>";
 *
 * @author  pchretien, npiedeloup
 * @param <S> Type de Statement utilisé
 */
public abstract class AbstractTaskEngineSQL<S extends SqlPreparedStatement> extends TaskEngine {
	/**
	 * Identifiant de ressource SQL par défaut.
	 */
	public static final VTransactionResourceId<SqlConnection> SQL_RESOURCE_ID = new VTransactionResourceId<>(VTransactionResourceId.Priority.TOP, "Sql");

	/**
	 * Nom de l'attribut recevant le nombre de lignes affectées par un Statement.
	 * Dans le cas des Batchs ce nombre correspond à la somme de toutes les lignes affectées par le batch.
	 */
	//Qui utilise ça ?? // peut on revenir à une forme explicite 
	public static final String SQL_ROWCOUNT = "INT_SQL_ROWCOUNT";

	/**
	 * Liste des séparateurs utilisés dans le traitement des requêtes KSP.
	 */
	private static final List<ScriptSeparator> SQL_SEPARATORS = createSqlSeparators();

	/**
	 * Liste des paramètres
	 */
	private List<TaskEngineSQLParam> params;

	private final ScriptManager scriptManager;
	private final VTransactionManager transactionManager;
	private final SqlDataBaseManager sqlDataBaseManager;

	/**
	 * Constructeur.
	 * @param scriptManager Manager de traitment de scripts
	 */
	protected AbstractTaskEngineSQL(
			final ScriptManager scriptManager,
			final VTransactionManager transactionManager,
			final SqlDataBaseManager sqlDataBaseManager) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(sqlDataBaseManager);
		//-----
		this.scriptManager = scriptManager;
		this.transactionManager = transactionManager;
		this.sqlDataBaseManager = sqlDataBaseManager;
	}

	private static List<ScriptSeparator> createSqlSeparators() {
		return new ListBuilder<ScriptSeparator>()
				.add(new ScriptSeparator(InOutType.SQL_IN.separator))
				.add(new ScriptSeparator(InOutType.SQL_OUT.separator))
				.unmodifiable().build();
	}

	/**
	 * Exécution de la requête.
	 * @param connection Connexion BDD
	 * @param statement Requête
	 * @return Nombre de lignes affectées (Insert/ Update / Delete)
	 * @throws SQLException Erreur sql
	 */
	protected abstract int doExecute(final SqlConnection connection, final S statement) throws SQLException;

	/**
	 * Vérification de la syntaxe sql.
	 * @param sql Syntaxe sql de la requête
	 */
	protected abstract void checkSqlQuery(String sql);

	/** {@inheritDoc} */
	@Override
	public void execute() {
		final SqlConnection connection = obtainConnection();
		final String sql = prepareParams(getSqlQuery().trim());
		checkSqlQuery(sql);

		try (final S statement = createStatement(sql, connection)) {
			//Inialise les paramètres.
			registerParameters(statement);
			try {
				//Initialise le statement JDBC.
				statement.init();
				//Execute le Statement JDBC.
				final int sqlRowcount = doExecute(connection, statement);
				//On positionne le nombre de lignes affectées.
				setRowCount(sqlRowcount);
			} catch (final BatchUpdateException sqle) { //some exception embedded the usefull one
				// Gère les erreurs d'exécution Batch JDBC.
				handleSQLException(connection, sqle.getNextException(), statement);
			} catch (final SQLException sqle) {
				//Gère les erreurs d'exécution JDBC.
				handleSQLException(connection, sqle, statement);
			}
		}
	}

	private void setRowCount(final int sqlRowcount) {
		if (getTaskDefinition().getOutAttributeOption().isDefined()) {
			final TaskAttribute outTaskAttribute = getTaskDefinition().getOutAttributeOption().get();
			if (SQL_ROWCOUNT.equals(outTaskAttribute.getName())) {
				setResult(sqlRowcount);
			}
		}
	}

	//-----
	/**
	 * Retourne la Query qui sera parsée
	 * Par défaut il s'agit de la request définie sur le service
	 * @return Chaine de configuration
	 */
	protected String getSqlQuery() {
		//On ajoute dans la requête SQL le nom de la tache utilisée
		return preProcessQuery(new StringBuilder()
				.append("/* TaskEngine : ")
				.append(getTaskDefinition().getName())
				.append(" */\n")
				.append(getTaskDefinition().getRequest())
				.toString());
	}

	/**
	 * Permet de créer du SQL dynamiquement interprété.
	 * Les paramètres IN de la tache peuvent être invoqués pour construire
	 * la requête SQL.
	 * Exemple :
	 * request = " Select *
	 *       From PRODUIT
	 *       <%if (dtoProduitCritere.getPrdLibelle()!=null) {%>
	 *               Where PRD_LIBELLE like #DTO_PRODUIT_CRITERE.PRD_LIBELLE#||'%%'
	 *       <%}%> order by <%=1%>";
	 * @param sqlQuery Requete à évaluer
	 * @return Requete évaluée
	 **/
	protected final String preProcessQuery(final String sqlQuery) {
		final Collection<TaskAttribute> inAttributes = getTaskDefinition().getInAttributes();
		final Map<TaskAttribute, Object> parameterValuesMap = new HashMap<>(inAttributes.size());
		for (final TaskAttribute taskAttribute : inAttributes) {
			parameterValuesMap.put(taskAttribute, getValue(taskAttribute.getName()));
		}
		//-----
		final ScriptPreProcessor scriptPreProcessor = new ScriptPreProcessor(scriptManager, parameterValuesMap, SeparatorType.CLASSIC);
		final TrimPreProcessor trimPreProcessor = new TrimPreProcessor(SeparatorType.BEGIN_SEPARATOR_CLASSIC, SeparatorType.END_SEPARATOR_CLASSIC);
		final WhereInPreProcessor whereInPreProcessor = new WhereInPreProcessor(parameterValuesMap);
		//--
		String sql = sqlQuery;
		sql = scriptPreProcessor.evaluate(sql);
		sql = trimPreProcessor.evaluate(sql);
		sql = whereInPreProcessor.evaluate(sql);
		return sql;
	}

	/**
	 * Permet de parser la requête afin d'enregistrer les paramètres utilisés
	 *
	 * @return La requête bindée
	 * @param query Requête SQL
	 */
	private String prepareParams(final String query) {
		Assertion.checkNotNull(query); //La requête ne peut pas être nulle
		Assertion.checkState(params == null, "La query a déjà été préparée !");
		//-----
		final SqlParserHandler scriptHandler = new SqlParserHandler(getTaskDefinition());
		scriptManager.parse(query, scriptHandler, SQL_SEPARATORS);
		params = scriptHandler.getParams();
		return scriptHandler.getSql();
	}

	//==========================================================================
	//========================CallableStatement=================================
	//==========================================================================
	/**
	 * Met à jour les paramètres de sorties
	 *
	 * @param cs CallableStatement
	 * @throws SQLException Si erreur */
	protected final void setOutParameters(final SqlCallableStatement cs) throws SQLException {
		Assertion.checkNotNull(cs); //KCallableStatement doit être renseigné
		//-----
		for (final TaskEngineSQLParam param : params) {
			if (!param.isIn()) {
				setOutParameter(cs, param);
			}
		}
	}

	/**
	 * Met à jour une valeur de sortie à partir du résultat de la requête.
	 *
	 * @param cs CallableStatement
	 * @param param Paramètre traité
	 * @throws SQLException Si erreur avec la base
	 */
	private void setOutParameter(final SqlCallableStatement cs, final TaskEngineSQLParam param) throws SQLException {
		final Object value = cs.getValue(param.getIndex());
		setValueParameter(param, value);
	}

	/**
	 * Crée le Statement pour le select ou bloc sql.
	 * Initialise la liste des paramètres en entrée et en sortie
	 *
	 * @param sql Requête SQL
	 * @param connection Connexion vers la base de données
	 * @return Statement StatementSQL
	 */
	protected abstract S createStatement(String sql, SqlConnection connection);

	/**
	 * Initialise les paramètres en entrée du statement
	 * @param statement Statement
	 */
	private void registerParameters(final SqlPreparedStatement statement) {
		for (final TaskEngineSQLParam param : params) {
			statement.registerParameter(param.getIndex(), getDataTypeParameter(param), param.isIn());
		}
	}

	/**
	 * Modifie le statement en fonction des paramètres
	 * Affecte les valeurs en entrée
	 *
	 * @param statement de type KPreparedStatement, KCallableStatement...
	 * @throws SQLException En cas d'erreur dans la configuration
	 */
	protected final void setInParameters(final SqlPreparedStatement statement) throws SQLException {
		Assertion.checkNotNull(statement);
		//-----
		for (final TaskEngineSQLParam param : params) {
			if (param.isIn()) {
				final Integer rowNumber = param.isList() ? param.getRowNumber() : null;
				setInParameter(statement, param, rowNumber);
			}
		}
	}

	/**
	 * @return Liste des paramètres
	 */
	protected final List<TaskEngineSQLParam> getParams() {
		return Collections.unmodifiableList(params);
	}

	/**
	 * Affecte un paramètre au Statement.
	 * @param ps PrepareStatement
	 * @param param Paramètre SQL
	 * @param rowNumber Ligne des données d'entrée.
	 * @throws SQLException Erreur sql
	 */
	protected final void setInParameter(final SqlPreparedStatement ps, final TaskEngineSQLParam param, final Integer rowNumber) throws SQLException {
		ps.setValue(param.getIndex(), getValueParameter(param, rowNumber));
	}

	private DataType getDataTypeParameter(final TaskEngineSQLParam param) {
		final Domain domain;
		if (param.isPrimitive()) {
			// Paramètre primitif
			// TODO reporter l'assertion dans le ServiceProviderSelect
			//if Assertion.invariant((this.getAttribute(paramName).getInOut() & ServiceRegistry.ATTR_IN) > 0, paramName  " must have attribute  ATTR_IN.");
			domain = getTaskDefinition().getInAttribute(param.getAttributeName()).getDomain();
		} else if (param.isObject()) {
			// DtObject
			final DtObject dto = getValue(param.getAttributeName());
			Assertion.checkNotNull(dto);
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
			domain = dtDefinition.getField(param.getFieldName()).getDomain();
		} else if (param.isList()) {
			// DtList
			final DtList<?> dtc = getValue(param.getAttributeName());
			Assertion.checkNotNull(dtc);
			domain = dtc.getDefinition().getField(param.getFieldName()).getDomain();
		} else {
			throw new IllegalStateException(" le param doit être un primitif, un objet ou une liste.");
		}
		return domain.getDataType();
	}

	private void setValueParameter(final TaskEngineSQLParam param, final Object value) {
		if (param.isPrimitive()) {
			Assertion.checkArgument(getTaskDefinition().getOutAttributeOption().isDefined(), "{0} must have one attribute ATTR_OUT", param.getAttributeName());
			setResult(value);
		} else if (param.isObject()) {
			//DtObject
			final DtObject dto = getValue(param.getAttributeName());
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
			final DtField dtField = dtDefinition.getField(param.getFieldName());
			dtField.getDataAccessor().setValue(dto, value);
		} else if (param.isList()) {
			// DtList
			final DtList<? extends DtObject> dtc = getValue(param.getAttributeName());
			final DtObject dto = dtc.get(param.getRowNumber());
			final DtField dtField = dtc.getDefinition().getField(param.getFieldName());
			dtField.getDataAccessor().setValue(dto, value);
		} else {
			throw new IllegalStateException(" le param doit être un primitif, un objet ou une liste.");
		}
	}

	private Object getValueParameter(final TaskEngineSQLParam param, final Integer rowNumber) {
		final Object value;
		if (param.isPrimitive()) {
			value = getValue(param.getAttributeName());
		} else if (param.isObject()) {
			// DtObject
			final DtObject dto = getValue(param.getAttributeName());
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
			final DtField dtField = dtDefinition.getField(param.getFieldName());
			value = dtField.getDataAccessor().getValue(dto);
		} else if (param.isList()) {
			// DtList
			final DtList<? extends DtObject> dtc = getValue(param.getAttributeName());
			final DtObject dto = dtc.get(rowNumber.intValue());
			final DtField dtField = dtc.getDefinition().getField(param.getFieldName());
			value = dtField.getDataAccessor().getValue(dto);
		} else {
			throw new IllegalStateException(" le param doit être un primitif, un objet ou une liste.");
		}
		return value;
	}

	/**
	 * Retourne la connexion SQL de cette transaction en la demandant au pool de connexion si nécessaire.
	 * @return Connexion SQL
	 */
	private SqlConnection obtainConnection() {
		final VTransaction transaction = transactionManager.getCurrentTransaction();
		SqlConnection connection = transaction.getResource(getVTransactionResourceId());
		if (connection == null) {
			// On récupère une connexion du pool
			// Utilise le provider de connexion déclaré sur le Container.
			try {
				connection = getConnectionProvider().obtainConnection();
			} catch (final SQLException e) {
				throw new RuntimeException("Obtention de connexion impossible", e);
			}
			transaction.addResource(getVTransactionResourceId(), connection);
		}
		return connection;
	}

	/**
	 * @return Id de la Ressource Connexion SQL dans la transaction
	 */
	protected VTransactionResourceId<SqlConnection> getVTransactionResourceId() {
		return SQL_RESOURCE_ID;
	}

	/**
	 * @return Manager de base de données
	 */
	protected final SqlDataBaseManager getDataBaseManager() {
		return sqlDataBaseManager;
	}

	/**
	 * Il est possible de surcharger la configuration SQL d'un service.
	 * @return Configuration SQL.
	 */
	protected SqlConnectionProvider getConnectionProvider() {
		return getDataBaseManager().getConnectionProvider();
	}

	/**
	 * Gestion centralisée des exceptions SQL.
	 * @param connection Connexion
	 * @param sqle Exception SQL
	 * @param statement Statement
	 */
	private static void handleSQLException(final SqlConnection connection, final SQLException sqle, final SqlPreparedStatement statement) {
		connection.getDataBase().getSqlExceptionHandler().handleSQLException(sqle, statement);
	}
}
