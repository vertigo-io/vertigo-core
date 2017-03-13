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
package io.vertigo.dynamo.plugins.store.datastore.sql;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.vendor.SqlDialect;
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
import io.vertigo.dynamo.store.criteria.CriteriaCtx;
import io.vertigo.dynamo.store.criteria.Criterions;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.dynamox.task.sqlserver.TaskEngineInsertWithGeneratedKeys;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;
import io.vertigo.lang.VSystemException;

/**
 * This class is the basic implementation of the dataStore in the sql way.
 *
 * @author  pchretien
 */
public final class SqlDataStorePlugin implements DataStorePlugin {
	private static final int MAX_TASK_SPECIFIC_NAME_LENGTH = 40;
	private static final Criteria EMPTY_CRITERIA = Criterions.alwaysTrue();
	private static final String SEQUENCE_FIELD = "SEQUENCE";

	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final String dataSpace;

	private final String connectionName;

	private final String sequencePrefix;
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

	private final SqlDialect sqlDialect;
	private final TaskManager taskManager;

	/**
	 * Constructor.
	 * @param optDataSpace the dataSpace (option)
	 * @param optConnectionName the name of the connection
	 * @param optSequencePrefix the prefix of sequences
	 * @param taskManager the taskManager
	 * @param sqlDataBaseManager the sqlDataBaseManager
	 */
	@Inject
	public SqlDataStorePlugin(
			@Named("dataSpace") final Optional<String> optDataSpace,
			@Named("connectionName") final Optional<String> optConnectionName,
			@Named("sequencePrefix") final Optional<String> optSequencePrefix,
			final TaskManager taskManager,
			final SqlDataBaseManager sqlDataBaseManager) {
		Assertion.checkNotNull(optDataSpace);
		Assertion.checkNotNull(optConnectionName);
		Assertion.checkNotNull(optSequencePrefix);
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(sqlDataBaseManager);
		//-----
		dataSpace = optDataSpace.orElse(StoreManager.MAIN_DATA_SPACE_NAME);
		connectionName = optConnectionName.orElse(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME);
		sequencePrefix = optSequencePrefix.orElse("SEQ_");
		this.taskManager = taskManager;
		sqlDialect = sqlDataBaseManager.getConnectionProvider(connectionName).getDataBase().getSqlDialect();
		integerDomain = new DomainBuilder("DO_INTEGER_SQL", DataType.Integer).build();
	}

	/**
	 * Return the tableName to which the dtDefinition is mapped.
	 *
	 * @param dtDefinition the dtDefinition
	 * @return the name of the table
	 */
	private static String getTableName(final DtDefinition dtDefinition) {
		return dtDefinition.getFragment().orElse(dtDefinition).getLocalName();
	}

	private static String getRequestedFields(final DtDefinition dtDefinition) {
		if (dtDefinition.getFragment().isPresent()) {
			return dtDefinition.getFields()
					.stream()
					.map(DtField::getName)
					.collect(Collectors.joining(", "));
		}
		return "*"; //all fields
	}

	/** {@inheritDoc} */
	@Override
	public String getDataSpace() {
		return dataSpace;
	}

	/** {@inheritDoc} */
	@Override
	public String getConnectionName() {
		return connectionName;
	}

	private static DtField getIdField(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---
		return dtDefinition.getIdField().orElseThrow(() -> new IllegalStateException("no ID found"));
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E readNullable(final DtDefinition dtDefinition, final URI<E> uri) {
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_SELECT + "_" + dtDefinition.getLocalName() + "_BY_URI";

		final String requestedFields = getRequestedFields(dtDefinition);
		final DtField idField = getIdField(dtDefinition);
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
		final String idFieldName = getIdField(dtDefinition).getName();
		//FK dans la table nn correspondant à la collection recherchée. (clé de jointure ).
		final AssociationNNDefinition associationNNDefinition = dtcUri.getAssociationDefinition();
		final String joinTableName = associationNNDefinition.getTableName();
		final DtDefinition joinDtDefinition = AssociationUtil.getAssociationNode(associationNNDefinition, dtcUri.getRoleName()).getDtDefinition();
		final DtField joinDtField = getIdField(joinDtDefinition);

		//La condition s'applique sur l'autre noeud de la relation (par rapport à la collection attendue)
		final AssociationNode associationNode = AssociationUtil.getAssociationNodeTarget(associationNNDefinition, dtcUri.getRoleName());
		final DtField fkField = getIdField(associationNode.getDtDefinition());
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

		return findByCriteria(dtDefinition, Criterions.isEqualTo(fkField, value), null);
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

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findByCriteria(final DtDefinition dtDefinition, final Criteria<E> criteria, final Integer maxRows) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(criteria);
		//-----
		final String tableName = getTableName(dtDefinition);
		final String requestedFields = getRequestedFields(dtDefinition);
		final String taskName = getListTaskName(tableName);
		final Tuples.Tuple2<String, CriteriaCtx> tuple = criteria.toSql(sqlDialect);
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

	private static String getListTaskName(final String tableName) {
		final String fullName = new StringBuilder(TASK.TK_SELECT.name())
				.append("_LIST_")
				.append(tableName)
				.append("_BY_CRITERIA")
				.toString();
		if (fullName.length() > MAX_TASK_SPECIFIC_NAME_LENGTH) {
			return fullName.substring(0, MAX_TASK_SPECIFIC_NAME_LENGTH);
		}
		return fullName;
	}

	//==========================================================================
	//=============================== Ecriture =================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public void create(final DtDefinition dtDefinition, final Entity entity) {
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
	public void update(final DtDefinition dtDefinition, final Entity entity) {
		Assertion.checkNotNull(DtObjectUtil.getId(entity), "Need an id to update an object ");
		//-----
		final boolean insert = false;
		final boolean saved = put(entity, insert);
		if (!saved) {
			throw new VSystemException("no data updated");
		}
	}

	/**
	 * Creates the update request.
	 *
	 * @param dtDefinition the dtDefinition
	 * @return the sql request
	 */
	private static String createUpdateQuery(final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final DtField idField = getIdField(dtDefinition);

		return new StringBuilder()
				.append("update ").append(tableName).append(" set ")

				.append(dtDefinition.getFields()
						.stream()
						.filter(dtField -> dtField.isPersistent() && dtField.getType() != DtField.FieldType.ID)
						.map(dtField -> dtField.getName() + " =#DTO." + dtField.getName() + '#')
						.collect(Collectors.joining(", ")))
				.append(" where ")
				.append(idField.getName()).append(" = #DTO.").append(idField.getName()).append('#')
				.toString();
	}

	private long buildNextSequence(final String sequenceName, final String query) {
		final String taskName = TASK.TK_SELECT.name() + '_' + sequenceName;
		final Domain resultDomain = new DomainBuilder("DO_HSQL", DataType.Long).build();

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(getDataSpace())
				.withRequest(query)
				.withOutRequired(SEQUENCE_FIELD, resultDomain)
				.build();

		final Task task = new TaskBuilder(taskDefinition).build();

		return taskManager
				.execute(task)
				.getResult();
	}

	/**
	 * @param insert Si opération de type insert
	 * @return Classe du moteur de tache à utiliser
	 */
	private Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		if (insert && sqlDialect.generatedKeys()) {
			return TaskEngineInsertWithGeneratedKeys.class;
		}
		return TaskEngineProc.class;
	}

	/**
	 * @param entity Objet à persiter
	 * @param insert Si opération de type insert (update sinon)
	 * @return Si "1 ligne sauvée", sinon "Aucune ligne sauvée"
	 */
	private boolean put(final Entity entity, final boolean insert) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final String tableName = getTableName(dtDefinition);
		if (insert) {
			//Pour les SGBDs ne possédant pas de système de séquence il est nécessaire de calculer la clé en amont.
			final Optional<String> optQuery = sqlDialect.createPrimaryKeyQuery(tableName, sequencePrefix);
			if (optQuery.isPresent()) {
				final long sequence = buildNextSequence(sequencePrefix + tableName, optQuery.get());
				final DtField idField = dtDefinition.getIdField().orElseThrow(() -> new IllegalStateException("no ID found"));
				idField.getDataAccessor().setValue(entity, sequence);
			}
		}

		final String taskName = (insert ? TASK.TK_INSERT : TASK.TK_UPDATE) + "_" + tableName;

		final String request = insert ? sqlDialect.createInsertQuery(dtDefinition, sequencePrefix, tableName) : createUpdateQuery(dtDefinition);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(getTaskEngineClass(insert))
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

	/** {@inheritDoc} */
	@Override
	public void delete(final DtDefinition dtDefinition, final URI uri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(uri);
		//---
		final DtField idField = getIdField(dtDefinition);
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
			throw new VSystemException("more than one row has been deleted");
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
	public <E extends Entity> E readNullableForUpdate(final DtDefinition dtDefinition, final URI<?> uri) {
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_LOCK + "_" + tableName;

		final String requestedFields = getRequestedFields(dtDefinition);
		final DtField idField = getIdField(dtDefinition);
		final String idFieldName = idField.getName();
		final String request = sqlDialect.createSelectForUpdateQuery(tableName, requestedFields, idFieldName);

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

	private String createLoadAllLikeQuery(
			final String tableName,
			final String requestedFields,
			final String where,
			final Integer maxRows) {

		final StringBuilder request = new StringBuilder("select ")
				.append(requestedFields)
				.append(" from ").append(tableName)
				.append(" where ").append(where);
		if (maxRows != null) {
			// the criteria is not null so the where is not empty at least 1=1 for alwaysTrue
			sqlDialect.appendMaxRows(request, maxRows);
		}
		return request.toString();
	}
}
