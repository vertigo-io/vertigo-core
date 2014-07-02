package io.vertigo.dynamo.impl.persistence.util;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.persistence.BrokerNN;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestion des tables de relations NN.
 * @author dchallas
 */
public final class BrokerNNImpl implements BrokerNN {
	private final Domain integerDomain;
	private final WorkManager workManager;

	private static final class DescriptionNN {
		final String tableName;
		final DtField sourceField;
		final Object sourceValue;
		final DtField targetField;

		DescriptionNN(final DtListURIForAssociation dtListURIForAssociation) {
			Assertion.checkNotNull(dtListURIForAssociation);
			final AssociationNNDefinition associationNNDefinition = dtListURIForAssociation.getAssociationDefinition().castAsAssociationNNDefinition();

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
	 * @param workManager Manager des works
	 */
	public BrokerNNImpl(final WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
		integerDomain = new Domain("DO_INTEGER_BROKER", DataType.Integer, new FormatterNumber("FMT_NUMBER_BROKER"));
	}

	/** {@inheritDoc} */
	public void appendNN(final DtListURIForAssociation dtListURI, final URI<DtObject> uriToAppend) {
		Assertion.checkNotNull(uriToAppend);
		//---------------------------------------------------------------------
		appendNN(new DescriptionNN(dtListURI), uriToAppend.getKey());
	}

	/** {@inheritDoc} */
	public void removeAllNN(final DtListURIForAssociation dtListURI) {
		// on supprime tout
		removeNN(new DescriptionNN(dtListURI));
	}

	/** {@inheritDoc} */
	public void removeNN(final DtListURIForAssociation dtListURI, final URI<DtObject> uriToDelete) {
		Assertion.checkNotNull(uriToDelete);
		//---------------------------------------------------------------------
		removeNN(new DescriptionNN(dtListURI), uriToDelete.getKey());
	}

	/** {@inheritDoc} */
	public void updateNN(final DtListURIForAssociation dtListURI, final List<URI<? extends DtObject>> newUriList) {
		Assertion.checkNotNull(newUriList);
		//---------------------------------------------------------------------
		final DescriptionNN descriptionNN = new DescriptionNN(dtListURI);
		//1. on supprime tout
		removeNN(descriptionNN);
		//2. on enregistre la liste actuelle (un par un)
		final Set<URI<? extends DtObject>> set = new HashSet<>();
		for (final URI<? extends DtObject> dtoUri : newUriList) {
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
			throw new VRuntimeException("Plus de 1 ligne a été insérée", null);
		} else if (sqlRowCount == 0) {
			throw new VRuntimeException("Aucune ligne insérée", null);
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

		final String request = String.format("delete from %s where %s = #%s# and %s = #%s#",//
				nn.tableName, sourceFieldName, sourceFieldName, targetFieldName, targetFieldName);
		final int sqlRowCount = processNN(taskName, request, nn.sourceField, nn.sourceValue, nn.targetField, targetValue);
		if (sqlRowCount > 1) {
			throw new VRuntimeException("Plus de 1 ligne a été supprimée", null);
		} else if (sqlRowCount == 0) {
			throw new VRuntimeException("Aucune ligne supprimée", null);
		}
	}

	private int processNN(final String taskDefinitionName, final String request,//
			final DtField sourceField, final Object sourceValue, // 
			final DtField targetField, final Object targetValue) {
		//FieldName
		final String sourceFieldName = sourceField.getName();

		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskDefinitionName)//
				.withEngine(TaskEngineProc.class)//
				.withRequest(request)//
				.withAttribute(sourceFieldName, sourceField.getDomain(), true, true); //IN, obligatoire
		if (targetField != null) {
			taskDefinitionBuilder.withAttribute(targetField.getName(), targetField.getDomain(), true, true);
		}
		//OUT, obligatoire
		final TaskDefinition taskDefinition = taskDefinitionBuilder.withAttribute(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerDomain, true, false)//
				.build();

		/* Création de la tache. */
		final TaskBuilder taskBuilder = new TaskBuilder(taskDefinition);
		taskBuilder.withValue(sourceFieldName, sourceValue);
		if (targetField != null) {
			taskBuilder.withValue(targetField.getName(), targetValue);
		}
		WorkItem<TaskResult, Task> workItem = new WorkItem<>(taskBuilder.build(), taskDefinition.getTaskEngineProvider());
		workManager.process(workItem);
		final TaskResult taskResult = workItem.getResult();
		return getSqlRowCount(taskResult);
	}

	private static int getSqlRowCount(final TaskResult taskResult) {
		return taskResult.<Integer> getValue(AbstractTaskEngineSQL.SQL_ROWCOUNT).intValue();
	}

}
