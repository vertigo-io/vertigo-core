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
package io.vertigo.dynamo.impl.persistence.util;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.persistence.datastore.BrokerNN;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.lang.Assertion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestion des tables de relations NN.
 * @author dchallas
 */
public final class BrokerNNImpl implements BrokerNN {
	private final Domain integerDomain;
	private final TaskManager taskManager;

	private static final class DescriptionNN {
		final String tableName;
		final DtField sourceField;
		final Object sourceValue;
		final DtField targetField;

		DescriptionNN(final DtListURIForNNAssociation dtListURIForAssociation) {
			Assertion.checkNotNull(dtListURIForAssociation);
			final AssociationNNDefinition associationNNDefinition = dtListURIForAssociation.getAssociationDefinition();

			//Par rapport à l'objet on distingue la source et la cible.
			final AssociationNode sourceAssociationNode = AssociationUtil.getAssociationNodeTarget(associationNNDefinition, dtListURIForAssociation.getRoleName());
			sourceField = sourceAssociationNode.getDtDefinition().getIdField().get();

			//Clés primaires de la relation n-n.
			final AssociationNode targetAssociationNode = AssociationUtil.getAssociationNode(associationNNDefinition, dtListURIForAssociation.getRoleName());
			targetField = targetAssociationNode.getDtDefinition().getIdField().get();
			sourceValue = dtListURIForAssociation.getSource().getKey();
			tableName = associationNNDefinition.getTableName();
		}
	}

	/**
	 * Constructeur.
	 * @param taskManager Manager des Tasks
	 */
	public BrokerNNImpl(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//-----
		this.taskManager = taskManager;
		integerDomain = new Domain("DO_INTEGER_BROKER", DataType.Integer);
	}

	/** {@inheritDoc} */
	@Override
	public void appendNN(final DtListURIForNNAssociation dtListURI, final URI uriToAppend) {
		Assertion.checkNotNull(uriToAppend);
		//-----
		appendNN(new DescriptionNN(dtListURI), uriToAppend.getKey());
	}

	/** {@inheritDoc} */
	@Override
	public void removeAllNN(final DtListURIForNNAssociation dtListURI) {
		// on supprime tout
		removeNN(new DescriptionNN(dtListURI));
	}

	/** {@inheritDoc} */
	@Override
	public void removeNN(final DtListURIForNNAssociation dtListURI, final URI uriToDelete) {
		Assertion.checkNotNull(uriToDelete);
		//-----
		removeNN(new DescriptionNN(dtListURI), uriToDelete.getKey());
	}

	/** {@inheritDoc} */
	@Override
	public void updateNN(final DtListURIForNNAssociation dtListURI, final List<URI> newUriList) {
		Assertion.checkNotNull(newUriList);
		//-----
		final DescriptionNN descriptionNN = new DescriptionNN(dtListURI);
		//1. on supprime tout
		removeNN(descriptionNN);
		//2. on enregistre la liste actuelle (un par un)
		final Set<URI> set = new HashSet<>();
		for (final URI dtoUri : newUriList) {
			//On vérifie que l'on n'enregistre pas deux fois la même relation.
			Assertion.checkArgument(set.add(dtoUri), "Duplicate key [{0}]dans la nouvelle collection.", dtoUri);
			appendNN(descriptionNN, dtoUri.getKey());
		}
	}

	/**
	 * Supprime toutes les associations liées à l'objet source.
	 * @param nn description de la nn
	 */
	private void removeNN(final DescriptionNN nn) {
		//FieldName
		final String sourceFieldName = nn.sourceField.getName();

		final String taskName = "TK_DELETE_" + nn.tableName;
		final String request = String.format("delete from %s where %s = #%s#", nn.tableName, sourceFieldName, sourceFieldName);

		processNN(taskName, request, nn.sourceField, nn.sourceValue, null, null);
	}

	/**
	 * Créer une association.
	 * @param nn description de la nn
	 * @param targetValue targetValue
	 */
	private void appendNN(final DescriptionNN nn, final Object targetValue) {
		//FieldName
		final String sourceFieldName = nn.sourceField.getName();
		final String targetFieldName = nn.targetField.getName();
		final String taskName = "TK_INSERT_" + nn.tableName;

		final String request = String.format("insert into %s (%s, %s) values (#%s#, #%s#)", nn.tableName, sourceFieldName, targetFieldName, sourceFieldName, targetFieldName);
		final int sqlRowCount = processNN(taskName, request, nn.sourceField, nn.sourceValue, nn.targetField, targetValue);
		if (sqlRowCount > 1) {
			throw new RuntimeException("Plus de 1 ligne a été insérée");
		} else if (sqlRowCount == 0) {
			throw new RuntimeException("Aucune ligne insérée");
		}
	}

	/**
	 * Supprime une association.
	 * @param nn description de la nn
	 * @param targetValue targetValue
	 */
	private void removeNN(final DescriptionNN nn, final Object targetValue) {
		//FieldName
		final String sourceFieldName = nn.sourceField.getName();
		final String targetFieldName = nn.targetField.getName();
		final String taskName = "TK_DELETE_" + nn.tableName;

		final String request = String.format("delete from %s where %s = #%s# and %s = #%s#",
				nn.tableName, sourceFieldName, sourceFieldName, targetFieldName, targetFieldName);
		final int sqlRowCount = processNN(taskName, request, nn.sourceField, nn.sourceValue, nn.targetField, targetValue);
		if (sqlRowCount > 1) {
			throw new RuntimeException("Plus de 1 ligne a été supprimée");
		} else if (sqlRowCount == 0) {
			throw new RuntimeException("Aucune ligne supprimée");
		}
	}

	private int processNN(final String taskDefinitionName, final String request,
			final DtField sourceField, final Object sourceValue,
			final DtField targetField, final Object targetValue) {
		//FieldName
		final String sourceFieldName = sourceField.getName();

		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(TaskEngineProc.class)
				.withRequest(request)
				.withInAttribute(sourceFieldName, sourceField.getDomain(), true); //IN, obligatoire
		if (targetField != null) {
			taskDefinitionBuilder.withInAttribute(targetField.getName(), targetField.getDomain(), true);
		}
		//OUT, obligatoire
		final TaskDefinition taskDefinition = taskDefinitionBuilder.withOutAttribute(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerDomain, true)
				.build();

		/* Création de la tache. */
		final TaskBuilder taskBuilder = new TaskBuilder(taskDefinition)
				.withValue(sourceFieldName, sourceValue);
		if (targetField != null) {
			taskBuilder.withValue(targetField.getName(), targetValue);
		}

		final TaskResult taskResult = taskManager.execute(taskBuilder.build());
		return getSqlRowCount(taskResult);
	}

	private static int getSqlRowCount(final TaskResult taskResult) {
		return taskResult.<Integer> getValue(AbstractTaskEngineSQL.SQL_ROWCOUNT).intValue();
	}

}
