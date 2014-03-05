package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.kernel.lang.Assertion;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Classe de bouchon pour mettre des données fictives dans les requêtes.
 *
 * @author tchassagnette
 * @version $Id: TaskPopulator.java,v 1.5 2014/01/28 18:49:55 pchretien Exp $
 */
public final class TaskPopulator {
	private final TaskDefinition taskDefinition;
	private final TaskBuilder taskBuilder;

	/**
	 * Constructeur.
	 * @param taskDefinition Definition de la tache
	 */
	public TaskPopulator(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		taskBuilder = new TaskBuilder(taskDefinition);
	}

	/**
	 * Rempli la task avec les données fictives.
	 * @return Tache préparé à l'exécution
	 */
	public Task populateTask() {
		for (final TaskAttribute attribute : taskDefinition.getAttributes()) {
			populateTaskAttribute(attribute);
		}
		return taskBuilder.build();
	}

	private void populateTaskAttribute(final TaskAttribute attribute) {
		if (attribute.isIn()) {
			final String attributeName = attribute.getName();
			Object value = null;
			switch (attribute.getDomain().getDataType()) {
				case Boolean:
					value = Boolean.TRUE;
					break;
				case String:
					value = "Test";
					break;
				case Date:
					value = new Date();
					break;
				case Double:
					value = Double.valueOf(1);
					break;
				case Integer:
					value = Integer.valueOf(1);
					break;
				case BigDecimal:
					value = BigDecimal.valueOf(1);
					break;
				case Long:
					value = Long.valueOf(1);
					break;
				case DtObject:
					value = DtObjectUtil.createDtObject(attribute.getDomain().getDtDefinition());
					break;
				case DtList:
					value = new DtList(attribute.getDomain().getDtDefinition());
					break;
				default:
					break;

			}
			taskBuilder.withValue(attributeName, value);

		}
	}
}
