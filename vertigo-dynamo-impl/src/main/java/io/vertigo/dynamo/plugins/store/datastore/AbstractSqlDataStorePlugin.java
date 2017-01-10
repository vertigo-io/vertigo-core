/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.store.datastore;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.app.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DomainBuilder;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.Criterions;
import io.vertigo.dynamo.store.criteria.CriteriaCtx;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;
import io.vertigo.lang.VSystemException;

/**
 * This class is the basic implementation of the dataStore in the sql way.
 *
 * @author  pchretien
 */
public abstract class AbstractSqlDataStorePlugin implements DataStorePlugin {
	private static final int MAX_TASK_SPECIFIC_NAME_LENGTH = 40;
	private static final Criteria EMPTY_CRITERIA = Criterions.alwaysTrue();

	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final String dataSpace;

	private final String connectionName;
	/**
	 * Domaine à usage interne.
	 * Ce domaine n'est pas enregistré.
	 */
	private final Domain integerDomain;

	private enum TASK {
		/** Prefix of the SELECT.*/
		TK_SELECT,
		/** Prefix of the INSERT.*/
		TK_INSERT,
		/** Prefix of the UPDATE.*/
		TK_UPDATE,
		/** Prefix of the DELETE.*/
		TK_DELETE,
		/** Prefix of the COUNT.*/
		TK_COUNT,
		/** Prefix of the LOCK.*/
		TK_LOCK
	}

	private final TaskManager taskManager;

	/**
	 * Constructor.
	 * @param dataSpaceOption the dataSpace (option)
	 * @param connectionName the name of the connection
	 * @param taskManager the taskManager
	 */
	protected AbstractSqlDataStorePlugin(final Optional<String> dataSpaceOption, final Optional<String> connectionName, final TaskManager taskManager) {
		Assertion.checkNotNull(dataSpaceOption);
		Assertion.checkNotNull(connectionName);
		Assertion.checkNotNull(taskManager);
		//-----
		dataSpace = dataSpaceOption.orElse(StoreManager.MAIN_DATA_SPACE_NAME);
		this.connectionName = connectionName.orElse(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME);
		this.taskManager = taskManager;
		integerDomain = new DomainBuilder("DO_INTEGER_SQL", DataType.Integer).build();
	}

	/**
	 * Return the tableName to which the dtDefinition is mapped.
	 *
	 * @param dtDefinition the dtDefinition
	 * @return the name of the table
	 */
	protected static final String getTableName(final DtDefinition dtDefinition) {
		return dtDefinition.getFragment().orElse(dtDefinition).getLocalName();
	}

	protected static String getRequestedField(final DtDefinition dtDefinition) {
		if (dtDefinition.getFragment().isPresent()) {
			return dtDefinition.getFields()
					.stream()
					.map(dtField -> dtField.getName())
					.collect(Collectors.joining(", "));
		}
		return "*"; //all fields
	}

	/** {@inheritDoc} */
	@Override
	public final String getDataSpace() {
		return dataSpace;
	}

	/** {@inheritDoc} */
	@Override
	public final String getConnectionName() {
		return connectionName;
	}

	protected final TaskManager getTaskManager() {
		return taskManager;
	}

	/** {@inheritDoc} */
	@Override
	public final <E extends Entity> E readNullable(final DtDefinition dtDefinition, final URI<E> uri) {
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_SELECT + "_" + dtDefinition.getLocalName() + "_BY_URI";

		final String requestedFields = getRequestedField(dtDefinition);
		final DtField idField = dtDefinition.getIdField().get();
		final String idFieldName = idField.getName();
		final String request = new StringBuilder()
				.append(" select ").append(requestedFields)
				.append(" from ").append(tableName)
				.append(" where ").append(idFieldName).append(" = #").append(idFieldName).append('#')
				.toString();

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.addInRequired(idFieldName, idField.getDomain())
				.withOutOptional("dto", Home.getApp().getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + uri.getDefinition().getName() + "_DTO", Domain.class))
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue(idFieldName, uri.getId())
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForNNAssociation dtcUri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtcUri);
		//-----
		final String tableName = getTableName(dtDefinition);

		final String taskName = TASK.TK_SELECT + "_N_N_LIST_" + tableName + "_BY_URI";

		//PK de la DtList recherchée
		final String idFieldName = dtDefinition.getIdField().get().getName();
		//FK dans la table nn correspondant à la collection recherchée. (clé de jointure ).
		final AssociationNNDefinition associationNNDefinition = dtcUri.getAssociationDefinition();
		final String joinTableName = associationNNDefinition.getTableName();
		final DtDefinition joinDtDefinition = AssociationUtil.getAssociationNode(associationNNDefinition, dtcUri.getRoleName()).getDtDefinition();
		final DtField joinDtField = joinDtDefinition.getIdField().get();

		//La condition s'applique sur l'autre noeud de la relation (par rapport à la collection attendue)
		final AssociationNode associationNode = AssociationUtil.getAssociationNodeTarget(associationNNDefinition, dtcUri.getRoleName());
		final DtField fkField = associationNode.getDtDefinition().getIdField().get();
		final String fkFieldName = fkField.getName();

		final String request = new StringBuilder(" select t.* from ")
				.append(tableName).append(" t")
				//On établit une jointure fermée entre la pk et la fk de la collection recherchée.
				.append(" join ").append(joinTableName).append(" j on j.").append(joinDtField.getName()).append(" = t.").append(idFieldName)
				//Condition de la recherche
				.append(" where j.").append(fkFieldName).append(" = #").append(fkFieldName).append('#')
				.toString();

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.addInRequired(fkFieldName, fkField.getDomain())
				.withOutRequired("dtc", Home.getApp().getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTC", Domain.class))
				.build();

		final URI uri = dtcUri.getSource();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue(fkFieldName, uri.getId())
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForSimpleAssociation dtcUri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtcUri);
		//-----
		final DtField fkField = dtcUri.getAssociationDefinition().getFKField();
		final Comparable value = (Comparable) dtcUri.getSource().getId();

		return findByCriteria(dtDefinition, Criterions.isEqualTo(() -> fkField.getName(), value), null);
	}

	/**
	 * Ajoute à la requete les éléments techniques nécessaire pour limiter le resultat à {maxRows}.
	 * @param separator Séparateur de la close where à utiliser
	 * @param request Buffer de la requete
	 * @param maxRows Nombre de lignes max
	 */
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForCriteria<E> uri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(uri);
		//-----
		final Criteria<E> criteria = uri.getCriteria();
		final Integer maxRows = uri.getMaxRows();
		//-----
		final Criteria<E> filterCriteria = criteria == null ? EMPTY_CRITERIA : criteria;
		return findByCriteria(dtDefinition, filterCriteria, maxRows);
	}

	protected String getConcatOperator() {
		//default
		return " || ";
	}

	private static <E extends Entity> String getListTaskName(final String tableName, final Criteria<E> criteria) {
		return getListTaskName(tableName, criteria.toSql().getVal2().getAttributeNames());
	}

	private static <E extends Entity> String getListTaskName(final String tableName, final Set<String> criteriaFieldNames) {
		final StringBuilder sb = new StringBuilder()
				.append("LIST_")
				.append(tableName);

		//si il y a plus d'un champs : on nomme _BY_CRITERIA, sinon le nom sera trop long
		if (criteriaFieldNames.size() <= 1) {
			String sep = "_BY_";
			for (final String filterName : criteriaFieldNames) {
				sb.append(sep);
				sb.append(filterName);
				sep = "_AND_";
			}
		} else {
			sb.append("_BY_CRITERIA");
		}
		String result = sb.toString();
		if (result.length() > MAX_TASK_SPECIFIC_NAME_LENGTH) {
			result = result.substring(result.length() - MAX_TASK_SPECIFIC_NAME_LENGTH);
		}
		return result;
	}

	//==========================================================================
	//=============================== Ecriture =================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public final void create(final DtDefinition dtDefinition, final Entity entity) {
		Assertion.checkArgument(DtObjectUtil.getId(entity) == null, "Only object without any id can be created");
		//------
		final boolean insert = true;
		final boolean saved = put(entity, insert);
		if (!saved) {
			throw new VSystemException("no data created");
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void update(final DtDefinition dtDefinition, final Entity entity) {
		Assertion.checkNotNull(DtObjectUtil.getId(entity), "Need an id to update an object ");
		//-----
		final boolean insert = false;
		final boolean saved = put(entity, insert);
		if (!saved) {
			throw new VSystemException("no data updated");
		}
	}

	/**
	 * Creates the insert request.
	 *
	 * @param dtDefinition the dtDefinition
	 * @return the sql request
	 */
	protected abstract String createInsertQuery(final DtDefinition dtDefinition);

	/**
	 * Creates the update request.
	 *
	 * @param dtDefinition the dtDefinition
	 * @return the sql request
	 */
	private static String createUpdateQuery(final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final DtField idField = dtDefinition.getIdField().get();
		final StringBuilder request = new StringBuilder()
				.append("update ").append(tableName).append(" set ");
		String separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			//On ne met à jour que les champs persistants hormis la PK
			if (dtField.isPersistent() && dtField.getType() != DtField.FieldType.ID) {
				request.append(separator);
				request.append(dtField.getName()).append(" = #DTO.").append(dtField.getName()).append('#');
				separator = ", ";
			}
		}
		request.append(" where ").append(idField.getName()).append(" = #DTO.").append(idField.getName()).append('#');
		return request.toString();
	}

	/**
	 * @param insert Si opération de type insert
	 * @return Classe du moteur de tache à utiliser
	 */
	protected abstract Class<? extends TaskEngine> getTaskEngineClass(final boolean insert);

	/**
	 * @param entity Objet à persiter
	 * @param insert Si opération de type insert (update sinon)
	 * @return Si "1 ligne sauvée", sinon "Aucune ligne sauvée"
	 */
	private boolean put(final Entity entity, final boolean insert) {
		if (insert) {
			//Pour les SGBDs ne possédant pas de système de séquence il est nécessaire de calculer la clé en amont.
			preparePrimaryKey(entity);
		}
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);

		final String tableName = getTableName(dtDefinition);
		final String taskName = (insert ? TASK.TK_INSERT : TASK.TK_UPDATE) + "_" + tableName;

		final String request = insert ? createInsertQuery(dtDefinition) : createUpdateQuery(dtDefinition);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(getTaskEngineClass(insert))//IN, obligatoire
				.withDataSpace(dataSpace)
				.withRequest(request)
				.addInRequired("DTO", Home.getApp().getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTO", Domain.class))
				.withOutRequired(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerDomain)
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue("DTO", entity)
				.build();

		final int sqlRowCount = taskManager
				.execute(task)
				.getResult();

		if (sqlRowCount > 1) {
			throw new VSystemException(insert ? "more than one row has been inserted" : "more than one row has been updated");
		}
		return sqlRowCount != 0; // true si "1 ligne sauvée", false si "Aucune ligne sauvée"
	}

	/**
	 * Prépare la PK si il n'y a pas de système de sequence.
	 * @param entity Objet à sauvegarder (création ou modification)
	 */
	protected void preparePrimaryKey(final Entity entity) {
		// rien par default
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final DtDefinition dtDefinition, final URI uri) {
		final DtField idField = dtDefinition.getIdField().get();
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_DELETE + "_" + tableName;

		final String idFieldName = idField.getName();

		final String request = new StringBuilder()
				.append("delete from ").append(tableName)
				.append(" where ").append(idFieldName).append(" = #").append(idFieldName).append('#')
				.toString();

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineProc.class)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.addInRequired(idFieldName, idField.getDomain())
				.withOutRequired(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerDomain)
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue(idFieldName, uri.getId())
				.build();

		final int sqlRowCount = taskManager
				.execute(task)
				.getResult();

		if (sqlRowCount > 1) {
			throw new VSystemException("more tha one row has been deleted");
		} else if (sqlRowCount == 0) {
			throw new VSystemException("no row has been deleted");
		}
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkArgument(dtDefinition.isPersistent(), "DtDefinition is not  persistent");
		//-----
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_COUNT + "_" + tableName;
		final Domain countDomain = new DomainBuilder("DO_COUNT", DataType.Long).build();

		final String request = "select count(*) from " + tableName;

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.withOutRequired("count", countDomain)
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.build();

		final Long count = taskManager
				.execute(task)
				.getResult();

		return count.intValue();
	}

	/** {@inheritDoc} */
	@Override
	public final <E extends Entity> E readNullableForUpdate(final DtDefinition dtDefinition, final URI<?> uri) {
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_LOCK + "_" + tableName;

		final String requestedFields = getRequestedField(dtDefinition);
		final DtField idField = dtDefinition.getIdField().get();
		final String idFieldName = idField.getName();
		final String request = getSelectForUpdate(tableName, requestedFields, idFieldName);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.addInRequired(idFieldName, idField.getDomain())
				//IN, obligatoire
				.withOutOptional("dto", Home.getApp().getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + uri.getDefinition().getName() + "_DTO", Domain.class))
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue(idFieldName, uri.getId())
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

	/**
	 * Requête à exécuter pour faire un select for update. Doit pouvoir être surchargé pour tenir compte des
	 * spécificités de la base de données utilisée..
	 * @param tableName nom de la table
	 * @param idFieldName nom de la clé primaire
	 * @return select à exécuter.
	 */
	protected String getSelectForUpdate(final String tableName, final String requestedFields, final String idFieldName) {
		return new StringBuilder()
				.append(" select ").append(requestedFields)
				.append(" from ").append(tableName)
				.append(" where ").append(idFieldName).append(" = #").append(idFieldName).append('#')
				.append(" for update ")
				.toString();
	}

	@Override
	public <E extends Entity> DtList<E> findByCriteria(final DtDefinition dtDefinition, final Criteria<E> criteria, final Integer maxRows) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(criteria);
		//-----
		final String tableName = getTableName(dtDefinition);
		final String requestedFields = getRequestedField(dtDefinition);
		final String taskName = "TK_TEST2";
		final Tuples.Tuple2<String, CriteriaCtx> tuple = criteria.toSql();
		final String where = tuple.getVal1();
		final String request = createLoadAllLikeQuery(tableName, requestedFields, where, maxRows);
		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(dataSpace)
				.withRequest(request);

		final CriteriaCtx ctx = tuple.getVal2();
		//IN, obligatoire
		for (final String attributeName : ctx.getAttributeNames()) {
			taskDefinitionBuilder.addInRequired(attributeName, dtDefinition.getField(ctx.getDtFieldName(attributeName)).getDomain());
		}
		//OUT, obligatoire
		final TaskDefinition taskDefinition = taskDefinitionBuilder
				.withOutRequired("dtc", Home.getApp().getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTC", Domain.class))
				.build();

		final TaskBuilder taskBuilder = new TaskBuilder(taskDefinition);
		for (final String attributeName : ctx.getAttributeNames()) {
			taskBuilder.addValue(attributeName, ctx.getAttributeValue(attributeName));
		}
		return taskManager
				.execute(taskBuilder.build())
				.getResult();
	}

	private String createLoadAllLikeQuery(final String tableName, final String requestedFields, final String where, final Integer maxRows) {
		final StringBuilder request = new StringBuilder("select ").append(requestedFields)
				.append(" from ").append(tableName);
		request.append(" where ").append(where);
		if (maxRows != null) {
			// the criteria is not null so the where is not empty at least 1=1 for alwaysTrue
			appendMaxRows(" and ", request, maxRows);
		}
		return request.toString();
	}

}
